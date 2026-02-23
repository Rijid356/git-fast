"""Enable PMU power rails via I2C, then toggle backlight GPIO45.
All from download mode - no firmware needed."""
import serial, struct, time, sys

SLIP_END = 0xC0

def slip_encode(data):
    out = bytes([SLIP_END])
    for b in data:
        if b == 0xC0: out += b'\xdb\xdc'
        elif b == 0xDB: out += b'\xdb\xdd'
        else: out += bytes([b])
    out += bytes([SLIP_END])
    return out

def slip_decode(data):
    out = b''
    i = 0
    while i < len(data):
        if data[i] == 0xDB and i+1 < len(data):
            if data[i+1] == 0xDC: out += b'\xc0'
            elif data[i+1] == 0xDD: out += b'\xdb'
            i += 2
        elif data[i] == 0xC0:
            i += 1
        else:
            out += bytes([data[i]])
            i += 1
    return out

def make_cmd(op, data, chk=0):
    return slip_encode(struct.pack('<BBHI', 0x00, op, len(data), chk) + data)

port = sys.argv[1] if len(sys.argv) > 1 else 'COM3'
s = serial.Serial(port, 115200, timeout=0.3)
time.sleep(0.3)

for _ in range(2):
    s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
    time.sleep(0.3)
    s.read(4096)
print('SYNC OK', flush=True)

SDA = 10; SCL = 11
SDA_MASK = 1 << SDA; SCL_MASK = 1 << SCL

def wreg(a, v):
    s.write(make_cmd(0x09, struct.pack('<IIII', a, v, 0xFFFFFFFF, 0)))
    time.sleep(0.003)

def read_reg(addr):
    s.write(make_cmd(0x0A, struct.pack('<I', addr)))
    time.sleep(0.05)
    raw = s.read(4096)
    decoded = slip_decode(raw)
    if len(decoded) >= 10:
        return struct.unpack_from('<I', decoded, 4)[0]
    return None

def drain():
    s.read(4096)

# ============================================================
# PART 1: Configure I2C GPIO10/11
# ============================================================
wreg(0x6000902C, 0x1B00); wreg(0x60009030, 0x1B00)  # IO_MUX: GPIO func, pull-up
wreg(0x6000409C, 0x04); wreg(0x600040A0, 0x04)      # Open-drain
wreg(0x6000457C, 0x500); wreg(0x60004580, 0x500)     # GPIO out_sel
wreg(0x6000400C, SDA_MASK | SCL_MASK)                 # GPIO_OUT clear
wreg(0x60004028, SDA_MASK | SCL_MASK)                 # Disable output (float HIGH)
time.sleep(0.02)
drain()
print('I2C READY', flush=True)

def sda_lo(): wreg(0x60004024, SDA_MASK)
def sda_hi(): wreg(0x60004028, SDA_MASK)
def scl_lo(): wreg(0x60004024, SCL_MASK)
def scl_hi(): wreg(0x60004028, SCL_MASK)

def i2c_start():
    sda_hi(); scl_hi(); time.sleep(0.005)
    sda_lo(); time.sleep(0.005); scl_lo()

def i2c_stop():
    sda_lo(); scl_hi(); time.sleep(0.005)
    sda_hi(); time.sleep(0.005)

def i2c_write_byte(b):
    for i in range(7, -1, -1):
        if (b >> i) & 1: sda_hi()
        else: sda_lo()
        time.sleep(0.001); scl_hi(); time.sleep(0.001); scl_lo(); time.sleep(0.001)
    sda_hi(); time.sleep(0.001); scl_hi(); time.sleep(0.001)
    val = read_reg(0x6000403C)
    ack = 0 if val is None else (val >> SDA) & 1
    scl_lo(); time.sleep(0.001)
    return ack == 0

def axp_write(reg, val):
    i2c_start()
    a1 = i2c_write_byte(0x68)
    a2 = i2c_write_byte(reg)
    a3 = i2c_write_byte(val)
    i2c_stop()
    drain()
    return a1 and a2 and a3

def axp_read(reg):
    i2c_start()
    i2c_write_byte(0x68)
    i2c_write_byte(reg)
    i2c_start()
    i2c_write_byte(0x69)
    result = 0
    sda_hi()
    for i in range(7, -1, -1):
        time.sleep(0.001); scl_hi(); time.sleep(0.001)
        val = read_reg(0x6000403C)
        bit = 0 if val is None else (val >> SDA) & 1
        result |= (bit << i)
        scl_lo(); time.sleep(0.001)
    sda_hi(); time.sleep(0.001); scl_hi(); time.sleep(0.001); scl_lo()
    i2c_stop()
    drain()
    return result

