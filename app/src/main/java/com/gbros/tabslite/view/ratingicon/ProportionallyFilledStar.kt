package com.gbros.tabslite.view.ratingicon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun ProportionallyFilledStar(
    fillPercentage: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Unfilled star (background)
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.background,
        )

        // Filled portion of the star
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RectShape(fillPercentage))
        )
    }
}

private class RectShape(private val fillPercentage: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width * fillPercentage,
                bottom = size.height
            )
        )
    }
}

@Composable @Preview
private fun ProportionallyFilledStarPreview(){
    AppTheme {
        Column {
            RatingIcon(5.0)
            RatingIcon(4.9)
            RatingIcon(4.7)
            RatingIcon(4.1)
            RatingIcon(3.5)
            RatingIcon(2.5)
            RatingIcon(0.9)
            RatingIcon(0.5)
            RatingIcon(0.1)
            RatingIcon(0.0)
        }
    }
}