package com.talsk.amadz.ui.home.favourite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.extensions.stateInScoped
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    val favourites = contactRepository.observeFavourites()
        .stateInScoped(emptyList())

    private val _frequentCalledContacts =
        MutableStateFlow<List<Contact>>(emptyList())

    val frequentCalledContacts: StateFlow<List<Contact>> =
        _frequentCalledContacts.asStateFlow()

    init {
        loadFrequent()
    }


    private fun loadFrequent() = viewModelScope.launch {
        runCatching { callLogRepository.getFrequentCalledContacts() }
            .onSuccess {
                _frequentCalledContacts.value = it
            }.onFailure {
                Log.d("favmeod", "loadFrequent: ${it.message}")
            }

    }

}
