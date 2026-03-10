package com.vikashsinghapp.lockin.presentation.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activeCategory by viewModel.selectedCategory.collectAsState()
    var inputText by rememberSaveable { mutableStateOf("") }

//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .padding(innerPadding) // This pushes content below the TopBar
                .imePadding() // <-- if not apply then the input bar is like 20.dp away from bottom when keyboard appear
        ) {
            val listState = rememberLazyListState()

            // Auto-scroll ONLY when a new message is added AND user was at bottom
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    // do not do animateScrollTo => kind of freeze when at the top & add message, scroll laggingly
                    listState.scrollToItem(messages.lastIndex)
                }
            }

            // 1. Category Filter Strip
            LazyRow(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = cat == activeCategory,
                        onClick = { viewModel.setCategory(cat) },
                        label = { Text(cat, color = White) }
                    )
                }
            }
/*
            When the keyboard opens:
            The semantic intent (“user was at bottom”) is lost
            We must capture intent BEFORE resize
            Then restore scroll AFTER resize
            */
            // Detect if user is already near bottom
            var userWasAtBottom by remember { mutableStateOf(true) }

            LaunchedEffect(listState) {
                snapshotFlow {
                    val lastVisible =
                        listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    lastVisible == messages.lastIndex
                }.collect { atBottom ->
                    userWasAtBottom = atBottom
                }
            }
//            LaunchedEffect(messages.size) {
//                if (messages.isNotEmpty() && isAtBottom.value) {
//                    listState.animateScrollToItem(messages.lastIndex)
//                }
//            }

            // 3. THE LIST: weight(1f) tells the list to take all available space
            // but shrink when the keyboard (IME) pushes the input bar up.
            MessageList(
                modifier = Modifier.weight(1f)
//                    .border(5.dp, Color.Blue)
                ,
                messages = messages,
                listState = listState,
            )
            // detect open keyboard
            val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
            LaunchedEffect(imeBottom) {
                // Only scroll to bottom if keyboard is open and user was at bottom
                // not scrolling when not at bottom
                if (imeBottom > 0 && messages.isNotEmpty() && userWasAtBottom) {
                    listState.scrollToItem(messages.lastIndex)
                }
            }



            // 4. THE INPUT: imePadding() makes this bar "stick" to the top of the keyboard. =>
            // without it the message input just show statically at the bottom of screen
            MessageInputBar(
                modifier = Modifier
//                    .border(2.dp, Color.Red)
                    .imePadding(),
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim())
                        inputText = ""
                    }
                }
            )
        }
    }
//}