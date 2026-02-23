"""PMU fix v3: Properly configure GPIO IO_MUX before I2C bit-bang."""
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

# Sync (send twice for reliability)
for _ in range(2):
    s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
    time.sleep(0.3)
    s.read(4096)
print('SYNC OK', flush=True)

SDA = 10  # GPIO10
SCL = 11  # GPIO11
SDA_MASK = 1 << SDA  # 0x400
SCL_MASK = 1 << SCL  # 0x800

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
# STEP 1: Configure GPIO10/11 for I2C bit-bang
# ============================================================

# 1a. Set IO_MUX to GPIO function with pull-up
#   MCU_SEL=1 (GPIO), FUN_DRV=2, FUN_IE=1, FUN_WPU=1
#   = (1<<12) | (2<<10) | (1<<9) | (1<<8) = 0x1B00
wreg(0x6000902C, 0x1B00)  # IO_MUX_GPIO10 (SDA)
wreg(0x60009030, 0x1B00)  # IO_MUX_GPIO11 (SCL)

# 1b. Set GPIO_PIN for open-drain mode (PAD_DRIVER=1, bit 2)
wreg(0x6000409C, 0x04)    # GPIO_PIN10 (SDA) open-drain
wreg(0x600040A0, 0x04)    # GPIO_PIN11 (SCL) open-drain

# 1c. Set GPIO_FUNC OUT_SEL to simple GPIO (256) with OEN_SEL=1
#   OUT_SEL=256 (0x100) | OEN_SEL=1 (bit 10) = 0x500
wreg(0x6000457C, 0x500)   # GPIO_FUNC10_OUT_SEL
wreg(0x60004580, 0x500)   # GPIO_FUNC11_OUT_SEL

# 1d. Clear GPIO_OUT data bits (output LOW when enabled)
wreg(0x6000400C, SDA_MASK | SCL_MASK)  # GPIO_OUT_W1TC

# 1e. Initially disable both outputs (let pins float HIGH)
wreg(0x60004028, SDA_MASK | SCL_MASK)  # GPIO_ENABLE_W1TC

time.sleep(0.02)
drain()

# Verify configuration
val = read_reg(0x6000902C)
print(f'IO_MUX_GPIO10 = 0x{val:08X} (expect 0x1B00)', flush=True)
val = read_reg(0x6000403C)
gpio10 = (val >> 10) & 1
gpio11 = (val >> 11) & 1
print(f'GPIO_IN: SDA={gpio10} SCL={gpio11} (expect both 1 = HIGH)', flush=True)

if gpio10 != 1 or gpio11 != 1:
    print('WARNING: I2C pins not HIGH - pull-ups may be missing', flush=True)

print('GPIO CONFIG DONE', flush=True)

# ============================================================
# STEP 2: I2C bit-bang functions
# ============================================================

def sda_lo():
    wreg(0x60004024, SDA_MASK)  # GPIO_ENABLE_W1TS - drive LOW

def sda_hi():
    wreg(0x60004028, SDA_MASK)  # GPIO_ENABLE_W1TC - float HIGH

def scl_lo():
    wreg(0x60004024, SCL_MASK)  # GPIO_ENABLE_W1TS - drive LOW

def scl_hi():
    wreg(0x60004028, SCL_MASK)  # GPIO_ENABLE_W1TC - float HIGH

def i2c_start():
    sda_hi(); scl_hi()
    time.sleep(0.005)
    sda_lo()
    time.sleep(0.005)
    scl_lo()

def i2c_stop():
    sda_lo()
    scl_hi()
    time.sleep(0.005)
    sda_hi()
    time.sleep(0.005)

def i2c_write_byte(b):
    for i in range(7, -1, -1):
        if (b >> i) & 1:
            sda_hi()
        else:
            sda_lo()
        time.sleep(0.001)
        scl_hi()
        time.sleep(0.001)
        scl_lo()
        time.sleep(0.001)
    # ACK bit: release SDA, clock, read (we skip read, just clock)
    sda_hi()
    time.sleep(0.001)
    scl_hi()
    time.sleep(0.001)
    # Read SDA for ACK
    val = read_reg(0x6000403C)
    ack = 0 if val is None else (val >> SDA) & 1
    scl_lo()
    time.sleep(0.001)
    return ack == 0  # ACK = SDA LOW

