package com.talsk.amadz.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import coil3.decode.DecodeUtils.calculateInSampleSize
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repos.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */

const val TAG = "ContactsRepository"

class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ContactRepository {
    val contentResolver: ContentResolver = context.contentResolver
    val telephonyManager = getSystemService(context, TelephonyManager::class.java)
    private val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Organization.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
        ContactsContract.Contacts.STARRED
    )


    override suspend fun getAllContacts(): List<ContactData> = withContext(ioDispatcher) {
        val contacts = mutableMapOf<Long, ContactData>()
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val contact = cursor.toContactData()
                contacts.putIfAbsent(contact.id, contact)
            }
        }
        contacts.values.toList()
    }

    override suspend fun searchContacts(query: String): List<ContactData> =
        withContext(ioDispatcher) {

            if (query.isBlank()) return@withContext emptyList()

            val contacts = mutableMapOf<Long, ContactData>()

            val selection = """
            ${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?
            OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?
        """.trimIndent()

            val args = arrayOf(
                "%$query%",
                "%$query%"
            )

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                args,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val contact = cursor.toContactData()
                    contacts.putIfAbsent(contact.id, contact)
                }
            }

            contacts.values.toList()
        }


    override suspend fun getContactByPhone(phoneNumber: String): ContactData? =
        withContext(ioDispatcher) {
            val normalizedPhone = normalizePhoneNumber(phoneNumber) ?: return@withContext null

            val selection = """
                REPLACE(REPLACE(REPLACE(${ContactsContract.CommonDataKinds.Phone.NUMBER},
                ' ', ''), '-', ''), '(', '') LIKE ?
            """.trimIndent()
            val selectionArgs = arrayOf("%$normalizedPhone%")

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use {
                if (it.moveToFirst()) {
                    val oldContactData = it.toContactData()
                    val photoBitmap = loadContactImage(oldContactData.image)
                    val companyName = getCompanyName(oldContactData.id) ?: ""
                    oldContactData.copy(companyName = companyName, imageBitmap = photoBitmap)
                } else {
                    null
                }
            }
        }

    private suspend fun getCompanyName(contactId: Long): String? = withContext(ioDispatcher) {
        val orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?"
        val orgWhereParams = arrayOf(
            contactId.toString(),
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        )

        contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Organization.COMPANY),
            orgWhere,
            orgWhereParams,
            null
        )?.use {
            if (it.moveToFirst()) {
                val companyNameColumnIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)
                it.getStringOrNull(companyNameColumnIndex)
            } else {
                null
            }
        }
    }


    fun normalizePhoneNumber(phone: String): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()

        // Get the user's default country code
        val defaultRegion = telephonyManager?.simCountryIso?.uppercase(Locale.getDefault())
            ?: telephonyManager?.networkCountryIso?.uppercase(Locale.getDefault())
            ?: "US" // Default to "US" if unknown

        return try {
            // Parse the phone number
            val numberProto = phoneUtil.parse(phone, defaultRegion)

            // Format it into E.164 format (+<country_code><number>)
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            null // Return null if the phone number is invalid
        }
    }

    override suspend fun loadContactImage(photoUri: Uri?): Bitmap? = withContext(ioDispatcher) {
        if (photoUri == null) return@withContext null
        val targetSizePx: Int = 200
        return@withContext try {
            // 1. Decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            contentResolver.openInputStream(photoUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return@withContext null
            }

            // 2. Calculate optimal inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options.outWidth,
                options.outHeight,
                targetSizePx,
                targetSizePx,
                coil3.size.Scale.FILL
            )

            // 3. Decode scaled bitmap
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            contentResolver.openInputStream(photoUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

        } catch (e: SecurityException) {
            Log.e("ContactFetcher", "Permission denied while loading contact image", e)
            null
        } catch (e: IOException) {
            Log.e("ContactFetcher", "Error loading contact image", e)
            null
        }
    }


    /**
     * Get all favourite contacts as ContactData list
     */
    override suspend fun getAllFavourites(): List<ContactData> {
        val favourites = mutableListOf<ContactData>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.Contacts.STARRED
        )

        val selection = "${ContactsContract.Contacts.STARRED} = 1"

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                favourites.add(it.toContactData())
            }
        }

        // One contact may have multiple numbers → dedupe
        return favourites.distinctBy { it.id }
    }

    /**
     * Internal helper to update STARRED flag
     */
    override suspend fun updateStarred(contactId: Long, starred: Boolean) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, if (starred) 1 else 0)
        }

        val uri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactId
        )

        contentResolver.update(uri, values, null, null)
    }
}


fun Cursor.toContactData(): ContactData {
    val idColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
    val nameColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

    val numberColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val photoUriColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

    val starredColumnIndex =
        getColumnIndexOrThrow(ContactsContract.Contacts.STARRED) // ✅ NEW

    // Retrieve the contact details
    val contactId = this.getLong(idColumnIndex)
    val contactName = this.getString(nameColumnIndex)
    val contactNumber = this.getString(numberColumnIndex)
    val photoUri = this.getStringOrNull(photoUriColumnIndex)?.toUri()
    val isFavourite = getInt(starredColumnIndex) == 1
    return ContactData(
        id = contactId,
        name = contactName,
        companyName = "",
        phone = contactNumber,
        image = photoUri,
        isFavourite = isFavourite,
        imageBitmap = null
    )


}

