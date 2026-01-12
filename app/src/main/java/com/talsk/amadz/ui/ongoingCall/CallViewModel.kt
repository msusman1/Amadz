package com.talsk.amadz.ui.ongoingCall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallAdapter
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.extensions.stateInScoped
import com.talsk.amadz.domain.entity.CallState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */

@HiltViewModel
class CallViewModel @Inject constructor(
    private val contactsRepository: ContactRepository,
    private val savedStateHandle: SavedStateHandle,
    private val callAdapter: CallAdapter
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val contact: StateFlow<ContactWithCompanyName> =
        savedStateHandle.getStateFlow("phone", "")
            .mapLatest { number ->
                val realContact = contactsRepository.getContactByPhone(number)
                if (realContact != null) {
                    val companyName = contactsRepository.getCompanyName(realContact.id)
                    ContactWithCompanyName(realContact, companyName)
                } else {
                    ContactWithCompanyName(Contact.unknown(number), "")
                }
            }.stateInScoped(ContactWithCompanyName(Contact.unknown(""), ""))

    val callState: StateFlow<CallState> = callAdapter.callState

    fun onAction(action: CallAction) {
        callAdapter.dispatch(action)
    }

}

data class ContactWithCompanyName(
    val contact: Contact,
    val companyName: String?
)
