package com.gitfast.app.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.util.XpCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    characterRepository: CharacterRepository,
) : ViewModel() {

    val profile: StateFlow<CharacterProfile> =
        characterRepository.getProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CharacterProfile())

    val recentXpTransactions: StateFlow<List<XpTransaction>> =
        characterRepository.getRecentXpTransactions(20)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
