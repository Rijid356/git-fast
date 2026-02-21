package com.gitfast.app.ui.analytics.records

data class RecordItem(
    val title: String,
    val value: String,
    val context: String,
    val workoutId: String?,
)

data class RecordSection(
    val header: String,
    val records: List<RecordItem>,
)

data class PersonalRecordsUiState(
    val sections: List<RecordSection> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
)
