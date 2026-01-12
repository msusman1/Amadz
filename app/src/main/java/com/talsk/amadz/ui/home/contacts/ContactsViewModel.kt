package com.talsk.amadz.ui.home.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.talsk.amadz.data.CallLogsPagingSource
import com.talsk.amadz.data.ContactsPagingSource
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.home.calllogs.CallLogUiModel
import com.talsk.amadz.util.isSameDay
import com.talsk.amadz.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date


@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactPagingSource: ContactsPagingSource,
) : ViewModel() {


    val contacts: Flow<PagingData<ContactUiModel>> = Pager(
        config = PagingConfig(pageSize = ContactsPagingSource.PAGE_SIZE),
        initialKey = ContactsPagingSource.FIRST_PAGE
    ) { contactPagingSource }.flow
        .map { pagingData ->
            pagingData.map {
                ContactUiModel.Item(it)
            }.insertSeparators { before, after ->
                when {
                    after == null -> null
                    before == null -> {
                        ContactUiModel.Header(after.contact.name.uppercase().first())
                    }

                    before.contact.name.first() != after.contact.name.first() -> {
                        ContactUiModel.Header(after.contact.name.uppercase().first())
                    }

                    else -> null
                }
            }
        }.cachedIn(viewModelScope)


}

sealed interface ContactUiModel {
    data class Header(val letter: Char) : ContactUiModel
    data class Item(val contact: Contact) : ContactUiModel
}