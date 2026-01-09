package com.talsk.amadz.ui.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallAdapter
import com.talsk.amadz.domain.repos.ContactRepository
import com.talsk.amadz.ui.extensions.stateInScoped
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
    val contact: StateFlow<ContactData> =
        savedStateHandle.getStateFlow("phone", "")
            .mapLatest { number ->
                contactsRepository.getContactByPhone(number)
                    ?: ContactData.unknown(number)
            }.stateInScoped(ContactData.unknown(""))

    val callState: StateFlow<CallState> = callAdapter.callState

    fun onAction(action: CallAction) {
        callAdapter.dispatch(action)
    }
}
