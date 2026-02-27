package com.talsk.amadz.ui.home.searchbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.talsk.amadz.data.ContactsSearchPagingSource
import com.talsk.amadz.domain.DtmfToneGenerator
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val dtmfToneGenerator: DtmfToneGenerator
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val contacts: Flow<PagingData<Contact>> =
        query
            .debounce(300) // prevent excessive queries
            .distinctUntilChanged()
            .flatMapLatest { q ->
                Pager(
                    config = PagingConfig(
                        pageSize = ContactsSearchPagingSource.PAGE_SIZE,
                        enablePlaceholders = false
                    )
                ) {
                    ContactsSearchPagingSource(
                        contactRepository = contactRepository,
                        callLogRepository = callLogRepository,
                        query = q
                    )
                }.flow
            }
            .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        _query.value = query
    }

    fun startTone(char: Char) {
        dtmfToneGenerator.startTone(char)
    }

    fun stopTone() {
        dtmfToneGenerator.stopTone()
    }

}
