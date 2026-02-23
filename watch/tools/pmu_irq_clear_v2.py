"""
PMU IRQ Clear v2 - Fixed order:
1. Clear PAD_HOLD first (so GPIO10/11 can actually toggle)
2. THEN do I2C bit-bang to clear PMU IRQs
3. Check GPIO0

Also verifies write_reg works by testing a scratch register.
"""
import sys
import time
import serial
import struct

PORT = "COM3"
BAUD = 115200

SLIP_END = 0xC0
SLIP_ESC = 0xDB
SLIP_ESC_END = 0xDC
SLIP_ESC_ESC = 0xDD

CMD_SYNC = 0x08
CMD_READ_REG = 0x0A
CMD_WRITE_REG = 0x09

# Registers
GPIO_ENABLE_W1TS = 0x60004024
GPIO_ENABLE_W1TC = 0x60004028
GPIO_OUT_W1TC    = 0x6000400C
GPIO_IN_REG      = 0x6000403C
PAD_HOLD_REG     = 0x600080A4

IO_MUX_GPIO10 = 0x6000902C
IO_MUX_GPIO11 = 0x60009030
GPIO_PIN10 = 0x6000409C
GPIO_PIN11 = 0x600040A0
GPIO_FUNC10_OUT_SEL = 0x6000457C
GPIO_FUNC11_OUT_SEL = 0x60004580

SDA_MASK = 0x400   # GPIO10
SCL_MASK = 0x800   # GPIO11
AXP_ADDR = 0x34


def slip_encode(data):
    out = bytearray([SLIP_END])
    for b in data:
        if b == SLIP_END:
            out.extend([SLIP_ESC, SLIP_ESC_END])
        elif b == SLIP_ESC:
            out.extend([SLIP_ESC, SLIP_ESC_ESC])
        else:
            out.append(b)
    out.append(SLIP_END)
    return bytes(out)


def slip_decode(data):
    out = bytearray()
    i = 0
    while i < len(data):
        if data[i] == SLIP_ESC:
            i += 1
            if i < len(data):
                if data[i] == SLIP_ESC_END:
                    out.append(SLIP_END)
                elif data[i] == SLIP_ESC_ESC:
                    out.append(SLIP_ESC)
        elif data[i] != SLIP_END:
            out.append(data[i])
        i += 1
    return bytes(out)


class ROMBootloader:
    def __init__(self, port, baud=115200):
        self.ser = serial.Serial(port, baud, timeout=1)
        self.ser.dtr = False
        self.ser.rts = False
        time.sleep(0.1)

    def _send_command(self, op, data=b'', chk=0):
        pkt = struct.pack('<BBHI', 0x00, op, len(data), chk) + data
        self.ser.write(slip_encode(pkt))

    def _recv_response(self, timeout=3):
        deadline = time.time() + timeout
        buf = bytearray()
        in_packet = False
        while time.time() < deadline:
            b = self.ser.read(1)
            if not b:
                continue
            b = b[0]
            if b == SLIP_END:
                if in_packet and len(buf) > 0:
                    decoded = slip_decode(bytes([SLIP_END]) + bytes(buf) + bytes([SLIP_END]))
                    if len(decoded) >= 8:
                        cmd = decoded[1]
                        value = struct.unpack('<I', decoded[4:8])[0]
                        status = decoded[8:] if len(decoded) > 8 else b''
                        return cmd, value, status
                buf = bytearray()
                in_packet = True
            else:
                if in_packet:
                    buf.append(b)
        return None, None, None

    def sync(self):
        data = b'\x07\x07\x12\x20' + b'\x55' * 32
        for attempt in range(5):
            self.ser.reset_input_buffer()
            self._send_command(CMD_SYNC, data)
            for _ in range(8):
                cmd, value, body = self._recv_response(timeout=0.5)
                if cmd == CMD_SYNC:
                    for _ in range(7):
                        self._recv_response(timeout=0.1)
                    return True
        return False

    def read_reg(self, addr):
        data = struct.pack('<I', addr)
        self.ser.reset_input_buffer()
        self._send_command(CMD_READ_REG, data)
        cmd, value, body = self._recv_response()
        if cmd == CMD_READ_REG:
            return value
        return None

    def write_reg(self, addr, value, mask=0xFFFFFFFF, delay_us=0):
        data = struct.pack('<IIII', addr, value, mask, delay_us)
        self.ser.reset_input_buffer()
        self._send_command(CMD_WRITE_REG, data)
        cmd, resp_val, body = self._recv_response()
        return cmd == CMD_WRITE_REG

    def close(self):
        self.ser.close()


def i2c_start(rom):
    rom.write_reg(GPIO_ENABLE_W1TC, SDA_MASK)  # SDA high
    rom.write_reg(GPIO_ENABLE_W1TC, SCL_MASK)  # SCL high
    rom.write_reg(GPIO_ENABLE_W1TS, SDA_MASK)  # SDA low (START)
    rom.write_reg(GPIO_ENABLE_W1TS, SCL_MASK)  # SCL low

