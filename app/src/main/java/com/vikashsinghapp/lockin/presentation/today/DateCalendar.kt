package com.vikashsinghapp.lockin.presentation.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vikashsinghapp.lockin.Constants.TAG
import com.vikashsinghapp.lockin.R
import com.vikashsinghapp.lockin.formatDate
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun DateCalendar(
    modifier: Modifier = Modifier,
    isCalendarVisible: Boolean,
    onShowCalendar: () -> Unit,
    onHideCalendar: () -> Unit,
    selectedDate: LocalDate,
    onSelectedDateChange: (LocalDate) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF2D5BFF), RoundedCornerShape(24.dp))
                .clickable {
                    if (isCalendarVisible) onHideCalendar() else onShowCalendar()
                }
                .padding(start = 4.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val date = remember(selectedDate) {
                if (LocalDate.now() == selectedDate) "Today" else selectedDate.formatDate()
            }
            Text(
                modifier = Modifier,
                text = date,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = White,
            )
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .then(
                        if (!isCalendarVisible) Modifier.rotate(180F) else Modifier
                    ),
                painter = painterResource(id = R.drawable.ic_up_down),
                tint = White,
                contentDescription = if (isCalendarVisible) "Close" else "Open"
            )
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = isCalendarVisible,
            enter = slideInVertically(
                animationSpec = tween(200),
                initialOffsetY = { -it }) + expandVertically(
                // Expand from the top.
                expandFrom = Alignment.Top
            ) + fadeIn(
                // Fade in with the initial alpha of 0.2f.
                initialAlpha = 0f
            ),
            exit = slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { it }) + shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            val datePickerState =
                rememberDatePickerState(
                    // set time to midnight and
                    // get the epoch (since 1 Jan 1970) Milliseconds
                    initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC)
                        .toInstant().toEpochMilli()
                )
            Timber.tag(TAG)
                .d("datePickerState.selectedDateMillis: ${datePickerState.selectedDateMillis}")
            LaunchedEffect(key1 = datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { dateMillis ->
                    Timber.tag(TAG)
                        .d("datePickerState.selectedDateMillis: ${datePickerState.selectedDateMillis}")
                    val localDate =
                        Instant.ofEpochMilli(dateMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    Timber.tag(TAG).d("localDate: $localDate")
                    onSelectedDateChange(localDate)
                }
            }

            // Wrap DatePicker in a custom MaterialTheme to force icon tint to White
            Surface(
                color = Transparent,
                contentColor = White // needed for Month drop down icon
            ) {
                DatePicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .border(
                            1.dp,
                            Color(0xFF071146), // Blue border for visibility
                            MaterialTheme.shapes.large
                        )
                        .padding(1.dp),
                    state = datePickerState,
                    showModeToggle = true,
                    colors = DatePickerDefaults.colors(
                        containerColor = SurfaceDarkElevated, // Calendar background
                        titleContentColor = White.copy(alpha = 0.7f), // "Select date" title
                        headlineContentColor = White, // Selected date (e.g., "10 Mar 2026")
                        weekdayContentColor = Color(0xFFB0B0B0), // Light gray for weekdays
                        subheadContentColor = Color(0xFFB0B0B0), // Light gray for month/year
                        navigationContentColor = White, // Navigation arrows/icons
                        yearContentColor = White, // Year text
                        disabledYearContentColor = Color(0xFF666666), // Dark gray for disabled year
                        currentYearContentColor = Color(0xFF2D5BFF), // Blue accent for current year
                        selectedYearContentColor = White, // Selected year text
                        disabledSelectedYearContentColor = Color(0xFF888888), // Disabled selected year
                        selectedYearContainerColor = Color(0xFF2D5BFF).copy(alpha = 0.2f), // Selected year background
                        disabledSelectedYearContainerColor = SurfaceDarkElevated, // Disabled selected year background
                        dayContentColor = White, // Day text
                        disabledDayContentColor = Color(0xFF666666), // Disabled day
                        selectedDayContentColor = White, // Selected day text
                        disabledSelectedDayContentColor = Color(0xFF888888), // Disabled selected day
                        selectedDayContainerColor = Color(0xFF2D5BFF), // Selected day background
                        disabledSelectedDayContainerColor = SurfaceDarkElevated, // Disabled selected day background
                        todayContentColor = Color(0xFF2D5BFF), // Today text
                        todayDateBorderColor = Color(0xFF2D5BFF), // Today border
                        dayInSelectionRangeContentColor = White, // Range day text
                        dayInSelectionRangeContainerColor = Color(0xFF2D5BFF).copy(alpha = 0.15f), // Range day background
                        dividerColor = Color(0xFF444444), // Divider
                        dateTextFieldColors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            disabledTextColor = Color(0xFF888888),
                            errorTextColor = Color(0xFFFF5252), // Red for error
                            focusedContainerColor = SurfaceDarkElevated,
                            unfocusedContainerColor = SurfaceDarkElevated,
                            disabledContainerColor = SurfaceDarkElevated,
                            errorContainerColor = SurfaceDarkElevated,
                            cursorColor = Color(0xFF2D5BFF),
                            errorCursorColor = Color(0xFFFF5252),
                            focusedIndicatorColor = Color(0xFF2D5BFF), // Blue border when focused
                            unfocusedIndicatorColor = Color(0xFF444444), // Gray border when unfocused
                            disabledIndicatorColor = Color(0xFF444444),
                            errorIndicatorColor = Color(0xFFFF5252),
                            focusedLabelColor = White,
                            unfocusedLabelColor = Color(0xFFB0B0B0),
                            disabledLabelColor = Color(0xFF888888),
                            errorLabelColor = Color(0xFFFF5252),
                            focusedPlaceholderColor = Color(0xFFB0B0B0),
                            unfocusedPlaceholderColor = Color(0xFFB0B0B0),
                            disabledPlaceholderColor = Color(0xFF888888),
                            errorPlaceholderColor = Color(0xFFFF5252),
                        ),
                    )
                )
            }
        }
    }
}

