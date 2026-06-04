package com.vikashsinghapp.lockin.presentation.settings.feedback

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vikashsinghapp.lockin.presentation.settings.composeEmailWithUris
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark
import com.vikashsinghapp.lockin.ui.theme.Error
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf("Bug Report") }
    var feedbackText by remember { mutableStateOf("") }

    val feedbackTypes = listOf("Bug Report", "Feature Request", "Get Help", "Other")
    var attachedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Contact Us",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- 1. Type Selector ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "What can we help you with?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Using a FlowRow or wrapped layout for chips
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    feedbackTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    type,
                                    color = if (selectedType == type) Color.Black else Color.White
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SurfaceDarkElevated,
                                selectedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = null
                        )
                    }
                }
            }

            // --- 2. Custom Input Area with Image Preview ---
            FeedbackTextFieldWithImage(
                modifier = Modifier.weight(1f),
                text = feedbackText,
                onValueChange = { feedbackText = it },
                attachedUris = attachedUris,
                onUrisChanged = { attachedUris = it }
            )

            // --- 3. Submit Button ---
            Button(
                onClick = {
                    val formattedMessage = "Issue Type: $selectedType\n\nDetails:\n$feedbackText"
                    composeEmailWithUris(context, feedback = formattedMessage, attachedUris)
                },
                enabled = feedbackText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Running,
                    disabledContainerColor = SurfaceDarkElevated
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = if (feedbackText.isNotBlank()) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                // "Send via Email" perfectly sets their expectation. They know exactly what is going to happen when they tap it.
                Text(
                    "Send via Email",
                    color = if (feedbackText.isNotBlank()) Color.White else Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FeedbackTextFieldWithImage(
    modifier: Modifier = Modifier,
    text: String,
    onValueChange: (String) -> Unit,
    attachedUris: List<Uri>,
    onUrisChanged: (List<Uri>) -> Unit,
) {
    val configuration = LocalWindowInfo.current
    val minHeight = configuration.containerSize.height.dp * 0.25f
    val focusManager = LocalFocusManager.current

    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Modern Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val combinedUris = (attachedUris + uris).distinct()
            onUrisChanged(combinedUris)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkElevated)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .weight(1f, fill = false)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null // This prevents the ugly button ripple effect!
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show() // --- FORCE the keyboard to open ---
                }
        ) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                value = text,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Running),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                    keyboardController?.hide()
                }),
            )
            if (text.isEmpty()) {
                Text("Please describe the issue or feature in detail...", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Preview Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                IconButton(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray.copy(alpha = 0.3f))
                        .padding(8.dp),
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Photo",
                        tint = Color.LightGray
                    )
                }
            }

            items(attachedUris) { uri ->
                Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    // AsyncImage natively supports Uris!
                    AsyncImage(
                        model = uri,
                        contentDescription = "Attached Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    // Remove Button
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Error)
                            .clickable {
                                onUrisChanged(attachedUris.filter { it != uri })
                            }
                            .padding(4.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White
                    )
                }
            }
        }
    }
}