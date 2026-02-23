"""
Bit-bang I2C via ESP32-S3 ROM bootloader WRITE_MEM/READ_MEM
to read and fix AXP2101 PMU registers that are holding GPIO0 LOW.
"""
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

def make_cmd(op, data, chk=0):
    hdr = struct.pack('<BBHI', 0x00, op, len(data), chk)
    return slip_encode(hdr + data)

def parse_response(raw):
    raw = raw.replace(b'\xdb\xdc', bytes([0xC0])).replace(b'\xdb\xdd', bytes([0xDB]))
    frames = raw.split(bytes([SLIP_END]))
    for f in frames:
        if len(f) >= 12 and f[0] == 0x01:
            val = struct.unpack('<I', f[4:8])[0]
            return val
    return None

# GPIO registers
GPIO_OUT_W1TS    = 0x60004008
GPIO_OUT_W1TC    = 0x6000400C
GPIO_ENABLE_W1TS = 0x60004024
GPIO_ENABLE_W1TC = 0x60004028
GPIO_IN          = 0x60004004

SDA_PIN = 10; SCL_PIN = 11
SDA_MASK = (1 << SDA_PIN); SCL_MASK = (1 << SCL_PIN)

port = sys.argv[1] if len(sys.argv) > 1 else 'COM3'
s = serial.Serial(port, 115200, timeout=2)
time.sleep(0.3)

# Sync
s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
time.sleep(0.5)
s.read(2048)
print('Synced with ROM bootloader')

def write_reg(addr, val):
    s.write(make_cmd(0x09, struct.pack('<II', addr, val)))
    time.sleep(0.005)
    s.read(256)

def read_reg(addr):
    s.write(make_cmd(0x0A, struct.pack('<I', addr)))
    time.sleep(0.005)
    return parse_response(s.read(256))

# Configure IO_MUX for GPIO10/11 as GPIO function
IO_MUX_GPIO10 = 0x60009004 + 10 * 4
IO_MUX_GPIO11 = 0x60009004 + 11 * 4
mux_val = (1 << 12) | (1 << 9) | (1 << 8) | (2 << 10)
write_reg(IO_MUX_GPIO10, mux_val)
write_reg(IO_MUX_GPIO11, mux_val)

# GPIO matrix: route GPIO output
FUNC_OUT_SEL_BASE = 0x60004554
write_reg(FUNC_OUT_SEL_BASE + SDA_PIN * 4, 0x80)
write_reg(FUNC_OUT_SEL_BASE + SCL_PIN * 4, 0x80)

# Init: both HIGH (output disabled, pull-up)
write_reg(GPIO_OUT_W1TC, SDA_MASK | SCL_MASK)
write_reg(GPIO_ENABLE_W1TC, SDA_MASK | SCL_MASK)
time.sleep(0.01)
print('I2C GPIO configured')

def sda_low():  write_reg(GPIO_ENABLE_W1TS, SDA_MASK)
def sda_high(): write_reg(GPIO_ENABLE_W1TC, SDA_MASK)
def scl_low():  write_reg(GPIO_ENABLE_W1TS, SCL_MASK)
def scl_high(): write_reg(GPIO_ENABLE_W1TC, SCL_MASK)

def read_sda():
    val = read_reg(GPIO_IN)
    return 1 if (val and (val & SDA_MASK)) else 0

def i2c_start():
    sda_high(); scl_high()
    time.sleep(0.001)
    sda_low()
    time.sleep(0.001)
    scl_low()

def i2c_stop():
    sda_low(); scl_high()
    time.sleep(0.001)
    sda_high()
    time.sleep(0.001)

def i2c_write_byte(byte_val):
    for i in range(7, -1, -1):
        if (byte_val >> i) & 1: sda_high()
        else: sda_low()
        scl_high(); scl_low()
    sda_high()
    scl_high()
    ack = read_sda()
    scl_low()
    return ack == 0

def i2c_read_byte(send_ack=True):
    val = 0
    sda_high()
    for i in range(7, -1, -1):
        scl_high()
        bit = read_sda()
        val |= (bit << i)
        scl_low()
    if send_ack: sda_low()
    else: sda_high()
    scl_high(); scl_low(); sda_high()
    return val

AXP_ADDR = 0x34

def axp_read(reg):
    i2c_start()
    if not i2c_write_byte((AXP_ADDR << 1) | 0):
        print(f'  NACK addr write 0x{reg:02X}'); i2c_stop(); return None
    if not i2c_write_byte(reg):
        print(f'  NACK reg 0x{reg:02X}'); i2c_stop(); return None
    i2c_start()
    if not i2c_write_byte((AXP_ADDR << 1) | 1):
        print(f'  NACK addr read 0x{reg:02X}'); i2c_stop(); return None
    val = i2c_read_byte(send_ack=False)
    i2c_stop()
    return val

def axp_write(reg, val):
    i2c_start()
    if not i2c_write_byte((AXP_ADDR << 1) | 0):
        print(f'  NACK addr 0x{reg:02X}'); i2c_stop(); return False
    if not i2c_write_byte(reg):
        print(f'  NACK reg 0x{reg:02X}'); i2c_stop(); return False
    if not i2c_write_byte(val):
        print(f'  NACK data 0x{val:02X}'); i2c_stop(); return False
    i2c_stop()
    return True

print()
print('=== Reading AXP2101 Registers ===')

regs = {
    0x22: 'PWROFF_EN',
    0x40: 'INTEN1', 0x41: 'INTEN2', 0x42: 'INTEN3',
    0x48: 'INTSTS1', 0x49: 'INTSTS2', 0x4A: 'INTSTS3',
    0x10: 'COMMON_CONFIG', 0x25: 'PWROK_SEQU_CTRL',
    0x27: 'IRQ_OFF_ON_LEVEL_CTRL',
}

values = {}
for addr, name in regs.items():
    val = axp_read(addr)
    if val is not None:
        values[addr] = val
        print(f'  0x{addr:02X} ({name}) = 0x{val:02X} = {val:08b}b')
    else:
        print(f'  0x{addr:02X} ({name}) = FAILED')

print()
print('=== Fixing PMU ===')

# 1. Clear ALL pending IRQ status (write 0xFF to status regs)
print('Clearing IRQ status registers...')
axp_write(0x48, 0xFF)
axp_write(0x49, 0xFF)
axp_write(0x4A, 0xFF)

# 2. Re-enable long press shutdown (set bit 1 of reg 0x22)
if 0x22 in values:
    new_val = values[0x22] | 0x02  # Set bit 1
    print(f'Setting PWROFF_EN: 0x{values[0x22]:02X} -> 0x{new_val:02X}')
    axp_write(0x22, new_val)

# 3. Verify fixes
print()
print('=== Verifying ===')
for addr in [0x22, 0x48, 0x49, 0x4A]:
    val = axp_read(addr)
    name = regs.get(addr, '?')
    if val is not None:
        print(f'  0x{addr:02X} ({name}) = 0x{val:02X}')
    else:
        print(f'  0x{addr:02X} ({name}) = FAILED')

# Check GPIO0 state
print()
gpio_in = read_reg(GPIO_IN)
gpio0 = (gpio_in & 1) if gpio_in else 0
print(f'GPIO_IN = 0x{gpio_in:08X}' if gpio_in else 'GPIO_IN read failed')
print(f'GPIO0 = {gpio0} ({"HIGH - FIXED!" if gpio0 else "still LOW"})')

s.close()
print()
print('Done. Try watchdog_reset now to boot firmware.')
