"""Turn on backlight directly from download mode via register writes.
GPIO45 = backlight pin. GPIO45 is in the high bank (32-47).
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

# GPIO45 is in the high bank (GPIOs 32-48)
# Bit position = 45 - 32 = 13
BL_BIT = 1 << 13  # 0x2000

# ESP32-S3 register addresses for GPIO high bank (32-48):
GPIO_OUT1_W1TS  = 0x60004014   # Set output data bits (high bank)
GPIO_OUT1_W1TC  = 0x60004018   # Clear output data bits (high bank)
GPIO_ENABLE1_W1TS = 0x60004030  # Enable output (high bank)
GPIO_ENABLE1_W1TC = 0x60004034  # Disable output (high bank)

# IO_MUX for GPIO45 = 0x60009004 + 45*4 = 0x600090B8
IO_MUX_GPIO45 = 0x600090B8

# GPIO_FUNC45_OUT_SEL = 0x60004554 + 45*4 = 0x60004608
GPIO_FUNC45_OUT_SEL = 0x60004608

# GPIO_PIN45_REG = 0x60004074 + 45*4 = 0x60004128 (but watch for RTC overlap)
# Actually: GPIO_PINn_REG base is 0x60004074
GPIO_PIN45 = 0x60004074 + 45 * 4  # 0x60004128

print('Configuring GPIO45 (backlight)...', flush=True)

# 1. IO_MUX: GPIO function (MCU_SEL=1), output, max drive
#    (1<<12) | (3<<10) | (1<<9) = 0x1E00
wreg(IO_MUX_GPIO45, 0x1E00)

# 2. GPIO_FUNC OUT_SEL: simple GPIO (256=0x100), OEN_SEL=1
wreg(GPIO_FUNC45_OUT_SEL, 0x500)

# 3. GPIO_PIN: push-pull (PAD_DRIVER=0)
wreg(GPIO_PIN45, 0x00)

# 4. Set output HIGH
wreg(GPIO_OUT1_W1TS, BL_BIT)

# 5. Enable output
wreg(GPIO_ENABLE1_W1TS, BL_BIT)

print('GPIO45 set HIGH - backlight should be ON now!', flush=True)
print('Keeping script alive for 30 seconds...', flush=True)

# Keep alive so user can observe
time.sleep(30)

s.close()
print('DONE', flush=True)
