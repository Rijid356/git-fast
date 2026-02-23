"""Clear hold, verify GPIO0, then trigger watchdog reset in one script."""
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
    time.sleep(0.01)
    s.read(4096)

def read_reg(addr):
    s.write(make_cmd(0x0A, struct.pack('<I', addr)))
    time.sleep(0.05)
    raw = s.read(4096)
    decoded = slip_decode(raw)
    if len(decoded) >= 10:
        return struct.unpack_from('<I', decoded, 4)[0]
    return None

RTC_BASE = 0x60008000

# Check current state of hold registers
dig_hold = read_reg(RTC_BASE + 0x00A8)
pad_hold = read_reg(RTC_BASE + 0x00A4)
print(f'DIG_PAD_HOLD = 0x{dig_hold:08X}' if dig_hold is not None else 'failed', flush=True)
print(f'PAD_HOLD = 0x{pad_hold:08X}' if pad_hold is not None else 'failed', flush=True)

# Check GPIO0 current state
gpio_in = read_reg(0x6000403C)
print(f'GPIO_IN_REG = 0x{gpio_in:08X}, GPIO0 = {gpio_in & 1}' if gpio_in else 'failed', flush=True)

# Clear hold registers
print('\nClearing holds...', flush=True)
wreg(RTC_BASE + 0x00A8, 0)  # DIG_PAD_HOLD
wreg(RTC_BASE + 0x00A4, 0)  # PAD_HOLD

# Set strong pull-up on GPIO0 via IO_MUX
# MCU_SEL=1 (GPIO), FUN_IE=1, FUN_WPU=1, FUN_DRV=3 (strongest)
# = (1<<12) | (3<<10) | (1<<9) | (1<<8) = 0x1F00
wreg(0x60009004, 0x1F00)

# Make sure GPIO0 is NOT configured as output
# GPIO_ENABLE_W1TC: clear bit 0
wreg(0x60004028, 0x01)

# Wait and check GPIO0 again
time.sleep(0.1)
gpio_in = read_reg(0x6000403C)
print(f'GPIO0 after hold clear + pull-up = {gpio_in & 1 if gpio_in else "?"}', flush=True)

# Read IO_MUX_GPIO0 to verify
iomux0 = read_reg(0x60009004)
print(f'IO_MUX_GPIO0 = 0x{iomux0:08X}' if iomux0 else 'failed', flush=True)

# Read GPIO_ENABLE to make sure GPIO0 not output
gpio_en = read_reg(0x60004020)
print(f'GPIO_ENABLE = 0x{gpio_en:08X}, GPIO0 out = {gpio_en & 1 if gpio_en else "?"}', flush=True)

# Check GPIO_STRAP
strap = read_reg(0x60004038)
print(f'GPIO_STRAP = 0x{strap:08X}' if strap else 'failed', flush=True)

# Now trigger RTC watchdog reset directly (don't rely on esptool)
# Program RTC_WDT to fire in ~100ms
print('\n=== Triggering RTC watchdog reset ===', flush=True)

# First, unlock RTC WDT write protect
# RTC_CNTL_WDTWPROTECT_REG = RTC_BASE + 0x00AC
wreg(RTC_BASE + 0x00AC, 0x50D83AA1)  # Magic unlock key

# Configure RTC WDT
# RTC_CNTL_WDTCONFIG0_REG = RTC_BASE + 0x0098
# Enable WDT, set stage0 to system reset (3), pause in sleep
# WDT_EN (bit 31) | WDT_STG0 (bits 29:28) = 3 (reset) | WDT_FLASHBOOT_MOD_EN=0
wreg(RTC_BASE + 0x0098, (1 << 31) | (3 << 28))

# RTC_CNTL_WDTCONFIG1_REG = timeout in slow clock cycles
# ~100ms = 100000us, slow clock ~150kHz = 15000 cycles
wreg(RTC_BASE + 0x009C, 15000)

# Feed the WDT to start countdown
# RTC_CNTL_WDTFEED_REG = RTC_BASE + 0x00A0
wreg(RTC_BASE + 0x00A0, 1)

print('WDT armed - reset in ~100ms', flush=True)
print('Device should reboot from flash if GPIO0 is HIGH', flush=True)

s.close()
print('DONE', flush=True)
