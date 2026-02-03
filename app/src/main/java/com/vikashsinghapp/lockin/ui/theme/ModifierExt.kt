package com.vikashsinghapp.lockin.ui.theme

import androidx.compose.ui.Modifier

// to apply modifier depending on available space
fun Modifier.mediaQuery(
    comparator: Boolean,
    whenComparatorIsTrue: Modifier = Modifier,
    whenComparatorIsFalse: Modifier = Modifier
): Modifier =
    // then() Concatenates previous Modifier
    this.then(
        if (comparator) {
            whenComparatorIsTrue
        } else {
            whenComparatorIsFalse
        }
    )