"""Check and clear RTC_CNTL_OPTION1_REG FORCE_DOWNLOAD_BOOT flag."""
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

# RTC_CNTL registers for ESP32-S3
RTC_CNTL_BASE = 0x60008000
OPTION1_REG = RTC_CNTL_BASE + 0x0128  # 0x60008128

# Read OPTION1_REG
val = read_reg(OPTION1_REG)
print(f'RTC_CNTL_OPTION1_REG (0x60008128) = 0x{val:08X}' if val is not None else 'FAILED to read', flush=True)
if val is not None:
    force_dl = val & 1
    print(f'  FORCE_DOWNLOAD_BOOT (bit 0) = {force_dl} ({"SET! THIS IS THE PROBLEM!" if force_dl else "not set"})', flush=True)

# Also check RTC_CNTL_RESET_STATE_REG for reset reason
RESET_STATE_REG = RTC_CNTL_BASE + 0x0038  # 0x60008038
val = read_reg(RESET_STATE_REG)
if val is not None:
    print(f'RTC_CNTL_RESET_STATE_REG (0x60008038) = 0x{val:08X}', flush=True)

# Read a few more RTC registers that might be relevant
for name, offset in [
    ('WDTCONFIG0', 0x0098),
    ('SWD_CONF', 0x00B0),
    ('SWD_WPROTECT', 0x00B4),
    ('DIG_PAD_HOLD', 0x00A8),  # GPIO hold register
    ('PAD_HOLD', 0x00A4),      # RTC pad hold
]:
    addr = RTC_CNTL_BASE + offset
    val = read_reg(addr)
    if val is not None:
        print(f'{name} (0x{addr:08X}) = 0x{val:08X}', flush=True)

# GPIO_STRAP_REG
val = read_reg(0x60004038)
if val is not None:
    print(f'GPIO_STRAP_REG (0x60004038) = 0x{val:08X}', flush=True)
    print(f'  GPIO0 at reset = {val & 1}', flush=True)

# Current GPIO0
val = read_reg(0x6000403C)
if val is not None:
    print(f'GPIO_IN_REG = 0x{val:08X}  GPIO0={val&1}', flush=True)

# ============================================================
# FIX: Clear FORCE_DOWNLOAD_BOOT if set
# ============================================================
val = read_reg(OPTION1_REG)
if val is not None and (val & 1):
    print('\n*** CLEARING FORCE_DOWNLOAD_BOOT ***', flush=True)
    wreg(OPTION1_REG, val & ~1)  # Clear bit 0
    # Verify
    val2 = read_reg(OPTION1_REG)
    print(f'After clear: OPTION1_REG = 0x{val2:08X}' if val2 is not None else 'verify failed', flush=True)
    print(f'  FORCE_DOWNLOAD_BOOT = {val2 & 1}' if val2 is not None else '', flush=True)

# Also clear DIG_PAD_HOLD if any bits set (could be holding GPIO0 state)
val = read_reg(RTC_CNTL_BASE + 0x00A8)
if val is not None and val != 0:
    print(f'\nClearing DIG_PAD_HOLD (was 0x{val:08X})...', flush=True)
    wreg(RTC_CNTL_BASE + 0x00A8, 0)
    val2 = read_reg(RTC_CNTL_BASE + 0x00A8)
    print(f'After clear: DIG_PAD_HOLD = 0x{val2:08X}' if val2 is not None else 'verify failed', flush=True)

val = read_reg(RTC_CNTL_BASE + 0x00A4)
if val is not None and val != 0:
    print(f'\nClearing PAD_HOLD (was 0x{val:08X})...', flush=True)
    wreg(RTC_CNTL_BASE + 0x00A4, 0)

s.close()
print('\nDONE - try watchdog_reset now', flush=True)
