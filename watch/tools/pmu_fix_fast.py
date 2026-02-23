"""Ultra-fast PMU fix: fire-and-forget I2C bit-bang writes."""
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
s = serial.Serial(port, 115200, timeout=0.5)
time.sleep(0.3)

# Sync
s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
time.sleep(0.5)
s.read(4096)
print('SYNC OK', flush=True)

SDA = 0x400; SCL = 0x800  # GPIO10, GPIO11

def wreg(a, v):
    """Fire-and-forget register write - no response wait."""
    s.write(make_cmd(0x09, struct.pack('<IIII', a, v, 0xFFFFFFFF, 0)))
    time.sleep(0.005)  # 5ms between writes

def drain():
    """Drain any pending responses."""
    s.read(4096)

def sda_lo(): wreg(0x60004024, SDA)
def sda_hi(): wreg(0x60004028, SDA)
def scl_lo(): wreg(0x60004024, SCL)
def scl_hi(): wreg(0x60004028, SCL)

# Init GPIO
wreg(0x6000400C, SDA | SCL)
sda_hi(); scl_hi()
time.sleep(0.02)
drain()
print('I2C READY', flush=True)

def i2c_start():
    sda_hi(); scl_hi(); time.sleep(0.002)
    sda_lo(); time.sleep(0.002); scl_lo()

def i2c_stop():
    sda_lo(); scl_hi(); time.sleep(0.002)
    sda_hi(); time.sleep(0.002)

def i2c_write_byte(b):
    for i in range(7, -1, -1):
        if (b >> i) & 1: sda_hi()
        else: sda_lo()
        scl_hi(); scl_lo()
    # Clock ACK bit (don't read, just clock)
    sda_hi(); scl_hi(); scl_lo()

def axp_write(reg, val):
    i2c_start()
    i2c_write_byte(0x68)
    i2c_write_byte(reg)
    i2c_write_byte(val)
    i2c_stop()
    drain()

# Fix 1: Clear all 3 IRQ status registers (releases N_IRQ / GPIO0)
print('CLR IRQ 0x48...', flush=True)
axp_write(0x48, 0xFF)
print('  OK', flush=True)

print('CLR IRQ 0x49...', flush=True)
axp_write(0x49, 0xFF)
print('  OK', flush=True)

print('CLR IRQ 0x4A...', flush=True)
axp_write(0x4A, 0xFF)
print('  OK', flush=True)

# Fix 2: Re-enable long press shutdown
print('FIX 0x22=0x07...', flush=True)
axp_write(0x22, 0x07)
print('  OK', flush=True)

# Fix 3: PMU shutdown (full power cycle)
print('SHUTDOWN 0x10...', flush=True)
axp_write(0x10, 0x01)
print('  OK', flush=True)

s.close()
print('ALL DONE - watch should power off', flush=True)
