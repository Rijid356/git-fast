"""
Clear AXP2101 PMU IRQs via I2C bit-bang from ESP32-S3 download mode.
Uses esptool's ROM bootloader write_reg/read_reg for fast register access.

GPIO0 is likely connected to PMU N_IRQ and being held LOW by asserted interrupts,
forcing the ESP32-S3 into download mode. This script clears those IRQs.
"""
import sys
import time
import serial
import struct

PORT = "COM3"
BAUD = 115200

# SLIP framing
SLIP_END = 0xC0
SLIP_ESC = 0xDB
SLIP_ESC_END = 0xDC
SLIP_ESC_ESC = 0xDD

# ROM bootloader commands
CMD_SYNC = 0x08
CMD_READ_REG = 0x0A
CMD_WRITE_REG = 0x09

# GPIO registers
GPIO_ENABLE_W1TS = 0x60004024
GPIO_ENABLE_W1TC = 0x60004028
GPIO_OUT_W1TC    = 0x6000400C
GPIO_IN_REG      = 0x6000403C
PAD_HOLD_REG     = 0x600080A4

# IO_MUX registers
IO_MUX_GPIO10 = 0x6000902C
IO_MUX_GPIO11 = 0x60009030

# GPIO_PIN registers (for open-drain)
GPIO_PIN10 = 0x6000409C
GPIO_PIN11 = 0x600040A0

# GPIO_FUNC_OUT_SEL registers
GPIO_FUNC10_OUT_SEL = 0x6000457C
GPIO_FUNC11_OUT_SEL = 0x60004580

# Masks
SDA_MASK = 0x400   # GPIO10
SCL_MASK = 0x800   # GPIO11

# AXP2101
AXP_ADDR = 0x34


def slip_encode(data):
    """SLIP encode a packet"""
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
    """SLIP decode a packet"""
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


def checksum(data):
    """XOR checksum of data"""
    cs = 0xEF
    for b in data:
        cs ^= b
    return cs


class ROMBootloader:
    def __init__(self, port, baud=115200):
        self.ser = serial.Serial(port, baud, timeout=1)
        self.ser.dtr = False
        self.ser.rts = False
        time.sleep(0.1)

    def _send_command(self, op, data=b'', chk=0):
        """Send a command to the bootloader"""
        pkt = struct.pack('<BBHI', 0x00, op, len(data), chk) + data
        self.ser.write(slip_encode(pkt))

    def _recv_response(self, timeout=3):
        """Receive a SLIP-framed response"""
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
                        direction = decoded[0]
                        cmd = decoded[1]
                        size = struct.unpack('<H', decoded[2:4])[0]
                        value = struct.unpack('<I', decoded[4:8])[0]
                        body = decoded[8:]
                        return cmd, value, body
                buf = bytearray()
                in_packet = True
            else:
                if in_packet:
                    buf.append(b)

        return None, None, None

    def sync(self):
        """Synchronize with the bootloader"""
        # Sync packet: 0x07 0x07 0x12 0x20 + 32 x 0x55
        data = b'\x07\x07\x12\x20' + b'\x55' * 32
        for attempt in range(5):
            self.ser.reset_input_buffer()
            self._send_command(CMD_SYNC, data)
            # Read multiple responses (sync sends back several)
            for _ in range(8):
                cmd, value, body = self._recv_response(timeout=0.5)
                if cmd == CMD_SYNC:
                    # Drain remaining sync responses
                    for _ in range(7):
                        self._recv_response(timeout=0.1)
                    return True
        return False

    def read_reg(self, addr):
        """Read a 32-bit register"""
        data = struct.pack('<I', addr)
        self.ser.reset_input_buffer()
        self._send_command(CMD_READ_REG, data)
        cmd, value, body = self._recv_response()
        if cmd == CMD_READ_REG:
            return value
        return None

    def write_reg(self, addr, value, mask=0xFFFFFFFF, delay_us=0):
        """Write a 32-bit register"""
        data = struct.pack('<IIII', addr, value, mask, delay_us)
        self._send_command(CMD_WRITE_REG, data)
        cmd, resp_val, body = self._recv_response()
        return cmd == CMD_WRITE_REG

    def close(self):
        self.ser.close()