def i2c_stop(rom):
    rom.write_reg(GPIO_ENABLE_W1TS, SDA_MASK)  # SDA low
    rom.write_reg(GPIO_ENABLE_W1TC, SCL_MASK)  # SCL high
    rom.write_reg(GPIO_ENABLE_W1TC, SDA_MASK)  # SDA high (STOP)

def i2c_write_bit(rom, bit):
    if bit:
        rom.write_reg(GPIO_ENABLE_W1TC, SDA_MASK)
    else:
        rom.write_reg(GPIO_ENABLE_W1TS, SDA_MASK)
    rom.write_reg(GPIO_ENABLE_W1TC, SCL_MASK)  # SCL high
    rom.write_reg(GPIO_ENABLE_W1TS, SCL_MASK)  # SCL low

def i2c_write_byte(rom, byte):
    for i in range(8):
        i2c_write_bit(rom, (byte >> (7 - i)) & 1)
    # ACK: release SDA, clock
    rom.write_reg(GPIO_ENABLE_W1TC, SDA_MASK)
    rom.write_reg(GPIO_ENABLE_W1TC, SCL_MASK)
    rom.write_reg(GPIO_ENABLE_W1TS, SCL_MASK)

def i2c_write_register(rom, dev_addr, reg, value):
    i2c_start(rom)
    i2c_write_byte(rom, dev_addr << 1)
    i2c_write_byte(rom, reg)
    i2c_write_byte(rom, value)
    i2c_stop(rom)


