"""Read GPIO0 and I2C pin states."""
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
s = serial.Serial(port, 115200, timeout=1)
time.sleep(0.3)

# Sync
s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
time.sleep(0.5)
s.read(4096)

# Second sync to be sure
s.write(make_cmd(0x08, b'\x07\x07\x12\x20' + (b'\x55' * 32)))
time.sleep(0.3)
s.read(4096)
print('SYNC OK', flush=True)

def read_reg(addr):
    """Read a 32-bit register using READ_REG command (0x0A)."""
    cmd = make_cmd(0x0A, struct.pack('<I', addr))
    s.write(cmd)
    time.sleep(0.1)
    raw = s.read(4096)
    decoded = slip_decode(raw)
    if len(decoded) >= 10:
        # Response: direction(1) + command(1) + size(2) + value(4) + status(1) + error(1)
        val = struct.unpack_from('<I', decoded, 4)[0]
        return val
    return None

# Read GPIO_IN_REG (current pin states 0-31)
val = read_reg(0x6000403C)
if val is not None:
    gpio0 = (val >> 0) & 1
    gpio10 = (val >> 10) & 1
    gpio11 = (val >> 11) & 1
    print(f'GPIO_IN_REG = 0x{val:08X}', flush=True)
    print(f'  GPIO0  (boot strap) = {gpio0} ({"LOW=download" if gpio0==0 else "HIGH=normal boot"})', flush=True)
    print(f'  GPIO10 (I2C SDA)    = {gpio10}', flush=True)
    print(f'  GPIO11 (I2C SCL)    = {gpio11}', flush=True)

# Read IO_MUX for GPIO10 and GPIO11
for pin, name, addr in [(10, 'SDA', 0x6000902C), (11, 'SCL', 0x60009030)]:
    val = read_reg(addr)
    if val is not None:
        mcu_sel = (val >> 12) & 7
        fun_ie = (val >> 9) & 1
        fun_wpu = (val >> 8) & 1
        fun_wpd = (val >> 7) & 1
        fun_drv = (val >> 10) & 3
        print(f'IO_MUX_GPIO{pin} ({name}) = 0x{val:08X}  MCU_SEL={mcu_sel} IE={fun_ie} WPU={fun_wpu} WPD={fun_wpd} DRV={fun_drv}', flush=True)

# Read GPIO_ENABLE_REG
val = read_reg(0x60004020)
if val is not None:
    print(f'GPIO_ENABLE_REG = 0x{val:08X}', flush=True)
    print(f'  GPIO10 output enabled = {(val>>10)&1}', flush=True)
    print(f'  GPIO11 output enabled = {(val>>11)&1}', flush=True)

# Read GPIO_OUT_REG
val = read_reg(0x60004004)
if val is not None:
    print(f'GPIO_OUT_REG = 0x{val:08X}', flush=True)

# Read GPIO_FUNCn_OUT_SEL for GPIO10/11
for pin, addr in [(10, 0x6000457C), (11, 0x60004580)]:
    val = read_reg(addr)
    if val is not None:
        out_sel = val & 0x1FF
        oen_sel = (val >> 10) & 1
        print(f'GPIO_FUNC{pin}_OUT_SEL = 0x{val:08X}  OUT_SEL={out_sel} OEN_SEL={oen_sel}', flush=True)

# Read GPIO_PIN10/11 for pad_driver (open-drain) config
for pin, addr in [(10, 0x6000409C), (11, 0x600040A0)]:
    val = read_reg(addr)
    if val is not None:
        pad_driver = (val >> 2) & 1
        print(f'GPIO_PIN{pin}_REG = 0x{val:08X}  PAD_DRIVER={pad_driver} ({"open-drain" if pad_driver else "push-pull"})', flush=True)

s.close()
print('DONE', flush=True)
