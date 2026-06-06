package com.vikashsinghapp.lockin.presentation.core

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
) {
    var readyToDraw by remember(text.length) { mutableStateOf(false) }
    // when character length changes like "11:00:00", "1:00:00", "10:00" etc
    // then recalculate the new fontSize (which will be greater as char reduce)
    var fontSize by remember(text.length) { mutableStateOf(246.sp) }

    // Loading while text not visible
    if (!readyToDraw) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
    Text(
        modifier = modifier
//            .border(1.dp, Color.Green)
            .drawWithContent { if (readyToDraw) drawContent() },
//            .border(1.dp, Color.Red),
        text = text,
        color = color,
        maxLines = maxLines,
        style = style,
        fontSize = fontSize,
        onTextLayout = {
//            Timber.tag(TAG).d("onTextLayout")
//            Timber.tag(TAG).d("it.didOverflowHeight = ${it.didOverflowHeight}")
//            Timber.tag(TAG).d("it.didOverflowWidth = ${it.didOverflowWidth}")
//            Timber.tag(TAG).d("!readyToDraw = ${!readyToDraw}")

            if (it.didOverflowHeight || it.didOverflowWidth) {
//                Timber.d("Did Overflow height, calculate next font size value")
                fontSize *= 0.9
//                Timber.tag(TAG).d("fontSizeValue: $fontSize")
            } else {
                readyToDraw = true
//                Timber.tag(TAG).d("readyToDraw = true")
            }
        },
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap
    )
}