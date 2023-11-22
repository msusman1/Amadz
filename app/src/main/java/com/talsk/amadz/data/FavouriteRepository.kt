package com.talsk.amadz.data

import android.content.Context
import androidx.core.content.edit

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/22/2023.
 */
class FavouriteRepository(val context: Context) {
    private var favContactIds = mutableSetOf<Long>()
    private val prefs = context.getSharedPreferences("amadx", Context.MODE_PRIVATE)
    val FAV = "favs"

    init {

        val stringSet = prefs.getStringSet(FAV, emptySet()) ?: emptySet()
        favContactIds.addAll(stringSet.map { it.toLong() }.toSet())
    }

    fun addToFav(id: Long) {
        favContactIds.add(id)
        prefs.edit {
            putStringSet(FAV, favContactIds.map { it.toString() }.toSet())
        }

    }

    fun removeFromFav(id: Long) {
        favContactIds.remove(id)
        prefs.edit {
            putStringSet(FAV, favContactIds.map { it.toString() }.toSet())
        }
    }

    fun getAllFavourites(): Set<Long> {
        return favContactIds
    }
}