"""PMU fix v4: Disable IRQs THEN clear status, read more PMU registers."""
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

# Configure GPIO10/11 for I2C
wreg(0x6000902C, 0x1B00); wreg(0x60009030, 0x1B00)
wreg(0x6000409C, 0x04); wreg(0x600040A0, 0x04)
wreg(0x6000457C, 0x500); wreg(0x60004580, 0x500)
wreg(0x6000400C, SDA_MASK | SCL_MASK)
wreg(0x60004028, SDA_MASK | SCL_MASK)
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
    return a1, a2, a3

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

# Read GPIO0 state
val = read_reg(0x6000403C)
gpio0 = (val >> 0) & 1 if val else -1
print(f'GPIO0 = {gpio0} (before fix)', flush=True)

# ============================================================
# STEP 1: Disable ALL interrupts
# ============================================================
print('Disabling all IRQs...', flush=True)
axp_write(0x40, 0x00)  # INTEN1 = 0
axp_write(0x41, 0x00)  # INTEN2 = 0
axp_write(0x42, 0x00)  # INTEN3 = 0

# STEP 2: Clear all IRQ status
print('Clearing all IRQ status...', flush=True)
axp_write(0x48, 0xFF)
axp_write(0x49, 0xFF)
axp_write(0x4A, 0xFF)

# Check GPIO0 now
time.sleep(0.05)
val = read_reg(0x6000403C)
gpio0 = (val >> 0) & 1 if val else -1
print(f'GPIO0 = {gpio0} (after disable+clear IRQs)', flush=True)

# Verify
for reg, name in [(0x40,'INTEN1'),(0x41,'INTEN2'),(0x42,'INTEN3'),
                   (0x48,'INTSTS1'),(0x49,'INTSTS2'),(0x4A,'INTSTS3')]:
    v = axp_read(reg)
    print(f'  {name}(0x{reg:02X}) = 0x{v:02X}', flush=True)

# ============================================================
# STEP 3: Read PMU GPIO/output config registers
# ============================================================
print('Reading PMU output config...', flush=True)
# AXP2101 GPIO config registers
for reg in range(0x90, 0x99):
    v = axp_read(reg)
    print(f'  PMU 0x{reg:02X} = 0x{v:02X}', flush=True)

# Power status
for reg, name in [(0x00,'STATUS1'),(0x01,'STATUS2'),(0x10,'COMMON_CFG'),
                   (0x20,'BATFET_CTRL'),(0x21,'DIE_TEMP_CFG'),(0x22,'PWROFF_EN'),
                   (0x23,'PWROFF_TIME'),(0x24,'SLEEP_CFG'),(0x25,'WAKEUP_CFG'),
                   (0x26,'FAST_PWRON')]:
    v = axp_read(reg)
    print(f'  {name}(0x{reg:02X}) = 0x{v:02X}', flush=True)

# ============================================================
# STEP 4: Try enabling GPIO0 pull-up more aggressively via ESP32
# ============================================================
print('Enabling ESP32 GPIO0 strong pull-up...', flush=True)
# IO_MUX_GPIO0_REG = 0x60009004
# Set FUN_WPU (bit 8) = 1, FUN_WPD (bit 7) = 0, FUN_IE (bit 9) = 1, MCU_SEL=1 (bit 12)
wreg(0x60009004, 0x1300)  # GPIO function + input enable + pull-up
time.sleep(0.02)
val = read_reg(0x6000403C)
gpio0 = (val >> 0) & 1 if val else -1
print(f'GPIO0 = {gpio0} (after strong pull-up)', flush=True)

# Read IO_MUX_GPIO0 to verify
val = read_reg(0x60009004)
print(f'IO_MUX_GPIO0 = 0x{val:08X}', flush=True)

s.close()
print('DONE', flush=True)
