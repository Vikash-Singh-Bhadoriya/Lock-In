package com.vikashsinghapp.lockin.presentation.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.ui.theme.Running


@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<JournalMessageWithTask>,
    listState: LazyListState,
//    bringIntoViewRequester: BringIntoViewRequester
) {

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
//            .bringIntoViewRequester(bringIntoViewRequester),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(messages, key = { it.message.id }) { message ->
            MessageBubble(message)
        }
    }
}

@Composable
fun MessageBubble(item: JournalMessageWithTask) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Tag Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            item.taskCategory?.let {
                Text(
                    text = it.uppercase(),
                    color = Running, // Your primary "Running" color
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            item.taskTitle?.let {
                Text(text = "• $it", color = Color.Gray, fontSize = 10.sp)
            }
        }

        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A1A))
                .padding(14.dp)
        ) {
            Text(
                text = item.message.content,
                color = Color.White,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.message.timestamp.toTimeString(),
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}