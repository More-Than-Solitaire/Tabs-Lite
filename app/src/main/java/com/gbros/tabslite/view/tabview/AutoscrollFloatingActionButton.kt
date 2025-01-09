package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

/**
 * AutoScroll floating action button.  Play button shows when paused, and speed slider disappears.  Pause
 * button shows when playing, and speed slider appears.  When play button is clicked, onPlay is called
 * with the current delay.  When speed is changed while playing, [onValueChange] is called with the
 * new delay.  When pause button is clicked, onPause is called.  Up on the screen will be a lower value.
 *
 */
@Composable
fun AutoscrollFloatingActionButton(
    sliderValue: Float,
    paused: Boolean,
    onValueChange: (sliderPosition: Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onButtonClick: () -> Unit,
    alignment: Alignment = Alignment.BottomEnd,
    padding: Dp = 16.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val buttonIsTouched by interactionSource.collectIsPressedAsState()
    var sliderIsTouched by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .alpha(if (buttonIsTouched || sliderIsTouched || paused) 1f else 0.5f)
                .align(alignment)
                .padding(all = padding)

        ) {
            if (!paused) {
                // vertical slider thanks https://stackoverflow.com/a/71129399/3437608
                Slider(
                    value = sliderValue,
                    valueRange = 0f..1f,
                    onValueChange = { newValue ->
                        sliderIsTouched = true
                        onValueChange(newValue)
                    },
                    onValueChangeFinished = {
                        sliderIsTouched = false
                        onValueChangeFinished()
                    },
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = 270f
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(
                                Constraints(
                                    minWidth = constraints.minHeight,
                                    maxWidth = constraints.maxHeight,
                                    minHeight = constraints.minWidth,
                                    maxHeight = constraints.maxHeight,
                                )
                            )
                            layout(placeable.height, placeable.width) {
                                placeable.place(-placeable.width, 0)
                            }
                        }
                        .width(200.dp)
                        .height(54.dp)

                )
            }

            FloatingActionButton(
                onClick = onButtonClick,
                interactionSource = interactionSource
            ) {
                if (paused) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = stringResource(R.string.generic_action_play))
                } else {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_pause),
                        contentDescription = stringResource(R.string.generic_action_pause)
                    )
                }
            }
        }
    }
}


@Composable @Preview
private fun AutoscrollFloatingActionButtonPreview() {
    AppTheme {
        AutoscrollFloatingActionButton(
            sliderValue = 0.5f,
            paused = false,
            onValueChange = {},
            onButtonClick = {},
            onValueChangeFinished = {}
        )
    }
}