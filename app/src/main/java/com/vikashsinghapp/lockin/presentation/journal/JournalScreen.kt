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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.vikashsinghapp.lockin.data.entity.JournalMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "LockIn",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { /* menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                    }
                }
            )

        },
        bottomBar = {
            MessageInputBar(
                text = inputText,
                onTextChange = {
                    inputText = it
                },
                onSend = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            )
        }
    ) { padding ->
//        Column(
//            modifier = modifier
//                .fillMaxSize()
//                .padding(8.dp)
//        ) {
        MessageList(
            messages = messages,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<JournalMessage>,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(messages) { message ->
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
            text = message.timestamp.toString(),
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}


@Composable
fun MessageInputBar(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Write to yourself…") },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color(0xFF2D5BFF),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}
