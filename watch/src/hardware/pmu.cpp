#include "pmu.h"
#include "../config/pins.h"
#include <Wire.h>

bool pmuInit(XPowersAXP2101& pmu) {
    if (!pmu.begin(Wire, AXP2101_SLAVE_ADDRESS, PIN_I2C_SDA, PIN_I2C_SCL)) {
        return false;
    }

    // Enable all power rails at 3.3V
    pmu.setALDO1Voltage(3300);  pmu.enableALDO1();
    pmu.setALDO2Voltage(3300);  pmu.enableALDO2();  // Display power
    pmu.setALDO3Voltage(3300);  pmu.enableALDO3();
    pmu.setALDO4Voltage(3300);  pmu.enableALDO4();
    pmu.setBLDO2Voltage(3300);  pmu.enableBLDO2();

    return true;
}

int pmuGetBatteryPercent(XPowersAXP2101& pmu) {
    if (!pmu.isBatteryConnect()) return -1;
    return pmu.getBatteryPercent();
}

bool pmuIsCharging(XPowersAXP2101& pmu) {
    return pmu.isCharging();
}

ButtonEvent pmuPollButton(XPowersAXP2101& pmu) {
    pmu.getIrqStatus();

    ButtonEvent event = ButtonEvent::NONE;

    if (pmu.isPekeyLongPressIrq()) {
        event = ButtonEvent::LONG_PRESS;
    } else if (pmu.isPekeyShortPressIrq()) {
        event = ButtonEvent::SHORT_PRESS;
    }

    pmu.clearIrqStatus();
    return event;
}
