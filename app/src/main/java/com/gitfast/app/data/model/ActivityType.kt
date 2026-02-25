package com.gitfast.app.data.model

enum class ActivityType {
    RUN, DOG_WALK, DOG_RUN;

    val isDogActivity: Boolean get() = this == DOG_WALK || this == DOG_RUN
}
