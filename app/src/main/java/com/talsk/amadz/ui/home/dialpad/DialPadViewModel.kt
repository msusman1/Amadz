package com.talsk.amadz.ui.home.dialpad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.talsk.amadz.core.DefaultDtmfToneGenerator
import com.talsk.amadz.data.ContactsSearchPagingSource
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.entity.filterContacts
import com.talsk.amadz.domain.DtmfToneGenerator
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.extensions.stateInScoped
import com.talsk.amadz.ui.home.calllogs.CallLogUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialPadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactsRepository: ContactRepository,
    private val toneGenerator: DtmfToneGenerator
) : ViewModel() {
    private val initialNumber: String = savedStateHandle["phoneNumber"] ?: ""

    private val _dialedNumber = MutableStateFlow(initialNumber)
    val dialedNumber = _dialedNumber.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val contacts: Flow<PagingData<Contact>> =
        dialedNumber
            .debounce(300) // prevent excessive queries
            .distinctUntilChanged()
            .flatMapLatest { query ->
                Pager(
                    config = PagingConfig(
                        pageSize = ContactsSearchPagingSource.PAGE_SIZE,
                        enablePlaceholders = false
                    )
                ) {
                    ContactsSearchPagingSource(
                        contactRepository = contactsRepository,
                        query = query
                    )
                }.flow
            }
            .cachedIn(viewModelScope)


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