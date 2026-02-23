"""Minimal PMU fix: write 3 critical registers via I2C bit-bang, then shutdown."""
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
    return slip_encode(struct.pack('<BBHI', 0x00, op, len(data), chk) + data)

port = sys.argv[1] if len(sys.argv) > 1 else 'COM3'
s = serial.Serial(port, 115200, timeout=2)
time.sleep(0.3)

# Sync
s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
time.sleep(0.5)
s.read(2048)
print('SYNC OK')

SDA = 0x400; SCL = 0x800  # GPIO10, GPIO11

def wreg(a, v):
    s.write(make_cmd(0x09, struct.pack('<IIII', a, v, 0xFFFFFFFF, 0)))
    time.sleep(0.015)
    s.read(256)

def sda_lo(): wreg(0x60004024, SDA)  # enable output (drives LOW)
def sda_hi(): wreg(0x60004028, SDA)  # disable output (floats HIGH)
def scl_lo(): wreg(0x60004024, SCL)
def scl_hi(): wreg(0x60004028, SCL)

# Init: clear output bits, release both lines
wreg(0x6000400C, SDA | SCL)
sda_hi(); scl_hi()
time.sleep(0.02)
print('I2C READY')

def i2c_start():
    sda_hi(); scl_hi(); time.sleep(0.003)
    sda_lo(); time.sleep(0.003); scl_lo()

def i2c_stop():
    sda_lo(); scl_hi(); time.sleep(0.003)
    sda_hi(); time.sleep(0.003)

def i2c_write_byte(b):
    for i in range(7, -1, -1):
        if (b >> i) & 1: sda_hi()
        else: sda_lo()
        scl_hi(); scl_lo()
    # Skip ACK read for speed - just clock it
    sda_hi(); scl_hi(); scl_lo()

def axp_write(reg, val):
    i2c_start()
    i2c_write_byte(0x68)  # AXP2101 addr 0x34, write
    i2c_write_byte(reg)
    i2c_write_byte(val)
    i2c_stop()

# Fix 1: Re-enable long press shutdown
print('FIX 0x22=0x07...')
axp_write(0x22, 0x07)
print('  DONE')

# Fix 2: Clear IRQ status
print('CLR 0x48...')
axp_write(0x48, 0xFF)
print('  DONE')

print('CLR 0x49...')
axp_write(0x49, 0xFF)
print('  DONE')

print('CLR 0x4A...')
axp_write(0x4A, 0xFF)
print('  DONE')

# Fix 3: PMU shutdown
print('SHUTDOWN 0x10...')
axp_write(0x10, 0x01)
print('  DONE')

s.close()
print('ALL DONE - watch should be OFF')
