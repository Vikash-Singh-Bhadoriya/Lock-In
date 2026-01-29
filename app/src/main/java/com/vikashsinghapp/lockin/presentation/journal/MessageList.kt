package com.vikashsinghapp.lockin.presentation.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.data.entity.JournalMessage


@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<JournalMessage>,
    listState: LazyListState,
//    bringIntoViewRequester: BringIntoViewRequester
) {

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
//            .bringIntoViewRequester(bringIntoViewRequester),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message)
        }
    }
}

@Composable
fun MessageBubble(message: JournalMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A1A))
                .padding(14.dp)
        ) {
            Text(
                text = message.content,
                color = Color.White,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = message.timestamp.toTimeString(),
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}