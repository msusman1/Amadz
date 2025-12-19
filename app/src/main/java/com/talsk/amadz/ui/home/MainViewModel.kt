package com.talsk.amadz.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.App
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.CallLogsRepository
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.ContactsRepositoryImpl
import com.talsk.amadz.data.FavouriteRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context
) : ViewModel() {
    private val contactsRepositoryImpl = ContactsRepositoryImpl(applicationContext)
    private val callLogsRepository = CallLogsRepository(applicationContext)
    private val favouriteRepositoryImpl = FavouriteRepositoryImpl(applicationContext)
    var contacts = MutableStateFlow<List<ContactData>>(emptyList())
    private val pageSize = 50
    private var currentPage = 0
    private var isLoading = false
    private var allDataLoaded = false

    private val _dialedNumber = MutableStateFlow<String?>(null)
    val dialedNumber: StateFlow<String?> = _dialedNumber
    var callLogs = MutableStateFlow<List<CallLogData>>(emptyList())
        private set

    init {
        loadData()
    }


    fun onDialIntent(number: String) {
        _dialedNumber.value = number
    }

    fun consumeDialedNumber() {
        _dialedNumber.value = null
    }

    fun loadNextPage() = viewModelScope.launch(Dispatchers.IO) {
        if (isLoading || allDataLoaded) return@launch
        isLoading = true

        val nextLogs =
            callLogsRepository.getCallLogsPaged(limit = pageSize, offset = currentPage * pageSize)

        if (nextLogs.isEmpty()) {
            allDataLoaded = true
        } else {
            callLogs.update { it + nextLogs }
            currentPage++
        }

        isLoading = false
    }

    fun reloadData() {
        if (App.needDataReload) {
            loadData()
            App.needDataReload = false
        }
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        val favourites = favouriteRepositoryImpl.getAllFavourites()
        contacts.value = contactsRepositoryImpl.getAllContacts().map {
            if (favourites.map { it.id }.contains(it.id)) {
                it.copy(isFavourite = true)
            } else {
                it
            }
        }
        callLogs.value = emptyList()
        currentPage = 0
        allDataLoaded = false
        loadNextPage()

    }


    fun toggleFavourite(contactData: ContactData) {
        if (contactData.isFavourite) {
            favouriteRepositoryImpl.removeFromFav(contactData.id)
        } else {
            favouriteRepositoryImpl.addToFav(contactData.id)
        }

        contacts.update { list ->
            list.map {
                if (it.id == contactData.id) {
                    it.copy(isFavourite = it.isFavourite.not())
                } else {
                    it
                }
            }
        }
    }
}