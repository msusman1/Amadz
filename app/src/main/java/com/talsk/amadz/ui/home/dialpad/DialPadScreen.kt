package com.talsk.amadz.ui.home.dialpad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.talsk.amadz.R
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.ui.components.ContactItem
import com.talsk.amadz.ui.components.LazyPagedColumn
import com.talsk.amadz.ui.home.HeaderItem
import com.talsk.amadz.ui.home.KeyPad
import kotlinx.coroutines.flow.flow

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/29/2023.
 */


@Preview
@Composable
private fun DialPadScreenPrev() {
    val flo = flow<PagingData<Contact>> {
        emit(PagingData.empty())
    }
    DialPadScreenInternal(
        contacts = flo.collectAsLazyPagingItems(),
        dialedNumber = "324334",
        onDigitPressed = {},
        onDigitReleased = {},
        onBackspace = {},
        onClear = {},
        onCallClicked = {},
        onContactCallClicked = {},
        onContactDetailClicked = {},
        onContactAddClicked = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialPadScreen(
    onPhoneDialed: (String) -> Unit,
    onContactDetailClicked: (Contact) -> Unit,
    onContactAddClicked: (String) -> Unit,
    vm: DialPadViewModel = hiltViewModel()
) {
    val contacts = vm.contacts.collectAsLazyPagingItems()
    val dialedNumber by vm.dialedNumber.collectAsStateWithLifecycle()

    DialPadScreenInternal(
        contacts = contacts,
        dialedNumber = dialedNumber,
        onDigitPressed = vm::onDigitPressed,
        onDigitReleased = vm::onDigitReleased,
        onBackspace = vm::onBackspace,
        onClear = vm::onClear,
        onCallClicked = { onPhoneDialed(dialedNumber) },
        onContactCallClicked = { onPhoneDialed(it.phone) },
        onContactDetailClicked = onContactDetailClicked,
        onContactAddClicked = onContactAddClicked,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialPadScreenInternal(
    contacts: LazyPagingItems<Contact>,
    dialedNumber: String,
    onDigitPressed: (Char) -> Unit,
    onDigitReleased: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCallClicked: () -> Unit,
    onContactCallClicked: (Contact) -> Unit,
    onContactDetailClicked: (Contact) -> Unit,
    onContactAddClicked: (String) -> Unit,
) {
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(
            rememberStandardBottomSheetState(
                skipHiddenState = true,
                initialValue = SheetValue.Expanded
            )
        )


    BottomSheetScaffold(
        modifier = Modifier.statusBarsPadding(),
        sheetContent = {
            KeyPad(
                phone = dialedNumber,
                onTapDown = onDigitPressed,
                onTapUp = onDigitReleased,
                onBackSpaceClicked = onBackspace,
                onClearClicked = onClear,
                onCallClicked = onCallClicked,
                showCallButton = true,
                showClearButton = true,
            )
        },
        sheetShape = BottomSheetDefaults.HiddenShape,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        scaffoldState = bottomSheetScaffoldState,
    ) {
        Column(modifier = Modifier.padding(it)) {
            if (dialedNumber.isNotEmpty()) {
                NewContactHeader(onContactAddClicked, dialedNumber)
            }
            if (contacts.itemCount > 0) {
                HeaderItem(text = "Suggested")
            }

            LazyPagedColumn(contacts) {
                items(contacts.itemCount, key = {
                    contacts[it]?.let { contact -> "${contact.id}_${contact.phone}" } ?: it
                }) {
                    contacts.peek(it)?.let {
                        ContactItem(
                            contact = it,
                            onContactDetailClick = onContactDetailClicked,
                            onCallClick = { onContactCallClicked(it) },
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun NewContactHeader(
    onContactAddCLicked: (String) -> Unit, dialPhone: String
) {
    ListItem(
        modifier = Modifier.clickable { onContactAddCLicked(dialPhone) },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_add_alt_24),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Call"
            )
        },
        headlineContent = {
            Text(
                text = "Create new contact", color = MaterialTheme.colorScheme.primary
            )
        },
    )
}