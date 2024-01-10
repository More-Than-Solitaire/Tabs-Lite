package com.gbros.tabslite.compose.ratingicon

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun HalfStarIcon(filledColor: Color = MaterialTheme.colorScheme.primary, emptyColor: Color = MaterialTheme.colorScheme.background) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_rating_star_left_half),
            contentDescription = "Half star",
            tint = filledColor,
        )
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_rating_star_right_half),
            contentDescription = null,
            tint = emptyColor,

        )
    }
}

@Composable @Preview
private fun HalfStarIconPreview() {
    AppTheme {
        HalfStarIcon()
    }
}