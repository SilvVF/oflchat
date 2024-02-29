package io.silv.oflchat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.util.fastForEach
import kotlin.math.roundToInt

@Composable
fun TwoCircleAlignedMaxFillLayout(
    circles: List<@Composable () -> Unit>,
    modifier: Modifier
) {
    Layout(
        {
            circles.fastForEach { Box(Modifier.clip(CircleShape)){ it() } }
        },
        modifier = modifier,
    ) {measurables, constraints ->

        val firstTwo = measurables.take(2)
        val radius = constraints.maxWidth * 0.292893218813

        val diameter = (radius * 2).roundToInt()

        val placeables = firstTwo.map {
            it.measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxWidth = diameter,
                    maxHeight = diameter
                )
            )
        }

        val top = placeables.first()
        val bottom = placeables.last()

        layout(constraints.maxWidth, constraints.maxHeight) {
            top.placeRelative(
                y = 0,
                x = 0
            )
            bottom.placeRelative(
                x = constraints.maxWidth - diameter,
                y = constraints.maxHeight - diameter
            )
        }
    }
}



@Composable
fun ThreeCircleAlignedTangentLayout(
    circles: List<@Composable () -> Unit>,
    modifier: Modifier
) {
    Layout(
        {
            circles.fastForEach { Box(Modifier.clip(CircleShape)){ it() } }
        },
        modifier = modifier,
    ) {measurables, constraints ->

        val firstThree = measurables.take(3)

        val radius = constraints.maxHeight / 4


        val placeables = firstThree.map {
            it.measure(
                constraints.copy(
                    maxWidth = radius * 2,
                    maxHeight = radius * 2,
                    minHeight = 0,
                    minWidth = 0,
                )
            )
        }


        val top = placeables.first()
        val bottomStart = placeables[1]
        val bottomEnd = placeables.last()

        layout(constraints.maxWidth, constraints.maxHeight) {

            top.placeRelative(
                y = (constraints.maxHeight * (1f - 0.6875f) - radius).roundToInt(),
                x = constraints.maxWidth / 2 - radius
            )

            bottomStart.placeRelative(
                x = 0,
                y = constraints.maxHeight - bottomStart.height
            )
            bottomEnd.placeRelative(
                x = constraints.maxWidth - bottomEnd.width,
                y = constraints.maxHeight - bottomEnd.height
            )
        }
    }
}