class I2CBitBang:
    def __init__(self, rom):
        self.rom = rom
        self._write_count = 0

    def init(self):
        """Configure GPIO10 (SDA) and GPIO11 (SCL) for I2C"""
        r = self.rom
        # IO_MUX: GPIO function, input enable, pull-up
        r.write_reg(IO_MUX_GPIO10, 0x1B00)
        r.write_reg(IO_MUX_GPIO11, 0x1B00)
        # Open-drain mode
        r.write_reg(GPIO_PIN10, 0x04)
        r.write_reg(GPIO_PIN11, 0x04)
        # GPIO_FUNC_OUT_SEL: simple GPIO output
        r.write_reg(GPIO_FUNC10_OUT_SEL, 0x500)
        r.write_reg(GPIO_FUNC11_OUT_SEL, 0x500)
        # Clear output values (output LOW when enabled)
        r.write_reg(GPIO_OUT_W1TC, SDA_MASK | SCL_MASK)
        # Both lines HIGH (output disabled = pulled up)
        r.write_reg(GPIO_ENABLE_W1TC, SDA_MASK | SCL_MASK)
        time.sleep(0.01)

    def _sda_low(self):
        self.rom.write_reg(GPIO_ENABLE_W1TS, SDA_MASK)
        self._write_count += 1

    def _sda_high(self):
        self.rom.write_reg(GPIO_ENABLE_W1TC, SDA_MASK)
        self._write_count += 1

    def _scl_low(self):
        self.rom.write_reg(GPIO_ENABLE_W1TS, SCL_MASK)
        self._write_count += 1

    def _scl_high(self):
        self.rom.write_reg(GPIO_ENABLE_W1TC, SCL_MASK)
        self._write_count += 1

    def start(self):
        self._sda_high()
        self._scl_high()
        self._sda_low()   # SDA LOW while SCL HIGH = START
        self._scl_low()

    def stop(self):
        self._sda_low()
        self._scl_high()
        self._sda_high()  # SDA HIGH while SCL HIGH = STOP

    def write_bit(self, bit):
        if bit:
            self._sda_high()
        else:
            self._sda_low()
        self._scl_high()
        self._scl_low()

    def read_ack(self):
        """Release SDA, clock ACK bit (assume ACK, can't reliably read)"""
        self._sda_high()   # Release SDA
        self._scl_high()   # Clock the ACK bit
        self._scl_low()

    def write_byte(self, byte):
        for i in range(8):
            self.write_bit((byte >> (7 - i)) & 1)
        self.read_ack()

    def write_register(self, dev_addr, reg, value):
        """Write a single register via I2C"""
        self.start()
        self.write_byte(dev_addr << 1)   # Address + write bit
        self.write_byte(reg)
        self.write_byte(value)
        self.stop()


def main():
    print("=" * 60)
    print("AXP2101 PMU IRQ Clear via I2C Bit-Bang")
    print("=" * 60)

    print(f"\nConnecting to {PORT}...")
    rom = ROMBootloader(PORT, BAUD)

    print("Syncing with ROM bootloader...")
    if not rom.sync():
        print("ERROR: Failed to sync with bootloader!")
        print("Make sure the device is in download mode and COM3 is available.")
        rom.close()
        sys.exit(1)
    print("Synced!")

    # Read initial state
    pad_hold = rom.read_reg(PAD_HOLD_REG)
    gpio_in = rom.read_reg(GPIO_IN_REG)
    gpio0 = gpio_in & 1 if gpio_in else "?"
    print(f"\nInitial state:")
    print(f"  PAD_HOLD  = 0x{pad_hold:08X}" if pad_hold is not None else "  PAD_HOLD  = read failed")
    print(f"  GPIO_IN   = 0x{gpio_in:08X} (GPIO0 = {gpio0})" if gpio_in is not None else "  GPIO_IN   = read failed")

    # Initialize I2C
    print("\nInitializing I2C bit-bang on GPIO10(SDA)/GPIO11(SCL)...")
    i2c = I2CBitBang(rom)
    i2c.init()

    # Disable all PMU IRQ sources
    print("\nDisabling PMU IRQ enables (regs 0x40-0x42)...")
    i2c.write_register(AXP_ADDR, 0x40, 0x00)
    print("  0x40 = 0x00 (done)")
    i2c.write_register(AXP_ADDR, 0x41, 0x00)
    print("  0x41 = 0x00 (done)")
    i2c.write_register(AXP_ADDR, 0x42, 0x00)
    print("  0x42 = 0x00 (done)")

    # Clear all pending IRQs
    print("\nClearing PMU IRQ status (regs 0x48-0x4A)...")
    i2c.write_register(AXP_ADDR, 0x48, 0xFF)
    print("  0x48 = 0xFF (done)")
    i2c.write_register(AXP_ADDR, 0x49, 0xFF)
    print("  0x49 = 0xFF (done)")
    i2c.write_register(AXP_ADDR, 0x4A, 0xFF)
    print("  0x4A = 0xFF (done)")

    print(f"\n  Total I2C register writes: {i2c._write_count}")

    # Check GPIO0 now
    gpio_in = rom.read_reg(GPIO_IN_REG)
    gpio0 = gpio_in & 1 if gpio_in else "?"
    print(f"\nAfter IRQ clear:")
    print(f"  GPIO_IN   = 0x{gpio_in:08X} (GPIO0 = {gpio0})" if gpio_in is not None else "  GPIO_IN   = read failed")

    if gpio_in is not None and (gpio_in & 1) == 1:
        print("\n*** GPIO0 is HIGH! PMU IRQ was the cause! ***")
        print("Clearing PAD_HOLD and preparing for boot...")
        rom.write_reg(PAD_HOLD_REG, 0x00000000)
        pad_hold = rom.read_reg(PAD_HOLD_REG)
        print(f"  PAD_HOLD  = 0x{pad_hold:08X}" if pad_hold is not None else "  PAD_HOLD  = read failed")
        print("\nNow unplug USB, wait 5 seconds, and plug back in.")
        print("The watch should boot into the splash screen firmware!")
    else:
        print("\n*** GPIO0 is still LOW ***")
        print("PMU IRQ might not be the only cause, or the IRQ clear didn't work.")
        print("Clearing PAD_HOLD anyway...")
        rom.write_reg(PAD_HOLD_REG, 0x00000000)

        # Also try: read PAD_HOLD to confirm
        pad_hold = rom.read_reg(PAD_HOLD_REG)
        print(f"  PAD_HOLD after clear = 0x{pad_hold:08X}" if pad_hold is not None else "  PAD_HOLD  = read failed")

    rom.close()
    print("\nDone.")


if __name__ == "__main__":
    main()
