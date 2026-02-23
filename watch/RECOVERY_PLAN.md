# T-Watch S3 Recovery Plan

## Status
GPIO0 stuck LOW → forced download mode on every reset. Watch is non-functional.
Battery drain in progress to clear persistent RTC state (PAD_HOLD register).

## What's in Flash
The exact working firmware from commit `030aa3f` (splash screen showing "git-fast").
All 4 files flashed and hash-verified: bootloader(0x0), partitions(0x8000), boot_app0(0xE000), firmware(0x10000).

## After Battery Drain (24-48 hours unplugged)

### Step 1: Plug in and try to boot
```bash
# Plug USB in — NO buttons held
# Then run:
python -m esptool --chip esp32s3 --port COM3 --before no_reset --after watchdog_reset run
```
If the screen shows "git-fast" on a black background — **it worked!** Skip to Step 4.

### Step 2: If still in download mode, check GPIO0
```bash
python C:/AppDev/Apps/git-fast/watch/tools/check_rtc.py COM3
```
Look for:
- `GPIO0 = 1` → good, try watchdog_reset again
- `GPIO0 = 0` → still stuck, check PAD_HOLD value
- `PAD_HOLD = 0x00000000` → hold cleared but GPIO0 still LOW = hardware short
- `PAD_HOLD = 0x00000FFF` → hold didn't clear, battery didn't fully drain — wait longer

### Step 3: If PAD_HOLD cleared but GPIO0 still LOW
Try clearing hold + driving GPIO0 HIGH + watchdog reset:
```bash
python C:/AppDev/Apps/git-fast/watch/tools/force_boot.py COM3
```
If that fails too, GPIO0 has a hardware short. Last resort: burn eFuse.

### Step 4: Last resort — burn DIS_DOWNLOAD_MODE eFuse (PERMANENT)
**Only do this if all other options exhausted. This is irreversible.**
```bash
# This permanently disables download mode — no more USB flashing ever
python -m espefuse --port COM3 --before no_reset burn_efuse DIS_DOWNLOAD_MODE
# Then reset:
python -m esptool --chip esp32s3 --port COM3 --before no_reset --after watchdog_reset run
```
After this, future firmware updates must use OTA (WiFi) or JTAG.

## Step 5: Once booted — fix the firmware
The current firmware is the simple splash screen. We need to restore the full UI but with SAFE PMU handling:

**NEVER do these again:**
- `pmu.disableLongPressShutdown()` — this bricked the watch
- `pmu.disableIRQ(XPOWERS_AXP2101_ALL_IRQ)` — disables all PMU interrupts

**Safe PMU button polling (already written in `src/hardware/pmu.cpp`):**
```cpp
bool pmuInit(XPowersAXP2101& pmu) {
    if (!pmu.begin(Wire, AXP2101_SLAVE_ADDRESS, PIN_I2C_SDA, PIN_I2C_SCL)) return false;
    pmu.setALDO1Voltage(3300); pmu.enableALDO1();
    pmu.setALDO2Voltage(3300); pmu.enableALDO2();
    pmu.setALDO3Voltage(3300); pmu.enableALDO3();
    pmu.setALDO4Voltage(3300); pmu.enableALDO4();
    pmu.setBLDO2Voltage(3300); pmu.enableBLDO2();
    // DO NOT disable long press shutdown or IRQs
    return true;
}

ButtonEvent pmuPollButton(XPowersAXP2101& pmu) {
    pmu.getIrqStatus();
    ButtonEvent event = ButtonEvent::NONE;
    if (pmu.isPekeyLongPressIrq()) event = ButtonEvent::LONG_PRESS;
    else if (pmu.isPekeyShortPressIrq()) event = ButtonEvent::SHORT_PRESS;
    pmu.clearIrqStatus();
    return event;
}
```

## Recovery Tools (in watch/tools/)
- `pmu_fix_v3.py` / `pmu_fix_v4.py` — I2C bit-bang PMU register fixes
- `battery_check.py` — read battery/power status via I2C
- `rtc_gpio_check.py` — scan RTC/LP_IO/USB registers for GPIO0 culprits
- `check_rtc.py` — check FORCE_DOWNLOAD_BOOT and PAD_HOLD registers
- `force_boot.py` — drive GPIO0 HIGH + hold + watchdog reset
- `read_gpio.py` — read GPIO pin states and IO_MUX config
- `backlight_test.py` — toggle backlight GPIO45 from download mode
- `power_and_backlight.py` — enable PMU LDOs + backlight from download mode

## Root Cause
The firmware called `disableLongPressShutdown()` + `disableIRQ(ALL)` on the AXP2101 PMU.
The firmware then crashed (or the device was reset) while GPIO0 was LOW.
PAD_HOLD (0x600080A4) = 0x00000FFF froze GPIO0-11 in their current state.
Since GPIO0 was LOW and PAD_HOLD persists across resets (battery-backed RTC), the device is locked in download mode.
