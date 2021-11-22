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

import android.graphics.Paint
import android.icu.text.DecimalFormat
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun TimeSeriesChart(modifier: Modifier,
                    xList: Array<Long>, yList: Array<Float>,
                    grid: Boolean, textHeight: Float
) {
    val cornerOffset = 10.dp
    val surfaceColor = MaterialTheme.colors.surface
    val pointListMapper by remember {
        mutableStateOf(PointMapper(Array(xList.size){xList[it].toFloat()},yList))
    }

    //remember state of limits causes to calculate new offsetList
    var xMinLim by remember { mutableStateOf(pointListMapper.pointsList.minOfOrNull { it.x } ?: 0f) }
    var xMaxLim by remember { mutableStateOf(pointListMapper.pointsList.maxOfOrNull { it.x } ?: 10f) }
    var yMinLim by remember { mutableStateOf(pointListMapper.pointsList.minOfOrNull { it.y } ?: 0f) }
    var yMaxLim by remember { mutableStateOf(pointListMapper.pointsList.maxOfOrNull { it.y } ?: 10f) }

    val _showedList = MutableStateFlow(arrayListOf<Offset>())
    val showedList = _showedList.asStateFlow()
    val drawList = showedList.collectAsState()

    var redraw by remember { mutableStateOf(false) }
    val offsetList = ArrayList<Offset>()

    LaunchedEffect(key1 = redraw) {
        val chunkedList = pointListMapper.pointsOnCanvas(xMinLim, xMaxLim).chunked(100)
        val futureList = List(size = chunkedList.size) { chunk_ind ->
            async {
                List(chunkedList[chunk_ind].size) { point_ind ->
                    chunkedList[chunk_ind][point_ind].pointOffset(
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
                    val xPoint = ((center.x / size.width) * xSize) + xMinLim
                    xMaxLim =
                        (xPoint + ((xMaxLim - xPoint).div(zoom))) - (pan.x / size.width) * xSize
                    xMinLim =
                        (xPoint - ((xPoint - xMinLim).div(zoom))) - (pan.x / size.width) * xSize
                    //yMaxLim = pointListMapper.pointsOnCanvas(xMinLim,xMaxLim).maxOfOrNull { it.y }?:10f
                    //yMinLim = pointListMapper.pointsOnCanvas(xMinLim,xMaxLim).minOfOrNull { it.y }?:0f
                    redraw = !redraw
                }
            },
        color = surfaceColor,
        elevation = 8.dp
    ) {
        Box(
            Modifier
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
                    }, onTap = {offset ->
                        //TODO onTap
                    })
                },
            contentAlignment = Alignment.Center, true
        ) {
            val lineColor = MaterialTheme.colors.onSurface
            val dotColor = MaterialTheme.colors.primaryVariant

            val paint = Paint()
            paint.color = MaterialTheme.colors.primaryVariant.hashCode()
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = textHeight

            var canvasSize by remember { mutableStateOf(Size(10f, 10f)) }
            var xLabelsCount = (canvasSize.width / ("111.111".length * paint.textSize)).roundToInt()
            var yLabelsCount = (canvasSize.height / (3 * paint.textSize).toInt()).roundToInt()

            Box(
                Modifier.padding(textHeight.dp), contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (canvasSize != size) {
                        canvasSize = size
                        xLabelsCount =
                            (canvasSize.width / ("111.111".length * paint.textSize)).roundToInt()
                        yLabelsCount = (canvasSize.height / (3 * paint.textSize).toInt()).roundToInt()
                        pointListMapper.canvasSize = size
                    }

                    if (grid){
                        pointListMapper.gridList(
                            yLabelsCount,
                            yMinLim,
                            yMaxLim,
                            PointMapper.Axis.Vertical
                        ).forEach {
                            val offset = it.pointOffset(size, xMinLim, xMaxLim, yMinLim, yMaxLim)
                            val decimalF = DecimalFormat()
                            decimalF.maximumSignificantDigits=4
                            val strVal = decimalF.format(it.y)
                            drawLine(
                                dotColor,
                                Offset(-100f, offset.y),
                                Offset(size.width + 100f, offset.y)
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                strVal,
                                -2*textHeight,
                                offset.y,
                                paint
                            )
                        }
                    }

                    if (grid){
                        val gridList = Array(xLabelsCount+1){""}
                        gridList[1] = "1231"
                        val timeLineSize = (xMaxLim - xMinLim).roundToLong()
                        when(timeLineSize){
                            in 0..(5*1000)->{}//mills
                            in (5*1001)..(5*60*1000)->{}//seconds
                            in (5*60*1000)..(2*60*60*1000)->{}//minutes
                            in (2*60*60*1000)..(2*24*60*60*1000)->{}//hours
                            else ->{}//date
                        }
                        pointListMapper.gridList(
                            xLabelsCount,
                            xMinLim,
                            xMaxLim,
                            PointMapper.Axis.Horizontal
                        ).forEach {
                            val offset = it.pointOffset(size, xMinLim, xMaxLim, yMinLim, yMaxLim)
                            val dcmf = DecimalFormat()
                            dcmf.maximumSignificantDigits=4
                            val strVal = dcmf.format(it.x)
                            drawLine(
                                dotColor,
                                Offset(offset.x, -100f),
                                Offset(offset.x, size.height+100f)
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                strVal,
                                offset.x-1.2f*textHeight,
                                size.height + paint.textSize,
                                paint
                            )
                        }
                    }
                    drawPoints(
                        points = drawList.value,
                        pointMode = PointMode.Polygon,
                        lineColor,
                        strokeWidth = 5.0f
                    )
                    drawPoints(
                        points = drawList.value,
                        pointMode = PointMode.Points,
                        dotColor,
                        strokeWidth = 10.0f
                    )
                }
            }
        }
    }
}




