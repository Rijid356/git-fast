#pragma once

#include <cstdint>

// Stub step counter — returns mock data until BMA423 driver is integrated.

void stepCounterInit();

// Get accumulated step count for today
uint32_t stepCounterGetSteps();

// Reset daily step count (call at midnight)
void stepCounterReset();