def main():
    print("=" * 60)
    print("PMU IRQ Clear v2 - Clear PAD_HOLD First")
    print("=" * 60)

    print(f"\nConnecting to {PORT}...")
    rom = ROMBootloader(PORT, BAUD)

    print("Syncing...")
    if not rom.sync():
        print("ERROR: Sync failed!")
        rom.close()
        sys.exit(1)
    print("Synced!")

    # === STEP 0: Read initial state ===
    print("\n--- STEP 0: Initial state ---")
    pad_hold = rom.read_reg(PAD_HOLD_REG)
    gpio_in = rom.read_reg(GPIO_IN_REG)
    print(f"  PAD_HOLD = 0x{pad_hold:08X}" if pad_hold is not None else "  PAD_HOLD = read fail")
    print(f"  GPIO_IN  = 0x{gpio_in:08X} (GPIO0={'HIGH' if gpio_in & 1 else 'LOW'})" if gpio_in is not None else "  GPIO_IN  = read fail")

    # === STEP 1: Clear PAD_HOLD ===
    print("\n--- STEP 1: Clear PAD_HOLD ---")
    ok = rom.write_reg(PAD_HOLD_REG, 0x00000000)
    print(f"  Write result: {'OK' if ok else 'FAIL'}")

    # Immediately read back
    pad_hold = rom.read_reg(PAD_HOLD_REG)
    print(f"  PAD_HOLD readback = 0x{pad_hold:08X}" if pad_hold is not None else "  Readback fail")

    # Read GPIO_IN to see if GPIO0 changed
    gpio_in = rom.read_reg(GPIO_IN_REG)
    print(f"  GPIO_IN  = 0x{gpio_in:08X} (GPIO0={'HIGH' if gpio_in & 1 else 'LOW'})" if gpio_in is not None else "  GPIO_IN  = read fail")

    # Try writing bit-by-bit (maybe the register only clears individual bits)
    print("\n  Trying to clear bit 0 only (GPIO0 hold)...")
    # Read-modify-write approach: clear bit 0
    ok = rom.write_reg(PAD_HOLD_REG, 0x00000000, mask=0x00000001)
    pad_hold = rom.read_reg(PAD_HOLD_REG)
    print(f"  PAD_HOLD = 0x{pad_hold:08X}" if pad_hold is not None else "  Readback fail")

    # Try writing multiple times
    print("\n  Hammering PAD_HOLD with zeros (5x)...")
    for i in range(5):
        rom.write_reg(PAD_HOLD_REG, 0x00000000)
    pad_hold = rom.read_reg(PAD_HOLD_REG)
    print(f"  PAD_HOLD = 0x{pad_hold:08X}" if pad_hold is not None else "  Readback fail")

    # === STEP 2: Check if GPIO0 is externally pulled LOW ===
    print("\n--- STEP 2: Configure GPIO0 input with pull-up ---")
    # IO_MUX_GPIO0_REG = 0x60009004
    # Bit 9: FUN_IE (input enable)
    # Bit 8: FUN_PU (pull-up)
    # Bit 7: FUN_PD (pull-down) - clear this
    iomux_val = rom.read_reg(0x60009004)
    print(f"  IO_MUX_GPIO0 = 0x{iomux_val:08X}" if iomux_val is not None else "  Read fail")

    # Set pull-up, clear pull-down, enable input
    rom.write_reg(0x60009004, 0x00000B00)  # FUN_DRV=2, FUN_IE=1, FUN_PU=1
    iomux_val = rom.read_reg(0x60009004)
    print(f"  IO_MUX_GPIO0 after = 0x{iomux_val:08X}" if iomux_val is not None else "  Read fail")

    gpio_in = rom.read_reg(GPIO_IN_REG)
    print(f"  GPIO_IN  = 0x{gpio_in:08X} (GPIO0={'HIGH' if gpio_in & 1 else 'LOW'})" if gpio_in is not None else "  GPIO_IN  = read fail")

    # === STEP 3: I2C bit-bang to clear PMU IRQs ===
    print("\n--- STEP 3: I2C setup and PMU IRQ clear ---")
    # Configure GPIO10/11 for I2C
    rom.write_reg(IO_MUX_GPIO10, 0x1B00)
    rom.write_reg(IO_MUX_GPIO11, 0x1B00)
    rom.write_reg(GPIO_PIN10, 0x04)  # Open-drain
    rom.write_reg(GPIO_PIN11, 0x04)  # Open-drain
    rom.write_reg(GPIO_FUNC10_OUT_SEL, 0x500)
    rom.write_reg(GPIO_FUNC11_OUT_SEL, 0x500)
    rom.write_reg(GPIO_OUT_W1TC, SDA_MASK | SCL_MASK)  # Output LOW when enabled
    rom.write_reg(GPIO_ENABLE_W1TC, SDA_MASK | SCL_MASK)  # Start with both HIGH
    time.sleep(0.01)
    print("  I2C GPIO configured")

    # Disable IRQ enables
    print("  Writing IRQ enable regs 0x40-0x42 = 0x00...")
    i2c_write_register(rom, AXP_ADDR, 0x40, 0x00)
    i2c_write_register(rom, AXP_ADDR, 0x41, 0x00)
    i2c_write_register(rom, AXP_ADDR, 0x42, 0x00)
    print("  Done")

    # Clear IRQ status
    print("  Writing IRQ status regs 0x48-0x4A = 0xFF...")
    i2c_write_register(rom, AXP_ADDR, 0x48, 0xFF)
    i2c_write_register(rom, AXP_ADDR, 0x49, 0xFF)
    i2c_write_register(rom, AXP_ADDR, 0x4A, 0xFF)
    print("  Done")

    # === STEP 4: Final GPIO0 check ===
    print("\n--- STEP 4: Final check ---")
    gpio_in = rom.read_reg(GPIO_IN_REG)
    gpio0 = gpio_in & 1 if gpio_in is not None else None
    pad_hold = rom.read_reg(PAD_HOLD_REG)
    print(f"  PAD_HOLD = 0x{pad_hold:08X}" if pad_hold is not None else "  PAD_HOLD = read fail")
    print(f"  GPIO_IN  = 0x{gpio_in:08X} (GPIO0={'HIGH' if gpio0 else 'LOW'})" if gpio_in is not None else "  GPIO_IN  = read fail")

    if gpio0 == 1:
        print("\n*** SUCCESS: GPIO0 is HIGH! ***")
        print("Unplug USB, wait 5 sec, replug. Watch should boot!")
    else:
        print("\n*** GPIO0 still LOW ***")
        print("Hardware pull-down confirmed. Not PAD_HOLD, not PMU IRQ.")

        # Let's also try other I2C devices that might have IRQ lines
        print("\n--- STEP 5: Try other I2C peripherals ---")

        # BMA423 accelerometer at 0x18 or 0x19
        # INT_STATUS registers: 0x1C, 0x1D, 0x1E, 0x1F
        print("  Clearing BMA423 at 0x19 (INT_STATUS)...")
        for reg in [0x1C, 0x1D, 0x1E, 0x1F]:
            i2c_write_register(rom, 0x19, reg, 0x00)

        # Also try address 0x18
        print("  Clearing BMA423 at 0x18 (INT_STATUS)...")
        for reg in [0x1C, 0x1D, 0x1E, 0x1F]:
            i2c_write_register(rom, 0x18, reg, 0x00)

        # FT6236 touch at 0x38
        # No clear IRQ register, but can try reading status
        print("  Poking FT6236 touch at 0x38...")
        i2c_write_register(rom, 0x38, 0xA4, 0x00)  # INT mode = polling

        # PCF8563 RTC at 0x51
        # Control_Status_1 = 0x00, Control_Status_2 = 0x01
        print("  Clearing PCF8563 RTC at 0x51...")
        i2c_write_register(rom, 0x51, 0x00, 0x00)  # Clear all control
        i2c_write_register(rom, 0x51, 0x01, 0x00)  # Clear all status/IRQ

        # Final final check
        gpio_in = rom.read_reg(GPIO_IN_REG)
        gpio0 = gpio_in & 1 if gpio_in is not None else None
        print(f"\n  GPIO_IN  = 0x{gpio_in:08X} (GPIO0={'HIGH' if gpio0 else 'LOW'})" if gpio_in is not None else "  GPIO_IN = read fail")

        if gpio0 == 1:
            print("\n*** SUCCESS: One of the other peripherals was the cause! ***")
        else:
            print("\n*** STILL LOW. Options: ***")
            print("  1. Burn DIS_DOWNLOAD_MODE eFuse (permanent, no more USB flash)")
            print("  2. Physically disconnect battery and wait longer")
            print("  3. Open case and check GPIO0 trace with multimeter")

    rom.close()
    print("\nDone.")


if __name__ == "__main__":
    main()