//@Composable
//fun InfiniteCalendarLazyRow(
//    selectedDate: LocalDate,
//    onDaySelected: (LocalDate) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    // Show 1000 days before and after for performance (can increase if needed)
//    val range = (-1000..1000)
//
//    Column(
//        modifier = modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Month & Year header
//        Text(
//            text = "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.year}",
//            color = Color.White,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//        LazyRow(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            flingBehavior = ScrollableDefaults.flingBehavior()
//        ) {
//            itemsIndexed(range.toList()) { _, offset ->
//                val date = selectedDate.plusDays(offset.toLong())
//                val isSelected = date == selectedDate
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier
//                        .padding(2.dp)
//                        .clip(RoundedCornerShape(0.6f))
//                        .background(if (isSelected) Color(0xFF2D5BFF) else SurfaceDarkElevated)
//                        .padding(2.dp)
//                        .clickable { onDaySelected(date) }
//                        .size(48.dp)
//                ) {
//                    Text(
//                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
//                        color = Color.White,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    Text(
//                        text = date.dayOfMonth.toString(),
//                        color = Color.White,
//                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AvocationCalendar(
//    selectedDate: java.time.LocalDate,
//    onDateSelected: (java.time.LocalDate) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    // Generate a list of days (e.g., 2 weeks back, 2 weeks forward)
//    val days = remember(selectedDate) {
//        (-14..14).map { selectedDate.plusDays(it.toLong()) }
//    }
//
//    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 11)
//
//    LazyRow(
//        state = listState,
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(vertical = 12.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        contentPadding = PaddingValues(horizontal = 16.dp),
//    ) {
//        items(days) { date ->
//            val isSelected = date == selectedDate
//            DayItem(
//                date = date,
//                isSelected = isSelected,
//                onClick = { onDateSelected(date) }
//            )
//        }
//    }
//}
//
//@Composable
//fun DayItem(
//    date: java.time.LocalDate,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    val dayName = date.dayOfWeek.name.take(2) // e.g., "MO"
//    val dayNumber = date.dayOfMonth.toString()
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .width(50.dp)
//            .clip(MaterialTheme.shapes.medium)
//            .clickable { onClick() }
//            .background(if (isSelected) Color(0xFF2D5BFF).copy(alpha = 0.2f) else Color.Transparent)
//            .padding(vertical = 8.dp)
//    ) {
//        Text(
//            text = dayName,
//            style = MaterialTheme.typography.labelSmall,
//            color = if (isSelected) Color(0xFF2D5BFF) else Color.Gray,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(Modifier.height(4.dp))
//
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier
//                .size(32.dp)
//                .drawBehind {
//                    if (isSelected) {
//                        drawCircle(
//                            color = Color(0xFF2D5BFF),
//                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
//                        )
//                    }
//                }
//        ) {
//            Text(
//                text = dayNumber,
//                style = MaterialTheme.typography.bodyMedium,
//                color = if (isSelected) White else White.copy(alpha = 0.7f),
//                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
//            )
//        }
//    }
//}
//
//data class CalendarDay(
//    val date: java.time.LocalDate,
//    val isSelected: Boolean,
//    val hasTasks: Boolean = false // You can later link this to your Room DB
//)


