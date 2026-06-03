
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.ui.theme.Error
import com.vikashsinghapp.lockin.ui.theme.ForceWhiteStatusBarIcons
import com.vikashsinghapp.lockin.ui.theme.Running
import com.vikashsinghapp.lockin.ui.theme.SurfaceDark
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategorySelectionAndManageSheet(
    categories: List<CategoryEntity>,
    currentCategory: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onAdd: (newCategory: CategoryEntity) -> Unit,
    onEdit: (oldCategory: CategoryEntity, newCategory: CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
) {
    // --- Use TextFieldValue for Cursor Control & Add FocusRequester ---
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // ---  The Premium Dark Mode Color Palette ---
    val PRESET_COLORS = listOf(
        0xFF8C9EFF, // Luminous Blue
        0xFFB388FF, // Neon Purple
        0xFFFFD54F, // Bright Yellow
        0xFF69F0AE, // Mint Green
        0xFFFF8A65, // Warm Orange
        0xFFF48FB1, // Soft Pink
        0xFF81D4FA  // Light Blue
    )
    var selectedColor by remember { mutableLongStateOf(PRESET_COLORS[0]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark
    ) {
        ForceWhiteStatusBarIcons()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- The Color Picker UI ---
            Text("Category Color", color = Color.Gray, fontSize = 13.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(PRESET_COLORS) { colorLong ->
                    val isSelected = selectedColor == colorLong
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorLong }
                    )
                }
            }
            OutlinedTextField(
                // --- FIX 2: Attach the focusRequester ---
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(if (editingCategory != null) "Rename category..." else "Search or create new...", color = Color.Gray)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Running, unfocusedBorderColor = SurfaceDarkElevated,
                    focusedContainerColor = SurfaceDarkElevated, unfocusedContainerColor = SurfaceDarkElevated
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val trimmed = inputText.text.trim() // Use .text with TextFieldValue
                        if (trimmed.isNotBlank()) {
                            if (editingCategory != null) {
                                onEdit(editingCategory!!, editingCategory!!.copy(name = trimmed, colorValue = selectedColor))
                                editingCategory = null
                            } else {
                                if (!categories.any { it.name.equals(trimmed, ignoreCase = true) }) {
                                    onAdd(CategoryEntity(name = trimmed, colorValue = selectedColor))
                                }
                                onSelect(trimmed)
                            }
                            inputText = TextFieldValue("") // Reset
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                ),
                trailingIcon = {
                    if (inputText.text.isNotBlank()) {
                        IconButton(onClick = {
                            val trimmed = inputText.text.trim()
                            if (editingCategory != null) {
                                onEdit(editingCategory!!, editingCategory!!.copy(name = trimmed, colorValue = selectedColor))
                                editingCategory = null
                            } else {
                                if (!categories.any { it.name.equals(trimmed, ignoreCase = true) }) {
                                    onAdd(CategoryEntity(name = trimmed, colorValue = selectedColor))
                                }
                                onSelect(trimmed)
                            }
                            inputText = TextFieldValue("") // Reset
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = Running)
                        }
                    }
                }
            )

            // --- FIX 3: Hide the list completely if we are in Edit Mode ---
            val displayCategories = if (editingCategory != null) {
                emptyList() // Returns an empty list so nothing renders below the text field!
            } else {
                categories.filter { it.name.contains(inputText.text, ignoreCase = true) }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayCategories, key = { it.id }) { cat ->
                    var showMenu by remember { mutableStateOf(false) }
                    val isSelected = currentCategory == cat.name

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Running.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (isSelected) Running else SurfaceDarkElevated, RoundedCornerShape(12.dp))
                            .drawBehind {
                                // --- Draw the color left border on the list item! ---
                                drawRect(color = Color(cat.colorValue), size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
                            }
                            .clickable { onSelect(cat.name) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat.name,
                            color = if (isSelected) Running else Color.White,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = Running, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    containerColor = SurfaceDarkElevated
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit", color = Color.White) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                                        onClick = {
                                            editingCategory = cat
                                            selectedColor = cat.colorValue // --- Auto-select its current color ---
                                            // --- Set the text, push the cursor to the end, and open keyboard ---
                                            inputText = TextFieldValue(
                                                text = cat.name,
                                                selection = TextRange(cat.name.length)
                                            )
                                            showMenu = false
                                            focusRequester.requestFocus()
                                            keyboardController?.show() // --- FORCE the keyboard to open ---
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = Error) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Error) },
                                        onClick = {
                                            onDelete(cat)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}