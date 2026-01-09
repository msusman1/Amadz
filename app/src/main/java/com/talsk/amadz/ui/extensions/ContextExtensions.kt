package com.talsk.amadz.ui.extensions

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import com.talsk.amadz.App

fun Context.openContactDetailScreen(contactId: Long) {
    val intent = Intent(Intent.ACTION_VIEW)
    val contactUri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
        .appendPath(contactId.toString())
        .build()
    intent.data = contactUri
    startActivity(intent)
}

fun Context.openContactAddScreen(phone: String) {
    val intent = Intent(Intent.ACTION_INSERT)
    intent.type = ContactsContract.Contacts.CONTENT_TYPE
    intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
    startActivity(intent)
}