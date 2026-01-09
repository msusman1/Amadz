package com.talsk.amadz.ui.home.searchbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repos.CallLogRepository
import com.talsk.amadz.domain.repos.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _contacts = MutableStateFlow<List<ContactData>>(emptyList())
    val contacts: StateFlow<List<ContactData>> = _contacts.asStateFlow()

    init {
        observeQuery()
    }

    /** Called from the Composable when the search text changes */
    fun onSearchQueryChanged(query: String) {
        _query.value = query
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeQuery() {
        viewModelScope.launch(ioDispatcher) {
            _query
                .debounce(300) // Wait 300ms to avoid querying on every keystroke
                .distinctUntilChanged()
                .flatMapLatest { q ->
                    flow {
                        val result = if (q.isBlank()) {
                            contactRepository.getAllContacts()
                        } else {
                            contactRepository.searchContacts(q)
                        }
                        emit(result)
                    }
                }
                .collect { result ->
                    _contacts.value = result
                }
        }
    }

    fun toggleFavourite(contactData: ContactData) = viewModelScope.launch {
        contactRepository.updateStarred(contactData.id, contactData.isFavourite)
    }
}
