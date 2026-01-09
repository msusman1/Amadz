package com.talsk.amadz.ui.home.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.domain.repos.CallLogRepository
import com.talsk.amadz.domain.repos.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val _contacts =
        MutableStateFlow<List<ContactData>>(emptyList())

    val contacts: StateFlow<List<ContactData>> =
        _contacts.asStateFlow()


    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _contacts.value = contactRepository.getAllContacts()
        }
    }

    fun toggleFavourite(contactData: ContactData) = viewModelScope.launch {
        contactRepository.updateStarred(contactData.id, contactData.isFavourite)
    }

}