package com.talsk.amadz.ui.home

import android.content.Context
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.talsk.amadz.R
import com.talsk.amadz.ui.IconButtonLongClickable
import com.talsk.amadz.ui.theme.AmadzTheme

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KeyPadPrew() {
    AmadzTheme(darkTheme = true) {

        Column {

            Spacer(Modifier.height(48.dp))
            KeyPad(
                phone = TextFieldValue("2345"),
                onPhoneChange = {},
                onTapDown = {},
                onTapUp = {},
                onBackSpaceClicked = {},
                onClearClicked = {},
                onCallClicked = {},
                showClearButton = false,
                showCallButton = false,
            )
        }
    }
}


@Composable
fun KeyPad(
    modifier: Modifier = Modifier,
    phone: TextFieldValue,
    onPhoneChange: (TextFieldValue) -> Unit,
    onTapDown: (Char) -> Unit,
    onTapUp: () -> Unit,
    onBackSpaceClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onCallClicked: () -> Unit,
    showCallButton: Boolean,
    showClearButton: Boolean,
) {
    val textStyle = MaterialTheme.typography.headlineMedium
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textSize = with(LocalDensity.current) { textStyle.fontSize.toPx() }

    Surface(modifier =modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ){
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 56.dp)
                        .heightIn(min = 48.dp),
                    factory = { context ->
                        DialpadEditText(context).apply {
                            gravity = Gravity.CENTER
                            setSingleLine(true)
                            inputType = EditorInfo.TYPE_CLASS_PHONE
                            showSoftInputOnFocus = false
                            isCursorVisible = true
                            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize)
                            setTextColor(textColor.toArgb())
                            background = null
                            onValueChange = onPhoneChange
                            setText(phone.text)
                            setSelection(phone.selection.start, phone.selection.end)
                        }
                    },
                    update = { editText ->
                        editText.onValueChange = onPhoneChange
                        editText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize)
                        editText.setTextColor(textColor.toArgb())
                        if (editText.text.toString() != phone.text) {
                            editText.setText(phone.text)
                        }
                        val start = phone.selection.start.coerceIn(0, editText.length())
                        val end = phone.selection.end.coerceIn(0, editText.length())
                        if (editText.selectionStart != start || editText.selectionEnd != end) {
                            editText.setSelection(start, end)
                        }
                    },
                )
                if (showClearButton) {
                    IconButtonLongClickable(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onLongClick = onClearClicked,
                        onClick = {
                            if (phone.text.isNotEmpty()) {
                                onBackSpaceClicked()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Backspace, contentDescription = "Call"
                        )
                    }
                }

            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DialButton(
                    title = '1',
                    subtitle = "",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '2',
                    subtitle = "ABC",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '3',
                    subtitle = "DEF",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DialButton(
                    title = '4',
                    subtitle = "GHI",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '5',
                    subtitle = "JKL",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '6',
                    subtitle = "MNO",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DialButton(
                    title = '7',
                    subtitle = "PQRS",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '8',
                    subtitle = "TUV",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '9',
                    subtitle = "WXYZ",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DialButton(
                    title = '*',
                    subtitle = "",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '0',
                    subtitle = "+",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
                DialButton(
                    title = '#',
                    subtitle = "",
                    onTapDown = onTapDown,
                    onTapUp = onTapUp
                )
            }
            if (showCallButton) {
                Button(
                    onClick = { onCallClicked() },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier
                        .height(56.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_call_24),
                        contentDescription = "phone"
                    )
                    Text(text = "Call", modifier = Modifier.padding(start = 16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}

private class DialpadEditText(context: Context) : EditText(context) {
    var onValueChange: ((TextFieldValue) -> Unit)? = null

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        val textLength = length()
        onValueChange?.invoke(
            TextFieldValue(
                text = text.toString(),
                selection = TextRange(
                    selStart.coerceIn(0, textLength),
                    selEnd.coerceIn(0, textLength),
                ),
            )
        )
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int,
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        val currentText = text?.toString().orEmpty()
        onValueChange?.invoke(
            TextFieldValue(
                text = currentText,
                selection = TextRange(
                    selectionStart.coerceIn(0, currentText.length),
                    selectionEnd.coerceIn(0, currentText.length),
                ),
            )
        )
    }
}

fun TextFieldValue.insertAtSelection(insertedText: String): TextFieldValue {
    val start = selection.min
    val end = selection.max
    val updatedText = text.replaceRange(start, end, insertedText)
    val cursor = start + insertedText.length
    return TextFieldValue(updatedText, TextRange(cursor))
}

fun TextFieldValue.deleteBeforeCursor(): TextFieldValue {
    val start = selection.min
    val end = selection.max
    if (start != end) {
        val updatedText = text.removeRange(start, end)
        return TextFieldValue(updatedText, TextRange(start))
    }
    if (start == 0) return this

    val updatedText = text.removeRange(start - 1, start)
    return TextFieldValue(updatedText, TextRange(start - 1))
}


@Composable
fun RowScope.DialButton(
    title: Char,
    subtitle: String,
    onTapDown: (Char) -> Unit,
    onTapUp: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,

                )
            .clip(RoundedCornerShape(16.dp))
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        onTapDown(title)
                        tryAwaitRelease()
                        interactionSource.emit(PressInteraction.Release(press))
                        onTapUp()
                    }
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}