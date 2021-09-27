/*
 Copyright 2021 Dmitriy Bychkov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

License applies to PointLisMapper class and PolygonChart
 */

package ru.voodster.smartcharts

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.voodster.smartcharts.ui.theme.SmartChartsTheme


@Composable
fun PolygonChart( //Charts with line drawn from point to point
    pointList: PointListMapper,
    modifier: Modifier,
    grid: Boolean,
    labels: Boolean
) {
    val cornerOffset = 10.dp
    val surfaceColor = MaterialTheme.colors.surface
    Surface(
        modifier = modifier.clip(RoundedCornerShape(cornerOffset)),
        color = surfaceColor,
        elevation = 8.dp
    ) {

        Drawing(pointList, modifier = modifier, grid, labels)
    }
}

@Composable
fun Drawing(
    pointListMapper: PointListMapper, modifier: Modifier,
    grid: Boolean, labels: Boolean
) {

    val dotColor = MaterialTheme.colors.primaryVariant

    val paint = Paint()
    paint.color = MaterialTheme.colors.primaryVariant.hashCode()
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 30f

    //remember state of limits causes to calculate new offsetList
    var xMinLim by remember { mutableStateOf(pointListMapper.xMinLim) }
    var xMaxLim by remember { mutableStateOf(pointListMapper.xMaxLim) }
    var yMinLim by remember { mutableStateOf(pointListMapper.yMinLim) }
    var yMaxLim by remember { mutableStateOf(pointListMapper.yMaxLim) }
    var canvasSize by remember { mutableStateOf(Size(10f, 10f)) }

    val pointList = pointListMapper.offsetList(
        rectSize = canvasSize,
        xMinLim = xMinLim,
        xMaxLim = xMaxLim,
        yMinLim = yMinLim,
        yMaxLim = yMaxLim
    )

    Box(
        modifier
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {   //resize to cover all points
                    xMaxLim = pointListMapper
                        .pointsList
                        .maxOfOrNull { it.x }
                        ?: pointListMapper.xMaxLim
                    xMinLim = pointListMapper
                        .pointsList
                        .minOfOrNull { it.x }
                        ?: pointListMapper.xMinLim
                    yMinLim = pointListMapper
                        .pointsList
                        .minOfOrNull { it.y }
                        ?: pointListMapper.yMinLim
                    yMaxLim = pointListMapper
                        .pointsList
                        .maxOfOrNull { it.y }
                        ?: pointListMapper.yMaxLim

                })
            },
        contentAlignment = Alignment.Center, true
    ) {
        val lineColor = MaterialTheme.colors.onSurface
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {   //zoom and scroll
                    detectTransformGestures { center, pan, zoom, _ ->
                        val xSize = xMaxLim - xMinLim
                        val ySize = yMaxLim - yMinLim
                        val xPoint = ((center.x / size.width) * xSize) + xMinLim
                        val yPoint =
                            (((size.height - center.y) / size.height) * ySize) + yMinLim
                        xMaxLim =
                            (xPoint + ((xMaxLim - xPoint).div(zoom))) - (pan.x / size.width) * xSize
                        xMinLim =
                            (xPoint - ((xPoint - xMinLim).div(zoom))) - (pan.x / size.width) * xSize
                        yMaxLim =
                            (yPoint + ((yMaxLim - yPoint).div(zoom))) + (pan.y / size.height) * ySize
                        yMinLim =
                            (yPoint - ((yPoint - yMinLim).div(zoom))) + (pan.y / size.height) * ySize
                    }
                }
        ) {
            if (canvasSize != size) {
                canvasSize = size
            }

            if (grid || labels) {
                pointListMapper.yGridList(paint.textSize, size, yMinLim, yMaxLim).forEach {
                    val offset = it.pointOffset(size, xMinLim, xMaxLim, yMinLim, yMaxLim)
                    val strVal = String.format("%.2f", it.y)
                    if (grid) {
                        drawLine(
                            dotColor,
                            Offset(-100f, offset.y),
                            Offset(size.width + 100f, offset.y)
                        )
                    }
                    if (labels) {
                        drawContext.canvas.nativeCanvas.drawText(
                            strVal,
                            paint.textSize,
                            offset.y,
                            paint
                        )
                    }
                }
                pointListMapper.xGridList(paint.textSize, size, xMinLim, xMaxLim).forEach {
                    val offset = it.pointOffset(size, xMinLim, xMaxLim, yMinLim, yMaxLim)
                    val strVal = String.format("%.2f", it.x)
                    if (grid) {
                        drawLine(
                            dotColor,
                            Offset(offset.x, -200f),
                            Offset(offset.x, size.height + 200f)
                        )
                    }
                    if (labels) {
                        drawContext.canvas.nativeCanvas.drawText(
                            strVal,
                            offset.x,
                            size.height + 100f + paint.textSize,
                            paint
                        )
                    }
                }
            }

            drawPoints(
                points = pointList,
                pointMode = PointMode.Polygon,
                lineColor,
                strokeWidth = 5.0f
            )
            drawPoints(
                points = pointList,
                pointMode = PointMode.Points,
                dotColor,
                strokeWidth = 10.0f
            )

        }
    }
}


@Preview("Chart")
@Preview("Chart. Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChartPreview() {
    SmartChartsTheme {
        Scaffold(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val mockX = listOf(1.0f, 8.0f, 1.0f, 16.0f, 32.0f)
                val mockY = listOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
                val pointList = PointListMapper(mockY, mockX)
                PolygonChart(
                    pointList,
                    modifier = Modifier.size(300.dp),
                    grid = true, labels = true
                )
            }
        }
    }
}

