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
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * @constructor Create empty Point list
 */
class PointListMapper(
    private val xList: Array<Float>,
    private val yList: Array<Float>,
    private val dateList: Array<Long>,
    private val multiList: Array<Array<Float>>,
    private val mode:ListMode
) {


    enum class Axis{
        Vertical,
        Horizontal
    }

    enum class ListMode {
        Float,
        TimeSeries,
        MultiSeries
    }

    constructor(xList: Array<Float>, yList: Array<Float>) : this(
        xList, yList, arrayOf(), arrayOf(),ListMode.Float
    )

    constructor(xList: Array<Float>, dateList: Array<Long>) : this(
        xList, arrayOf(), dateList, arrayOf(),ListMode.TimeSeries
    )
    constructor(multiList: Array<Array<Float>>, dateList: Array<Long>) : this(
        arrayOf(), arrayOf(), dateList, multiList,ListMode.MultiSeries
    )


    private fun checkErrors(){
        when(mode){
            ListMode.Float->{
                if(xList.size!=yList.size) throw IndexOutOfBoundsException("Arrays should be same size")
            }
            ListMode.TimeSeries->{
                if(xList.size!=dateList.size) throw IndexOutOfBoundsException("Arrays should be same size")
            }
            ListMode.MultiSeries->{
                multiList.forEach {
                    if (it.size!=dateList.size)throw IndexOutOfBoundsException("Arrays should be same size")
                }
            }
        }
    }


    /*

     */
    init {
        checkErrors()
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
    }

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
    fun pointsOnCanvas(xMin: Float, xMax: Float) =
        pointsList.filter {
            (xMax.plus((xMax - xMin).div(10)) > it.x && it.x > xMin.minus((xMax - xMin).div(10)))
        }

    @Composable
    fun canvasPoints(xMin: Float, xMax: Float,yMin: Float,yMax: Float) =
        pointsList.filter {
            (
                    xMax.plus((xMax - xMin).div(10)) > it.x
                            &&
                    it.x > xMin.minus((xMax - xMin).div(10)))
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


    @Composable
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


    private suspend fun offsetSequence(rectSize: Size,
                                       xMinLim: Float, xMaxLim: Float,
                                       yMinLim: Float, yMaxLim: Float):Sequence<Offset> = sequence{
        pointsOnCanvas(xMinLim,xMaxLim).forEach {
            yield(it.pointOffset(rectSize,xMinLim, xMaxLim,yMinLim,yMaxLim))
        }
    }

    @Composable
    fun testOffsetList(rectSize: Size,
                               xMinLim: Float, xMaxLim: Float,
                               yMinLim: Float, yMaxLim: Float):List<Offset>{
        val list = mutableListOf<Offset>()
        val curList = pointsOnCanvas(xMinLim,xMaxLim)
        val deferredList = mutableListOf<Deferred<Offset>>()
        val scope = rememberCoroutineScope()
        curList.forEach { point->
            deferredList.add(scope.async {
                point.pointOffset(rectSize,xMinLim, xMaxLim,yMinLim,yMaxLim)
            })
        }
        deferredList.forEach {
            scope.launch {
                list.add(it.await())
            }
        }
        Log.d("testOffsetList",list.toString())
        return list
    }

    fun deferedList(scope: CoroutineScope, rectSize: Size,
                    xMinLim: Float, xMaxLim: Float,
                    yMinLim: Float, yMaxLim: Float) :Deferred<List<Offset>> {
        return scope.async{
            List(pointsOnCanvas(xMinLim,xMaxLim).size){ Offset(0f,0f)}
        }
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