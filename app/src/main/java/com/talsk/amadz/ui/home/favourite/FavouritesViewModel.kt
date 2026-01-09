package com.talsk.amadz.ui.home.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.domain.repos.CallLogRepository
import com.talsk.amadz.domain.repos.ContactRepository
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

    private val _favourites =
        MutableStateFlow<List<ContactData>>(emptyList())

    val favourites: StateFlow<List<ContactData>> =
        _favourites.asStateFlow()

    private val _frequentCalledContacts =
        MutableStateFlow<List<ContactData>>(emptyList())

    val frequentCalledContacts: StateFlow<List<ContactData>> =
        _frequentCalledContacts.asStateFlow()

    init {
        loadFavourites()
        loadFrequent()
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            _favourites.value = contactRepository.getAllFavourites()
        }
    }

    private fun loadFrequent() {
        viewModelScope.launch {
            _frequentCalledContacts.value = callLogRepository.getFrequentCalledContacts()
        }
    }

}
