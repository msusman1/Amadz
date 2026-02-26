package com.talsk.amadz.ui.home.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.talsk.amadz.data.ContactsPagingSource
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
) : ViewModel() {


    val contacts: Flow<PagingData<ContactUiModel>> = Pager(
        config = PagingConfig(pageSize = ContactsPagingSource.PAGE_SIZE),
        initialKey = ContactsPagingSource.FIRST_PAGE
    ) { ContactsPagingSource(contactRepository) }.flow
        .map { pagingData ->
            pagingData.map {
                ContactUiModel.Item(it)
            }.insertSeparators { before, after ->
                val afterLetter = after?.contact?.name
                    ?.trim()
                    ?.firstOrNull()
                    ?.uppercaseChar() ?: '#'
                val beforeLetter = before?.contact?.name
                    ?.trim()
                    ?.firstOrNull()
                    ?.uppercaseChar() ?: '#'
                when {
                    after == null -> null
                    before == null -> {
                        ContactUiModel.Header(afterLetter)
                    }

                    beforeLetter != afterLetter -> {
                        ContactUiModel.Header(afterLetter)
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