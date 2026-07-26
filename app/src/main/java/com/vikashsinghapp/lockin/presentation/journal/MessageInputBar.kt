package com.vikashsinghapp.lockin.presentation.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@Composable
fun MessageInputBar(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = modifier
            .background(BackgroundDark)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text("Log entry...", color = Color(0xFF555555))
            },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceDarkElevated,
                unfocusedContainerColor = SurfaceDarkElevated,
                focusedTextColor = Color(0xFFE9EDEF),
                unfocusedTextColor = Color(0xFFE9EDEF),
                cursorColor = Color(0xFF8696A0),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank(),
            modifier = Modifier
                .size(46.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Log",
                tint = if (text.isNotBlank()) Color(0xFF8696A0) else Color(0xFF333333),
            )
        }
    }
}
