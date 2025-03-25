package com.talsk.amadz.ui.home

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.talsk.amadz.R
import com.talsk.amadz.core.dial
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.filterContacts
import com.talsk.amadz.data.openContactAddScreen
import com.talsk.amadz.data.openContactDetailScreen
import com.talsk.amadz.util.toTone

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/29/2023.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialpadScreen(contacts: List<ContactData>) {
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))
    val context = LocalContext.current
    var dialPhone by remember { mutableStateOf("") }
    val filteredContacts by remember(dialPhone) { mutableStateOf(contacts.filterContacts(dialPhone)) }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_DTMF, 100) }
    LaunchedEffect(key1 = true, block = {
        bottomSheetScaffoldState.bottomSheetState.expand()
    })
    BottomSheetScaffold(
        sheetContent = {
            Dialpad(
                phone = dialPhone,

                onTapDown = {
                    dialPhone += it
                    toneGenerator.startTone(it.toTone())
                },
                onTapUp = {
                    toneGenerator.stopTone()
                },
                onBackSpaceClicked = {
                    dialPhone = dialPhone.dropLast(1)
                },
                onClearClicked = {
                    dialPhone = ""
                },
                onCallClicked = { context.dial(dialPhone) },
                showCallButton = true,
                showClearButton = true,
            )
        },
        sheetShape = BottomSheetDefaults.HiddenShape,
        sheetPeekHeight = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetTonalElevation = 0.dp,
        sheetDragHandle = null,
        scaffoldState = bottomSheetScaffoldState,
    ) {
        Column(modifier = Modifier.padding(it)) {
            if (filteredContacts.isNotEmpty()) {
                HeaderItem(text = "Suggested")
            }
            LazyColumn {
                items(filteredContacts) { contact ->
                    ContactItem(
                        contact = contact,
                        onContactDetailClick = {
                            context.openContactDetailScreen(it.id)
                        },
                        onCallClick = { context.dial(it.phone) },
                    )
                }
                if (dialPhone.isNotEmpty()) {
                    item {
                        ListItem(
                            modifier = Modifier.clickable { context.openContactAddScreen(phone = dialPhone) },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_person_add_alt_24),
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = "Call"
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = "Create new contact",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                        )
                    }
                }
            }

        }

    }
}