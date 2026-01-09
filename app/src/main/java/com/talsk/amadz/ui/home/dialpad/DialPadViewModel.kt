package com.talsk.amadz.ui.home.dialpad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.core.DefaultDtmfToneGenerator
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.filterContacts
import com.talsk.amadz.domain.DtmfToneGenerator
import com.talsk.amadz.domain.repos.ContactRepository
import com.talsk.amadz.ui.extensions.stateInScoped
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialPadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactsRepository: ContactRepository,
    private val toneGenerator: DtmfToneGenerator
) : ViewModel() {
    private val initialNumber: String? = savedStateHandle["phoneNumber"]

    private val _dialedNumber =
        MutableStateFlow(initialNumber.orEmpty())

    private val _contacts = MutableStateFlow<List<ContactData>>(emptyList())

    val uiState: StateFlow<DialPadUiState> =
        combine(_dialedNumber, _contacts) { number, contacts ->
            DialPadUiState(
                dialedNumber = number,
                suggestedContacts = contacts.filterContacts(number)
            )
        }.stateInScoped(DialPadUiState())

    init {
        viewModelScope.launch {
            _contacts.value = contactsRepository.getAllContacts()
        }
    }

    fun onDigitPressed(digit: Char) {
        _dialedNumber.update { it + digit }
        toneGenerator.startTone(digit)
    }

    fun onBackspace() {
        _dialedNumber.update { it.dropLast(1) }
    }

    fun onClear() {
        _dialedNumber.value = ""
    }

    fun onDigitReleased() {
        toneGenerator.stopTone()
    }

    override fun onCleared() {
        super.onCleared()
        if (toneGenerator is DefaultDtmfToneGenerator) {
            toneGenerator.release()
        }
    }
}

data class DialPadUiState(
    val dialedNumber: String = "",
    val suggestedContacts: List<ContactData> = emptyList()
)
