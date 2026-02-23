import serial, struct, time, sys

port = sys.argv[1] if len(sys.argv) > 1 else 'COM3'
E = 0xC0

def se(d):
    o = bytes([E])
    for b in d:
        if b == 0xC0: o += b'\xdb\xdc'
        elif b == 0xDB: o += b'\xdb\xdd'
        else: o += bytes([b])
    return o + bytes([E])

def mc(op, d): return se(struct.pack('<BBHI', 0, op, len(d), 0) + d)

s = serial.Serial(port, 115200, timeout=2)
time.sleep(0.3)
s.write(mc(0x08, b'\x07\x07\x12\x20' + b'\x55' * 32))
time.sleep(0.5)
s.read(2048)
print('SYNC')

def wr(a, v):
    s.write(mc(0x09, struct.pack('<II', a, v)))
    time.sleep(0.02)
    s.read(256)

def rr(a):
    s.write(mc(0x0A, struct.pack('<I', a)))
    time.sleep(0.02)
    r = s.read(256)
    r2 = r.replace(b'\xdb\xdc', bytes([0xC0])).replace(b'\xdb\xdd', bytes([0xDB]))
    for f in r2.split(bytes([E])):
        if len(f) >= 8 and f[0] == 1 and f[1] == 0x0A:
            return struct.unpack('<I', f[4:8])[0]
    return None

SM = 0x400  # GPIO10 SDA
CM = 0x800  # GPIO11 SCL

# Init GPIO for I2C
wr(0x6000400C, SM | CM)  # output = LOW
wr(0x60004028, SM | CM)  # disable output (float HIGH)
time.sleep(0.02)

v = rr(0x60004004)
if v:
    print(f'BUS SDA={(v>>10)&1} SCL={(v>>11)&1} GPIO0={(v)&1}')
else:
    print('READ FAIL')

def sl(): wr(0x60004024, SM)
def sh(): wr(0x60004028, SM)
def cl(): wr(0x60004024, CM)
def ch(): wr(0x60004028, CM)

def rs():
    v = rr(0x60004004)
    if v is None: return 1
    return (v >> 10) & 1

def start():
    sh(); ch(); time.sleep(0.002)
    sl(); time.sleep(0.002); cl()

def stop():
    sl(); ch(); time.sleep(0.002)
    sh(); time.sleep(0.002)

def wb(b):
    for i in range(7, -1, -1):
        if (b >> i) & 1: sh()
        else: sl()
        ch(); cl()
    sh(); ch()
    a = rs()
    cl()
    return a == 0

def axp_write(reg, val):
    start()
    a1 = wb(0x68)  # AXP2101 addr 0x34 write
    a2 = wb(reg)
    a3 = wb(val)
    stop()
    return a1 and a2 and a3

# Fix 1: Re-enable long press shutdown
print('FIX reg 0x22...')
ok = axp_write(0x22, 0x07)
print(f'  0x22: {"OK" if ok else "FAIL"}')

# Fix 2: Clear IRQ status
print('CLR IRQ...')
for r in [0x48, 0x49, 0x4A]:
    ok = axp_write(r, 0xFF)
    print(f'  0x{r:02X}: {"OK" if ok else "FAIL"}')

# Fix 3: Shutdown PMU
print('SHUTDOWN...')
ok = axp_write(0x10, 0x01)
print(f'  0x10: {"OK" if ok else "FAIL"}')

s.close()
print('DONE - watch should power off now')
