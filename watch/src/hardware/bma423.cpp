#include "bma423.h"

static uint32_t _mockSteps = 1247;

void stepCounterInit() {
    // TODO: Initialize BMA423 via I2C
    // For now, start with a mock value
    _mockSteps = 1247;
}

uint32_t stepCounterGetSteps() {
    return _mockSteps;
}

void stepCounterReset() {
    _mockSteps = 0;
}