# ============================================================
# PART 2: Read current PMU LDO state
# ============================================================
ldo_ctrl = axp_read(0x90)
print(f'LDO enable reg 0x90 = 0x{ldo_ctrl:02X}', flush=True)
print(f'  ALDO1={ldo_ctrl&1} ALDO2={(ldo_ctrl>>1)&1} ALDO3={(ldo_ctrl>>2)&1} ALDO4={(ldo_ctrl>>3)&1}', flush=True)
print(f'  BLDO1={(ldo_ctrl>>4)&1} BLDO2={(ldo_ctrl>>5)&1}', flush=True)

dcdc_ctrl = axp_read(0x80)
print(f'DCDC enable reg 0x80 = 0x{dcdc_ctrl:02X}', flush=True)

# ============================================================
# PART 3: Enable ALL power rails
# ============================================================
print('Enabling all power rails...', flush=True)

# Set voltages first (AXP2101 voltage registers)
# ALDO1 voltage: reg 0x92, formula: (val * 100 + 500) mV, for 3300mV: (3300-500)/100 = 28
axp_write(0x92, 28)  # ALDO1 = 3300mV
axp_write(0x93, 28)  # ALDO2 = 3300mV
axp_write(0x94, 28)  # ALDO3 = 3300mV
axp_write(0x95, 28)  # ALDO4 = 3300mV
axp_write(0x97, 28)  # BLDO2 = 3300mV

# Enable all LDOs: ALDO1-4 + BLDO2
# Reg 0x90: bit0=ALDO1, bit1=ALDO2, bit2=ALDO3, bit3=ALDO4, bit4=BLDO1, bit5=BLDO2
axp_write(0x90, 0x2F)  # Enable ALDO1-4 + BLDO2

# Verify
ldo_ctrl = axp_read(0x90)
print(f'LDO enable after write: 0x{ldo_ctrl:02X}', flush=True)

time.sleep(0.2)  # Let power stabilize
print('Power rails enabled', flush=True)

# ============================================================
# PART 4: Toggle backlight GPIO45
# ============================================================
print('Setting GPIO45 (backlight) HIGH...', flush=True)

BL_BIT = 1 << 13  # GPIO45 = bit 13 in high bank (45-32=13)

# IO_MUX for GPIO45
wreg(0x600090B8, 0x1E00)  # GPIO func, max drive, input enable

# GPIO_FUNC45_OUT_SEL
wreg(0x60004608, 0x500)   # Simple GPIO, OEN from GPIO_ENABLE

# GPIO_PIN45: push-pull
wreg(0x60004128, 0x00)

# Output HIGH
wreg(0x60004014, BL_BIT)   # GPIO_OUT1_W1TS

# Enable output
wreg(0x60004030, BL_BIT)   # GPIO_ENABLE1_W1TS

# Verify
time.sleep(0.1)
gpio_out1 = read_reg(0x60004010)  # GPIO_OUT1_REG
gpio_en1 = read_reg(0x6000402C)   # GPIO_ENABLE1_REG
gpio_in1 = read_reg(0x60004040)   # GPIO_IN1_REG
iomux45 = read_reg(0x600090B8)
func45 = read_reg(0x60004608)

print(f'GPIO_OUT1   = 0x{gpio_out1:08X} (bit13={((gpio_out1 or 0)>>13)&1})' if gpio_out1 is not None else 'failed', flush=True)
print(f'GPIO_EN1    = 0x{gpio_en1:08X} (bit13={((gpio_en1 or 0)>>13)&1})' if gpio_en1 is not None else 'failed', flush=True)
print(f'GPIO_IN1    = 0x{gpio_in1:08X} (bit13={((gpio_in1 or 0)>>13)&1})' if gpio_in1 is not None else 'failed', flush=True)
print(f'IO_MUX_45   = 0x{iomux45:08X}' if iomux45 is not None else 'failed', flush=True)
print(f'FUNC45_SEL  = 0x{func45:08X}' if func45 is not None else 'failed', flush=True)

print('\nBacklight should be ON. Waiting 30 seconds...', flush=True)
time.sleep(30)

s.close()
print('DONE', flush=True)
