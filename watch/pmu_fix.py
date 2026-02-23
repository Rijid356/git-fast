"""
Bit-bang I2C to AXP2101 PMU via ESP32-S3 ROM bootloader WRITE_REG/READ_REG commands.
Fixes PMU registers and triggers shutdown for full power cycle.
"""
import serial, struct, time

SLIP_END = 0xC0
SDA_BIT = 10  # GPIO10
SCL_BIT = 11  # GPIO11
SDA_MASK = 1 << SDA_BIT   # 0x400
SCL_MASK = 1 << SCL_BIT   # 0x800

# ESP32-S3 GPIO registers
GPIO_OUT_W1TS = 0x60004008
GPIO_OUT_W1TC = 0x6000400C
GPIO_ENABLE_W1TS = 0x60004024
GPIO_ENABLE_W1TC = 0x60004028
GPIO_IN = 0x6000403C

AXP_ADDR_W = 0x68  # (0x34 << 1) | 0 = write mode

def slip_encode(data):
    out = bytes([SLIP_END])
    for b in data:
        if b == 0xC0:
            out += b'\xdb\xdc'
        elif b == 0xDB:
            out += b'\xdb\xdd'
        else:
            out += bytes([b])
    out += bytes([SLIP_END])
    return out

def make_cmd(op, data, chk=0):
    hdr = struct.pack('<BBHI', 0x00, op, len(data), chk)
    return slip_encode(hdr + data)

# Connect
print("Opening COM3...")
s = serial.Serial('COM3', 115200, timeout=2)
time.sleep(0.3)

# Sync with ROM bootloader
print("Syncing with ROM bootloader...")
s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
time.sleep(0.5)
resp = s.read(2048)
if not resp:
    print("ERROR: No sync response - is device in download mode?")
    s.close()
    exit(1)
print("Synced OK")

def write_reg(addr, val):
    data = struct.pack('<IIII', addr, val, 0xFFFFFFFF, 0)
    s.write(make_cmd(0x09, data))
    time.sleep(0.010)  # 10ms between writes to avoid USB buffer overflow
    s.read(256)

def read_reg(addr):
    data = struct.pack('<I', addr)
    s.write(make_cmd(0x0A, data))
    time.sleep(0.010)
    resp = s.read(256)
    raw = resp.replace(b'\xdb\xdc', bytes([0xC0])).replace(b'\xdb\xdd', bytes([0xDB]))
    for f in raw.split(bytes([SLIP_END])):
        if len(f) >= 8 and f[0] == 0x01 and f[1] == 0x0A:
            return struct.unpack('<I', f[4:8])[0]
    return None

# I2C bit-bang primitives
def sda_low():
    write_reg(GPIO_OUT_W1TC, SDA_MASK)
    write_reg(GPIO_ENABLE_W1TS, SDA_MASK)

def sda_high():
    write_reg(GPIO_ENABLE_W1TC, SDA_MASK)

def scl_low():
    write_reg(GPIO_OUT_W1TC, SCL_MASK)
    write_reg(GPIO_ENABLE_W1TS, SCL_MASK)

def scl_high():
    write_reg(GPIO_ENABLE_W1TC, SCL_MASK)

def read_sda():
    val = read_reg(GPIO_IN)
    if val is None:
        return 1  # assume NACK on error
    return (val >> SDA_BIT) & 1

def i2c_start():
    sda_high()
    scl_high()
    time.sleep(0.001)
    sda_low()
    time.sleep(0.001)
    scl_low()

def i2c_stop():
    sda_low()
    scl_high()
    time.sleep(0.001)
    sda_high()
    time.sleep(0.001)

def i2c_write_byte(byte_val):
    for bit in range(7, -1, -1):
        if byte_val & (1 << bit):
            sda_high()
        else:
            sda_low()
        scl_high()
        scl_low()
    # Read ACK
    sda_high()  # Release SDA for slave ACK
    scl_high()
    ack = read_sda()
    scl_low()
    return ack == 0  # ACK = SDA pulled LOW by slave

def i2c_write_reg(reg, val):
    """Write a single byte to an AXP2101 register via I2C."""
    i2c_start()
    ok1 = i2c_write_byte(AXP_ADDR_W)
    ok2 = i2c_write_byte(reg)
    ok3 = i2c_write_byte(val)
    i2c_stop()
    return ok1 and ok2 and ok3

# ============================================
# STEP 0: Initialize GPIO for I2C bit-bang
# ============================================
print("\n=== PMU Recovery via I2C Bit-Bang ===")
print("\nStep 0: Setting up GPIO for I2C...")
write_reg(GPIO_OUT_W1TC, SDA_MASK | SCL_MASK)  # Output register = 0 for both
sda_high()  # Release SDA (float HIGH via pull-up)
scl_high()  # Release SCL (float HIGH via pull-up)
time.sleep(0.01)

# Verify I2C bus is idle
gpio_in = read_reg(GPIO_IN)
if gpio_in is not None:
    sda_val = (gpio_in >> SDA_BIT) & 1
    scl_val = (gpio_in >> SCL_BIT) & 1
    print(f"  I2C bus: SDA={sda_val} SCL={scl_val}")
    if sda_val == 0 or scl_val == 0:
        print("  WARNING: Bus not idle, doing clock recovery...")
        for i in range(9):
            scl_high()
            time.sleep(0.001)
            scl_low()
            time.sleep(0.001)
        scl_high()
        sda_high()
        time.sleep(0.005)
else:
    print("  WARNING: Could not read GPIO state")

# ============================================
# STEP 1: Re-enable long press shutdown
# ============================================
print("\nStep 1: Writing reg 0x22 = 0x07 (re-enable long press shutdown)...")
ok = i2c_write_reg(0x22, 0x07)
print(f"  {'ACK - SUCCESS' if ok else 'NACK - FAILED'}")

# ============================================
# STEP 2: Clear IRQ status registers
# ============================================
print("\nStep 2: Clearing IRQ status (regs 0x48-0x4A)...")
for reg in [0x48, 0x49, 0x4A]:
    ok = i2c_write_reg(reg, 0xFF)
    print(f"  Reg 0x{reg:02X}: {'ACK' if ok else 'NACK'}")

# ============================================
# STEP 3: Trigger PMU software shutdown
# ============================================
print("\nStep 3: Triggering PMU shutdown (reg 0x10, bit 0)...")
ok = i2c_write_reg(0x10, 0x01)
print(f"  {'ACK - SHUTDOWN SENT' if ok else 'NACK - FAILED'}")

s.close()

print("\n=== DONE ===")
print("If PMU shutdown succeeded, the watch should be completely OFF.")
print("Wait 5 seconds, then plug in USB WITHOUT holding any buttons.")
print("The device should boot from flash normally.")
