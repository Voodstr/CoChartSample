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

import AbstractPointMapper
import android.content.res.Configuration
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.voodster.smartcharts.ui.theme.SmartChartsTheme
import kotlin.math.roundToInt

@Composable
fun Drawing(
    points: ArrayList<Offset>, modifier: Modifier,
    xMinLim: Float, xMaxLim: Float, yMinLim: Float, yMaxLim: Float,
    gridX: Boolean, gridY: Boolean,
    pointListMapper: AbstractPointMapper,
    textSize:Float
) {
    Log.d("Drawing", "Drawing start")
    val lineColor = MaterialTheme.colors.onSurface
    val dotColor = MaterialTheme.colors.primaryVariant
    val paint = Paint()
    paint.color = MaterialTheme.colors.primaryVariant.hashCode()
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = textSize

    var canvasSize by remember { mutableStateOf(Size(10f, 10f)) }
    var xLabelsCount = (canvasSize.width / ("111.111".length * paint.textSize)).roundToInt()
    var yLabelsCount = (canvasSize.height / (3 * paint.textSize).toInt()).roundToInt()

    Box(
        modifier.fillMaxSize(), contentAlignment = Alignment.Center, true
    ) {
        Canvas(modifier = modifier.matchParentSize()) {
            Log.d("Drawing", "Canvas start")
            if (canvasSize != size) {
                canvasSize = size
                xLabelsCount =
                    (canvasSize.width / ("111.111".length * paint.textSize)).roundToInt()
                yLabelsCount = (canvasSize.height / (3 * paint.textSize).toInt()).roundToInt()
                pointListMapper.canvasSize = size
            }

            if (gridY){
                pointListMapper.gridList(
                    yLabelsCount,
                    yMinLim,
                    yMaxLim,
                    AbstractPointMapper.Axis.Vertical
                ).forEach {
                    val offset = it.pointOffset(size, xMinLim, xMaxLim, yMinLim, yMaxLim)
                    val strVal = String.format("%.2f", it.y)
                    drawLine(
                        dotColor,
                        Offset(-100f, offset.y),
                        Offset(size.width + 100f, offset.y)
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        strVal,
                        paint.textSize,
                        offset.y,
                        paint
                    )
                }
            }

            if (gridX){
                pointListMapper.gridList(
                    xLabelsCount,
                    xMinLim,
                    xMaxLim,
                    AbstractPointMapper.Axis.Horizontal
                ).forEach {
                    val offset = it.pointOffset(size, xMinLim, xMaxLim, yMinLim, yMaxLim)
                    val strVal = String.format("%.2f", it.x)
                    drawLine(
                        dotColor,
                        Offset(offset.x, -100f),
                        Offset(offset.x, size.height)
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        strVal,
                        offset.x,
                        size.height - paint.textSize / 2,
                        paint
                    )
                }
            }

            drawPoints(
                points = points,
                pointMode = PointMode.Polygon,
                lineColor,
                strokeWidth = 5.0f
            )
            drawPoints(
                points = points,
                pointMode = PointMode.Points,
                dotColor,
                strokeWidth = 10.0f
            )
            Log.d("Drawing", "Canvas end")
        }
    }
    Log.d("Drawing", "Drawing end")
}


@Composable
fun PolygonChart(
    pointListMapper: AbstractPointMapper, modifier: Modifier,
    grid: Boolean,  textSize: Float
) {
    val cornerOffset = 10.dp
    val surfaceColor = MaterialTheme.colors.surface

    //remember state of limits causes to calculate new offsetList
    var xMinLim by remember { mutableStateOf(0f) }
    var xMaxLim by remember { mutableStateOf(10f) }
    var yMinLim by remember { mutableStateOf(0f) }
    var yMaxLim by remember { mutableStateOf(10f) }

    //TODO need to change this for smth more usefull
    val _showedList = MutableStateFlow(arrayListOf<Offset>())
    val showedList = _showedList.asStateFlow()
    val drawlist = showedList.collectAsState()

    var redraw by remember { mutableStateOf(false) }
    val offsetList = ArrayList<Offset>()

    LaunchedEffect(key1 = redraw) {
        val chunkedList = pointListMapper.pointsOnCanvas(xMinLim, xMaxLim).chunked(100)
        val futureList = List(size = chunkedList.size) { chunk_ind ->
            async {
                List(chunkedList[chunk_ind].size) { point_ind ->
                    chunkedList[chunk_ind][point_ind].asyncPointOffset(
                        pointListMapper.canvasSize,
                        xMinLim,
                        xMaxLim,
                        yMinLim,
                        yMaxLim
                    )
                }
            }
        }
        offsetList.clear()
        futureList.forEach { def ->
            def.await().forEach { offset -> offsetList.add(offset) }
        }
        _showedList.update { offsetList }
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerOffset))
            .pointerInput(Unit) {
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
                    redraw = !redraw
                }
            },
        color = surfaceColor,
        elevation = 8.dp
    ) {
        Box(
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {   //resize to cover all points
                        xMaxLim = pointListMapper.pointsList.maxOfOrNull { it.x }
                            ?: 10f
                        xMinLim = pointListMapper.pointsList.minOfOrNull { it.x }
                            ?: 0f
                        yMinLim = pointListMapper.pointsList.minOfOrNull { it.y }
                            ?: 0f
                        yMaxLim = pointListMapper.pointsList.maxOfOrNull { it.y }
                            ?: 10f
                        redraw = !redraw
                    })
                },
            contentAlignment = Alignment.Center, true
        ) {

                Drawing(
                    points = drawlist.value,
                    modifier = Modifier.fillMaxSize(),
                    xMinLim = xMinLim,
                    xMaxLim = xMaxLim,
                    yMinLim = yMinLim,
                    yMaxLim = yMaxLim,
                    gridX = grid,
                    gridY = grid,
                    pointListMapper = pointListMapper,
                    textSize = textSize
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
                val mockX = arrayOf(1.0f, 8.0f, 1.0f, 16.0f, 32.0f)
                val mockY = arrayOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
                val pointList = AbstractPointMapper(mockY, mockX)
                PolygonChart(
                    pointList,
                    modifier = Modifier.size(300.dp),
                    grid = true,  30f
                )
            }
        }
    }
}

