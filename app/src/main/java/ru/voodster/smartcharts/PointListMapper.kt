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

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * Point list mapper class
 * Translates given points with given limits to points on Canvas
 * xList size should be same as yList
 *
 * @property xList
 * @property yList
 * @property xMinLim
 * @property xMaxLim
 * @property yMinLim
 * @property yMaxLim
 * @constructor Create empty Point list
 */
class PointListMapper(
    private val xList: List<Float>,
    private val yList: List<Float>,
    var xMinLim: Float,
    var xMaxLim: Float,
    var yMinLim: Float,
    var yMaxLim: Float
) {

    constructor(xList: List<Float>, yList: List<Float>) : this(
        xList, yList,
        xMinLim = xList.minOrNull() ?: 0f,
        xMaxLim = xList.maxOrNull() ?: 10f,
        yMinLim = yList.minOrNull() ?: 0f,
        yMaxLim = yList.maxOrNull() ?: 10f
    ){
        if (yMinLim==yMaxLim){
            yMinLim-=yMinLim.div(2)
            yMaxLim+=yMaxLim.div(2)
        }
        if (xMinLim==xMaxLim){
            xMinLim-=xMinLim.div(2)
            xMaxLim+=xMaxLim.div(2)
        }
        //
    }

    init {
        if (xList.size != yList.size) throw IndexOutOfBoundsException("Lists (xList and yList) should be same size")
    }


    /**
     * Point - creates [pointOffset]
     * @property x - x value
     * @property y
     * @constructor Create empty Point
     */


    data class Point(
        val x: Float,
        val y: Float
    ) {
        /**
         * Point offset -
         * Calculates point position on canvas
         * @param canvasSize
         * @param xMin
         * @param xMax
         * @param yMin
         * @param yMax
         */
        fun pointOffset(canvasSize: Size, xMin: Float, xMax: Float, yMin: Float, yMax: Float) =
            Offset(
                (x - xMin).div(xMax - xMin).times(canvasSize.width),
                (yMax - y).div(yMax - yMin).times(canvasSize.height)
            )
        suspend fun susPointOffset(canvasSize: Size, xMin: Float, xMax: Float, yMin: Float, yMax: Float) =
            Offset(
                (x - xMin).div(xMax - xMin).times(canvasSize.width),
                (yMax - y).div(yMax - yMin).times(canvasSize.height)
            )

    }

    @Composable
    fun livePointOffset(canvasSize: Size, xMin: Float, xMax: Float, yMin: Float, yMax: Float) =
        offsetList(canvasSize, xMin, xMax, yMin, yMax)

    /**
     * Points list
     *
     */
    val pointsList = List(xList.size) {
        Point(x = xList[it], y = yList[it])
    }

    /**
     * Points on canvas - currently showed points on canvas
     * @param xMin
     * @param xMax
     */
    private fun pointsOnCanvas(xMin: Float, xMax: Float) =
        pointsList.filter {
            (xMax.plus((xMax - xMin).div(10)) > it.x && it.x > xMin.minus((xMax - xMin).div(10)))
        }

    /**
     * Offset list for canvas to draw
     *
     *
     * @param rectSize
     * @param xMinLim
     * @param xMaxLim
     * @param yMinLim
     * @param yMaxLim
     */


    fun offsetList(
        rectSize: Size,
        xMinLim: Float, xMaxLim: Float,
        yMinLim: Float, yMaxLim: Float
    ) = List(pointsOnCanvas(xMinLim, xMaxLim).size) {
        pointsOnCanvas(xMinLim, xMaxLim)[it].pointOffset(
            rectSize,
            xMinLim, xMaxLim,
            yMinLim, yMaxLim
        )
    }

    suspend fun susOffsetList(
        rectSize: Size,
        xMinLim: Float, xMaxLim: Float,
        yMinLim: Float, yMaxLim: Float
    ) = List(pointsOnCanvas(xMinLim, xMaxLim).size) {
        pointsOnCanvas(xMinLim, xMaxLim)[it].susPointOffset(
            rectSize,
            xMinLim, xMaxLim,
            yMinLim, yMaxLim
        )
    }



    fun isPointVisible(
        startPoint: Offset, endPoint: Offset,
    ) =
        hypot((startPoint.x-endPoint.x),(startPoint.y-endPoint.y)).pow(0.5f) > 3f


    fun visibleOffsets(
        rectSize: Size,
        xMinLim: Float, xMaxLim: Float,
        yMinLim: Float, yMaxLim: Float):List<Offset> {
        val visibleList: ArrayList<Offset> = arrayListOf()
        var previous = Offset(1f, 1f)
        offsetList(rectSize, xMinLim, xMaxLim, yMinLim, yMaxLim).forEachIndexed { index, offset ->
            if (index == 0) {
                previous = offset
                visibleList.add(offset)
            } else {
                if (isPointVisible(previous, offset)) {
                    previous = offset
                    visibleList.add(offset)
                }
            }
        }
        return visibleList
    }


    /**
     * X grid list
     * List of X values for grid
     *
     * @param textSize - size of text label
     * @param rectSize - canvas size
     * @param xMinLim
     * @param xMaxLim
     * @return
     */
    fun xGridList(
        textSize: Float, rectSize: Size,
        xMinLim: Float, xMaxLim: Float
    ): List<Point> {
        val precise = 0.001f
        val strLengthValue = "111.111".length
        val textWidth = (textSize * strLengthValue).roundToInt()
        val maxOfLabels = (rectSize.width / textWidth).roundToInt()
        val labelStep = ((xMaxLim - xMinLim) / maxOfLabels)

        return List(maxOfLabels) {
            Point(
                (round(xMinLim, precise) + (round(
                    labelStep / 2,
                    precise
                )) + (round(labelStep, precise) * it)), 1f
            )
        }
    }

    /**
     * Y grid list
     * List of Y values for grid
     *
     * @param textSize - size of text label
     * @param rectSize - canvas size
     * @param yMinLim
     * @param yMaxLim
     * @return
     */
    fun yGridList(textSize: Float, rectSize: Size, yMinLim: Float, yMaxLim: Float): List<Point> {
        val maxOfLabels = (rectSize.height / (textSize * 3)).roundToInt()
        val labelStep = ((yMaxLim - yMinLim) / maxOfLabels)
        return List(maxOfLabels+1) { Point(1f, yMinLim + (it * labelStep)) }
    }

    /**
     * Round TODO - Round labels
     *
     * @param value
     * @param precise
     */
    private fun round(value: Float, precise: Float) = value.minus(value % precise)

}