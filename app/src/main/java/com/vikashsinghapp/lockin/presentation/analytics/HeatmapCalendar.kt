package com.vikashsinghapp.lockin.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vikashsinghapp.lockin.domain.CompletionIntensity
import com.vikashsinghapp.lockin.domain.HeatmapDaySlot
import com.vikashsinghapp.lockin.domain.HeatmapMonthGroup
import com.vikashsinghapp.lockin.domain.HeatmapWeekColumn
import com.vikashsinghapp.lockin.domain.columnIndexForDate
import com.vikashsinghapp.lockin.ui.theme.HeatmapEmpty
import com.vikashsinghapp.lockin.ui.theme.HeatmapLevel1
import com.vikashsinghapp.lockin.ui.theme.HeatmapLevel2
import com.vikashsinghapp.lockin.ui.theme.HeatmapLevel3
import com.vikashsinghapp.lockin.ui.theme.HeatmapLevel4
import com.vikashsinghapp.lockin.ui.theme.Running
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first

private val CellSize = 12.dp
private val CellGap = 4.dp
private val MonthGap = 12.dp
private val CellCornerRadius = 2.dp
private val MonthLabelHeight = 18.dp
private val GridBottomPadding = 6.dp

@Composable
fun HeatmapCalendar(
    months: List<HeatmapMonthGroup>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    scrollToDate: LocalDate = LocalDate.now(),
) {
    val listState = rememberLazyListState()
    val today = LocalDate.now()
    val gridHeight = CellSize * 7 + CellGap * 6
    val density = LocalDensity.current
    var initialScrollDone by remember { mutableStateOf(false) }

    LaunchedEffect(months) {
        if (months.isEmpty() || initialScrollDone) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.totalItemsCount }.first { it > 0 }
        scrollToVisibleDate(listState, months, scrollToDate, density)
        initialScrollDone = true
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(gridHeight + MonthLabelHeight + 8.dp + GridBottomPadding),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        itemsIndexed(
            items = months,
            key = { _, month -> month.month },
        ) { index, month ->
            Row {
                if (index > 0) {
                    Spacer(modifier = Modifier.width(MonthGap))
                }
                MonthGroupItem(
                    month = month,
                    selectedDate = selectedDate,
                    today = today,
                    onDateSelected = onDateSelected,
                )
            }
        }
    }
}

/** Scrolls the heatmap so the week column containing [date] is on-screen (centered when possible). */
private suspend fun scrollToVisibleDate(
    listState: LazyListState,
    months: List<HeatmapMonthGroup>,
    date: LocalDate,
    density: Density,
) {
    val monthIdx = months.indexOfFirst { it.month == YearMonth.from(date) }
    if (monthIdx < 0) {
        // Fallback: show the latest month if [date] is outside the grid.
        val lastIdx = months.lastIndex
        listState.scrollToItem(lastIdx)
        return
    }

    val monthGroup = months[monthIdx]
    val colIdx = monthGroup.columnIndexForDate(date).coerceAtLeast(0)
    val colWidthPx = with(density) { (CellSize + CellGap).toPx() }
    val scrollOffset = (colIdx * colWidthPx).toInt()

    listState.scrollToItem(monthIdx, scrollOffset = scrollOffset)
    snapshotFlow {
        listState.layoutInfo.visibleItemsInfo.any { it.index == monthIdx }
    }.first { it }

    val info = listState.layoutInfo
    val monthItem = info.visibleItemsInfo.find { it.index == monthIdx } ?: return
    val columnCenter = monthItem.offset + scrollOffset + colWidthPx / 2f
    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
    val adjust = columnCenter - viewportCenter
    if (adjust != 0f) {
        listState.scroll { scrollBy(adjust) }
    }
}

@Composable
private fun MonthGroupItem(
    month: HeatmapMonthGroup,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val monthWidth = monthColumnSpanWidth(month.columns.size)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(CellGap)) {
            month.columns.forEach { column ->
                WeekColumnItem(
                    column = column,
                    selectedDate = selectedDate,
                    today = today,
                    onDateSelected = onDateSelected,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(monthWidth)
                .height(MonthLabelHeight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = month.label,
                color = Color.Gray,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun WeekColumnItem(
    column: HeatmapWeekColumn,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    Column(
        modifier = Modifier.padding(bottom = GridBottomPadding),
        verticalArrangement = Arrangement.spacedBy(CellGap),
    ) {
        column.days.forEach { slot ->
            if (slot.date == null) {
                Spacer(modifier = Modifier.size(CellSize))
            } else {
                HeatmapDayCell(
                    slot = slot,
                    isSelected = slot.date == selectedDate,
                    isClickable = !slot.date.isAfter(today),
                    onClick = { onDateSelected(slot.date) },
                )
            }
        }
    }
}

@Composable
private fun HeatmapDayCell(
    slot: HeatmapDaySlot,
    isSelected: Boolean,
    isClickable: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(CellSize)
            .clip(RoundedCornerShape(CellCornerRadius))
            .background(slot.intensity.toColor())
            .then(
                if (isSelected) {
                    Modifier.border(1.dp, Running, RoundedCornerShape(CellCornerRadius))
                } else {
                    Modifier
                }
            )
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    )
}

private fun monthColumnSpanWidth(columnCount: Int): Dp {
    if (columnCount == 0) return 0.dp
    return CellSize * columnCount + CellGap * (columnCount - 1)
}

private fun CompletionIntensity.toColor() = when (this) {
    CompletionIntensity.Level0 -> HeatmapEmpty
    CompletionIntensity.Level1 -> HeatmapLevel1
    CompletionIntensity.Level2 -> HeatmapLevel2
    CompletionIntensity.Level3 -> HeatmapLevel3
    CompletionIntensity.Level4 -> HeatmapLevel4
}
