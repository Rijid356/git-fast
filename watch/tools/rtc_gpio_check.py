"""Check ALL possible register spaces that could drive GPIO0 LOW."""
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

# ============================================================
# 1. USB_SERIAL_JTAG registers - could it be driving GPIO0?
# ============================================================
print('=== USB_SERIAL_JTAG (0x60038000) ===', flush=True)
USB_BASE = 0x60038000
for name, offset in [
    ('CONF0', 0x00), ('TEST', 0x04), ('MISC_CONF', 0x08),
    ('MEM_CONF', 0x0C), ('EP1_CONF', 0x18),
]:
    val = read_reg(USB_BASE + offset)
    if val is not None:
        print(f'  {name} (0x{USB_BASE+offset:08X}) = 0x{val:08X}', flush=True)

# ============================================================
# 2. RTC_CNTL registers - full dump of relevant ones
# ============================================================
print('\n=== RTC_CNTL (0x60008000) ===', flush=True)
RTC_BASE = 0x60008000
for name, offset in [
    ('OPTIONS0', 0x00), ('SLP_TIMER0', 0x04), ('SLP_TIMER1', 0x08),
    ('TIME_UPDATE', 0x0C), ('SLP_VAL', 0x10),
    ('TIMER1', 0x18), ('TIMER2', 0x1C), ('TIMER5', 0x28), ('TIMER6', 0x2C),
    ('STATE0', 0x30), ('TIMER3', 0x34), ('RESET_STATE', 0x38),
    ('WAKEUP_STATE', 0x3C), ('INT_RAW', 0x44), ('INT_ST', 0x48),
    ('INT_CLR', 0x50),
    ('STORE0', 0x54), ('STORE1', 0x58), ('STORE2', 0x5C), ('STORE3', 0x60),
    ('GPIO_OUT', 0x74), ('GPIO_IN', 0x7C),
    ('GPIO_STATUS', 0x80), ('GPIO_ENABLE', 0x84),
    ('PAD_HOLD', 0xA4), ('DIG_PAD_HOLD', 0xA8),
    ('BROWN_OUT', 0xBC),
    ('OPTION1', 0x128),
]:
    val = read_reg(RTC_BASE + offset)
    if val is not None:
        print(f'  {name} (0x{RTC_BASE+offset:08X}) = 0x{val:08X}', flush=True)

# ============================================================
# 3. RTCIO / LP_IO registers
# ============================================================
print('\n=== RTCIO (0x60008400+) ===', flush=True)
# ESP32-S3 RTCIO_RTC_PAD registers start at different offsets
# Try scanning for non-zero values around known RTCIO area
RTCIO_BASE = 0x60008400
for i in range(0, 0x100, 4):
    val = read_reg(RTCIO_BASE + i)
    if val is not None and val != 0:
        print(f'  0x{RTCIO_BASE+i:08X} = 0x{val:08X}', flush=True)

# Also try LP_IO at 0x600B2000
print('\n=== LP_IO (0x600B2000) ===', flush=True)
LP_IO_BASE = 0x600B2000
for i in range(0, 0x80, 4):
    val = read_reg(LP_IO_BASE + i)
    if val is not None and val != 0:
        print(f'  0x{LP_IO_BASE+i:08X} = 0x{val:08X}', flush=True)

# Also try LP_AON at 0x600B1000
print('\n=== LP_AON (0x600B1000) ===', flush=True)
LP_AON_BASE = 0x600B1000
for i in range(0, 0x40, 4):
    val = read_reg(LP_AON_BASE + i)
    if val is not None and val != 0:
        print(f'  0x{LP_AON_BASE+i:08X} = 0x{val:08X}', flush=True)

# ============================================================
# 4. Check RTC_GPIO specifically for GPIO0
# ============================================================
print('\n=== RTC GPIO0 specific ===', flush=True)
# RTC_CNTL_GPIO_OUT: RTC GPIO output data
val = read_reg(RTC_BASE + 0x74)
print(f'  RTC_GPIO_OUT = 0x{val:08X}, bit0={val&1}' if val is not None else '  failed', flush=True)

# RTC_CNTL_GPIO_ENABLE: RTC GPIO output enable
val = read_reg(RTC_BASE + 0x84)
print(f'  RTC_GPIO_ENABLE = 0x{val:08X}, bit0={val&1}' if val is not None else '  failed', flush=True)

# If RTC GPIO0 output is enabled, try disabling it
rtc_gpio_en = read_reg(RTC_BASE + 0x84)
if rtc_gpio_en is not None and (rtc_gpio_en & 1):
    print('\n*** RTC GPIO0 output is ENABLED! Disabling... ***', flush=True)
    wreg(RTC_BASE + 0x84, rtc_gpio_en & ~1)  # Clear bit 0
    time.sleep(0.1)
    # Check GPIO0 after
    val = read_reg(0x6000403C)
    print(f'  GPIO0 after RTC disable = {val & 1 if val else "?"}', flush=True)

# ============================================================
# 5. Try clearing ALL RTC GPIO output/enable
# ============================================================
print('\nClearing RTC GPIO output and enable...', flush=True)
wreg(RTC_BASE + 0x74, 0)  # Clear RTC GPIO output data
wreg(RTC_BASE + 0x84, 0)  # Clear RTC GPIO output enable
time.sleep(0.1)
val = read_reg(0x6000403C)
print(f'GPIO0 after RTC clear = {val & 1 if val else "?"}', flush=True)

s.close()
print('\nDONE', flush=True)
