"""Force GPIO0 HIGH as output and immediately trigger watchdog reset."""
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
s = serial.Serial(port, 115200, timeout=0.5)
time.sleep(0.3)

for _ in range(2):
    s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
    time.sleep(0.3)
    s.read(4096)
print('SYNC OK', flush=True)

def wreg(a, v):
    s.write(make_cmd(0x09, struct.pack('<IIII', a, v, 0xFFFFFFFF, 0)))
    time.sleep(0.005)

def wreg_fast(a, v):
    """Fire and forget - no delay."""
    s.write(make_cmd(0x09, struct.pack('<IIII', a, v, 0xFFFFFFFF, 0)))

def read_reg(addr):
    s.write(make_cmd(0x0A, struct.pack('<I', addr)))
    time.sleep(0.05)
    raw = s.read(4096)
    decoded = slip_decode(raw)
    if len(decoded) >= 10:
        return struct.unpack_from('<I', decoded, 4)[0]
    return None

RTC_BASE = 0x60008000

# Step 1: Check current GPIO0
gpio_in = read_reg(0x6000403C)
print(f'GPIO0 before = {gpio_in & 1 if gpio_in else "?"}', flush=True)

# Step 2: Clear hold registers
wreg(RTC_BASE + 0x00A8, 0)  # DIG_PAD_HOLD
wreg(RTC_BASE + 0x00A4, 0)  # PAD_HOLD
print('Holds cleared', flush=True)

# Step 3: Configure GPIO0 as push-pull output HIGH, max drive strength
# IO_MUX: MCU_SEL=1 (GPIO), FUN_DRV=3 (max), FUN_IE=1
wreg(0x60009004, (1<<12) | (3<<10) | (1<<9))  # 0x1E00

# GPIO_FUNC0_OUT_SEL: simple GPIO (256) with OEN_SEL=1
# Addr: 0x60004554 + 0*4 = 0x60004554
wreg(0x60004554, 0x500)

# GPIO_PIN0: push-pull (PAD_DRIVER=0)
# Addr: 0x60004074 + 0*4 = 0x60004074
wreg(0x60004074, 0x00)

# Set GPIO0 output data to HIGH
wreg(0x60004008, 0x01)  # GPIO_OUT_W1TS, set bit 0

# Enable GPIO0 output
wreg(0x60004024, 0x01)  # GPIO_ENABLE_W1TS, set bit 0

time.sleep(0.05)

# Check if GPIO0 went HIGH
gpio_in = read_reg(0x6000403C)
gpio0 = gpio_in & 1 if gpio_in else -1
print(f'GPIO0 after driving HIGH = {gpio0}', flush=True)

if gpio0 == 1:
    print('GPIO0 IS HIGH! The output driver overcame the pull-down!', flush=True)
    print('Now: clear holds + enable hold on GPIO0 (to keep it HIGH) + WDT reset', flush=True)

    # Clear holds first
    wreg(RTC_BASE + 0x00A8, 0)
    wreg(RTC_BASE + 0x00A4, 0)

    # Now ENABLE hold on GPIO0 only (bit 0) to freeze it HIGH through the reset
    wreg(RTC_BASE + 0x00A8, 0x01)  # DIG_PAD_HOLD bit 0 = hold GPIO0

    # Trigger WDT reset
    wreg(RTC_BASE + 0x00AC, 0x50D83AA1)  # Unlock WDT
    wreg(RTC_BASE + 0x0098, (1 << 31) | (3 << 28))  # Enable WDT, stage0=reset
    wreg(RTC_BASE + 0x009C, 5000)  # ~33ms timeout
    wreg(RTC_BASE + 0x00A0, 1)  # Feed/start WDT

    print('WDT armed! Reset in ~33ms with GPIO0 held HIGH!', flush=True)
else:
    print('GPIO0 still LOW even as output! Testing with read of output data...', flush=True)
    gpio_out = read_reg(0x60004004)
    gpio_en = read_reg(0x60004020)
    iomux = read_reg(0x60009004)
    func0 = read_reg(0x60004554)
    print(f'GPIO_OUT = 0x{gpio_out:08X}, bit0 = {gpio_out & 1}' if gpio_out else 'failed', flush=True)
    print(f'GPIO_EN  = 0x{gpio_en:08X}, bit0 = {gpio_en & 1}' if gpio_en else 'failed', flush=True)
    print(f'IO_MUX0  = 0x{iomux:08X}' if iomux else 'failed', flush=True)
    print(f'FUNC0    = 0x{func0:08X}' if func0 else 'failed', flush=True)

    # Even if GPIO0 reads LOW, try the hold trick anyway
    print('\nTrying hold trick anyway (holding output HIGH through reset)...', flush=True)
    wreg(RTC_BASE + 0x00A8, 0x01)  # Hold GPIO0 in current config

    # Try WDT reset
    wreg(RTC_BASE + 0x00AC, 0x50D83AA1)
    wreg(RTC_BASE + 0x0098, (1 << 31) | (3 << 28))
    wreg(RTC_BASE + 0x009C, 5000)
    wreg(RTC_BASE + 0x00A0, 1)
    print('WDT armed anyway.', flush=True)

s.close()
print('DONE', flush=True)