def axp_write(reg, val):
    i2c_start()
    a1 = i2c_write_byte(0x68)  # AXP2101 addr 0x34, write
    a2 = i2c_write_byte(reg)
    a3 = i2c_write_byte(val)
    i2c_stop()
    drain()
    return a1, a2, a3

def axp_read(reg):
    """Read a single byte from AXP2101 register."""
    # Write register address
    i2c_start()
    i2c_write_byte(0x68)  # write mode
    i2c_write_byte(reg)
    # Repeated start + read
    i2c_start()
    i2c_write_byte(0x69)  # read mode
    # Read 8 bits
    result = 0
    sda_hi()  # release SDA for slave to drive
    for i in range(7, -1, -1):
        time.sleep(0.001)
        scl_hi()
        time.sleep(0.001)
        val = read_reg(0x6000403C)
        bit = 0 if val is None else (val >> SDA) & 1
        result |= (bit << i)
        scl_lo()
        time.sleep(0.001)
    # Send NACK (SDA HIGH during ACK clock)
    sda_hi()
    time.sleep(0.001)
    scl_hi()
    time.sleep(0.001)
    scl_lo()
    i2c_stop()
    drain()
    return result

# ============================================================
# STEP 3: Test I2C communication
# ============================================================

print('Testing I2C read of AXP2101 reg 0x03 (chip ID)...', flush=True)
chip_id = axp_read(0x03)
print(f'  Chip ID = 0x{chip_id:02X} (expect 0x4A for AXP2101)', flush=True)

if chip_id != 0x4A:
    print('WARNING: Unexpected chip ID - I2C may not be working!', flush=True)
    print('Continuing anyway...', flush=True)

# ============================================================
# STEP 4: Read current PMU state
# ============================================================

print('Reading current PMU registers...', flush=True)
for reg, name in [(0x22, 'PWROFF_EN'), (0x40, 'INTEN1'), (0x41, 'INTEN2'), (0x42, 'INTEN3'),
                   (0x48, 'INTSTS1'), (0x49, 'INTSTS2'), (0x4A, 'INTSTS3')]:
    val = axp_read(reg)
    print(f'  0x{reg:02X} ({name}) = 0x{val:02X}', flush=True)

# ============================================================
# STEP 5: Fix PMU registers
# ============================================================

# Fix 1: Clear all IRQ status (releases N_IRQ pin → GPIO0 goes HIGH)
print('Clearing IRQ status registers...', flush=True)
for reg in [0x48, 0x49, 0x4A]:
    acks = axp_write(reg, 0xFF)
    print(f'  Write 0x{reg:02X}=0xFF acks={acks}', flush=True)

# Fix 2: Re-enable long press shutdown
print('Re-enabling long press shutdown...', flush=True)
acks = axp_write(0x22, 0x07)
print(f'  Write 0x22=0x07 acks={acks}', flush=True)

# Verify GPIO0 after IRQ clear
time.sleep(0.05)
val = read_reg(0x6000403C)
gpio0 = (val >> 0) & 1 if val else -1
print(f'GPIO0 after IRQ clear = {gpio0} ({"HIGH=FIXED!" if gpio0==1 else "still LOW"})', flush=True)

# Re-read IRQ status to verify cleared
for reg in [0x48, 0x49, 0x4A]:
    val = axp_read(reg)
    print(f'  After clear: 0x{reg:02X} = 0x{val:02X}', flush=True)

# ============================================================
# STEP 6: Shutdown PMU (power cycle)
# ============================================================

if gpio0 == 1:
    print('GPIO0 is HIGH! Fix worked!', flush=True)
    print('Triggering PMU shutdown for clean power cycle...', flush=True)
    axp_write(0x10, 0x01)
    print('PMU shutdown sent. Unplug USB, wait 5s, replug WITHOUT holding buttons.', flush=True)
else:
    print('GPIO0 still LOW. Trying PMU shutdown anyway...', flush=True)
    axp_write(0x10, 0x01)
    print('PMU shutdown sent. Unplug, wait 10s (drain battery capacitors), replug.', flush=True)

s.close()
print('DONE', flush=True)
