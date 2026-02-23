"""Check battery/power status and try to force VBUS system power."""
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

# Setup I2C
wreg(0x6000902C, 0x1B00); wreg(0x60009030, 0x1B00)
wreg(0x6000409C, 0x04); wreg(0x600040A0, 0x04)
wreg(0x6000457C, 0x500); wreg(0x60004580, 0x500)
wreg(0x6000400C, SDA_MASK | SCL_MASK)
wreg(0x60004028, SDA_MASK | SCL_MASK)
time.sleep(0.02); drain()
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
    i2c_stop(); drain()
    return a1 and a2 and a3

def axp_read(reg):
    i2c_start()
    i2c_write_byte(0x68)
    i2c_write_byte(reg)
    i2c_start()
    i2c_write_byte(0x69)
    result = 0; sda_hi()
    for i in range(7, -1, -1):
        time.sleep(0.001); scl_hi(); time.sleep(0.001)
        val = read_reg(0x6000403C)
        bit = 0 if val is None else (val >> SDA) & 1
        result |= (bit << i)
        scl_lo(); time.sleep(0.001)
    sda_hi(); time.sleep(0.001); scl_hi(); time.sleep(0.001); scl_lo()
    i2c_stop(); drain()
    return result

# Read ALL power status registers
print('=== Power Status ===', flush=True)
for reg, name in [
    (0x00, 'STATUS1'), (0x01, 'STATUS2'),
    (0x04, 'DATA_BUF0'), (0x05, 'DATA_BUF1'),
    (0x06, 'DATA_BUF2'), (0x07, 'DATA_BUF3'),
    (0x10, 'COMMON_CONFIG'),
    (0x18, 'CHARGE_GAUGE_WDT'),
    (0x20, 'BATFET_CTRL'), (0x22, 'PWROFF_EN'),
    (0x24, 'SLEEP_CFG'), (0x25, 'WAKEUP_CFG'),
    (0x30, 'VBUS_CUR_LIM'), (0x31, 'VBUS_VOL_LIM'),
]:
    v = axp_read(reg)
    print(f'  0x{reg:02X} ({name:20s}) = 0x{v:02X}  (0b{v:08b})', flush=True)

# Read charge status
print('\n=== Charge Config ===', flush=True)
for reg, name in [
    (0x61, 'LINEAR_CHG_CTL0'), (0x62, 'LINEAR_CHG_CTL1'),
    (0x63, 'LINEAR_CHG_CTL2'), (0x64, 'LINEAR_CHG_CTL3'),
]:
    v = axp_read(reg)
    print(f'  0x{reg:02X} ({name:20s}) = 0x{v:02X}', flush=True)

# Read battery voltage ADC
print('\n=== ADC/Battery ===', flush=True)
for reg, name in [
    (0x34, 'VBAT_H'), (0x35, 'VBAT_L'),
    (0x36, 'TS_H'), (0x37, 'TS_L'),
    (0x38, 'VBUS_H'), (0x39, 'VBUS_L'),
    (0x3A, 'VSYS_H'), (0x3B, 'VSYS_L'),
    (0xA4, 'BAT_PERCENT'),
]:
    v = axp_read(reg)
    print(f'  0x{reg:02X} ({name:20s}) = 0x{v:02X} ({v})', flush=True)

# Read DCDC and LDO enable/voltage
print('\n=== Power Output ===', flush=True)
for reg, name in [
    (0x80, 'DCDC_ONOFF'), (0x81, 'DCDC_FORCE_PWM'),
    (0x82, 'DCDC1_VOL'), (0x83, 'DCDC2_VOL'), (0x84, 'DCDC3_VOL'),
    (0x90, 'LDO_ONOFF0'), (0x91, 'LDO_ONOFF1'),
    (0x92, 'ALDO1_VOL'), (0x93, 'ALDO2_VOL'),
    (0x94, 'ALDO3_VOL'), (0x95, 'ALDO4_VOL'),
    (0x96, 'BLDO1_VOL'), (0x97, 'BLDO2_VOL'),
]:
    v = axp_read(reg)
    print(f'  0x{reg:02X} ({name:20s}) = 0x{v:02X}', flush=True)

# Try enabling ALL LDOs with multiple writes for reliability
print('\n=== Forcing all LDOs ON ===', flush=True)
for _ in range(3):
    axp_write(0x90, 0x3F)  # ALDO1-4 + BLDO1 + BLDO2
    time.sleep(0.05)
v = axp_read(0x90)
print(f'  LDO_ONOFF0 after 3x write: 0x{v:02X}', flush=True)

s.close()
print('DONE', flush=True)
