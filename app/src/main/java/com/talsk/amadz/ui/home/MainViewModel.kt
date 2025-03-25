package com.talsk.amadz.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.App
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.CallLogsRepository
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.ContactsRepository
import com.talsk.amadz.data.FavouriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsRepository = ContactsRepository(application.applicationContext)
    private val callLogsRepository = CallLogsRepository(application.applicationContext)
    private val favouriteRepository = FavouriteRepository(application.applicationContext)
    var contacts = MutableStateFlow<List<ContactData>>(emptyList())
    var callLogs = MutableStateFlow<List<CallLogData>>(emptyList())

    init {
        loadData()
    }

    fun reloadData() {
        if (App.needDataReload) {
            loadData()
            App.needDataReload = false
        }
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        val favIds = favouriteRepository.getAllFavourites()
        contacts.value = contactsRepository.getAllContacts().map {
            if (favIds.contains(it.id)) {
                it.copy(isFavourite = true)
            } else {
                it
            }
        }
        callLogs.value = callLogsRepository.getAllCallLogs()

    }


    fun toggleFavourite(contactData: ContactData) {
        if (contactData.isFavourite) {
            favouriteRepository.removeFromFav(contactData.id)
        } else {
            favouriteRepository.addToFav(contactData.id)
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