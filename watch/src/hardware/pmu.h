#pragma once

#include <XPowersLib.h>
#include "../state/app_state.h"

// Initialize AXP2101 PMU and power rails. Returns true on success.
bool pmuInit(XPowersAXP2101& pmu);

// Get battery percentage (0-100). Returns -1 if PMU not initialized.
int pmuGetBatteryPercent(XPowersAXP2101& pmu);

// Check if USB power is connected
bool pmuIsCharging(XPowersAXP2101& pmu);

// Poll PMU for power key press events. Call every loop iteration.
ButtonEvent pmuPollButton(XPowersAXP2101& pmu);
