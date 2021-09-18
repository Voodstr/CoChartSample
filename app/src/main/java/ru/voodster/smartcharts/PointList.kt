package ru.voodster.smartcharts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size


/**
 * Point list
 *
 * @property xList
 * @property yList
 * @property xMin
 * @property xMax
 * @property yMin
 * @property yMax
 * @constructor Create empty Point list
 */
data class PointList(
    private val xList: List<Float>,
    private val yList: List<Float>,
    var xMin: Float,
    var xMax: Float,
    var yMin: Float,
    var yMax: Float
) {


    constructor(xList: List<Float>, yList: List<Float>) : this(
        xList, yList,
        xMin = xList.minOrNull() ?: 0f,
        xMax = xList.maxOrNull() ?: 100f,
        yMin = yList.minOrNull() ?: 0f,
        yMax = yList.maxOrNull() ?: 100f
    )


    private val pointsList = MutableList(xList.size) {
        Point(x = xList[it], y = yList[it])
    }

    fun offsetList(rectSize: Size, xMin: Float,xMax: Float,yMin: Float,yMax: Float) = MutableList(xList.size) {
        pointsList[it].pointOffset(rectSize, xMin, xMax, yMin, yMax)
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
         * Point offset
         * Calculate dot position on canvas
         * @param canvasSize
         * @param xMin
         * @param xMax
         * @param yMin
         * @param yMax
         */
        fun pointOffset(canvasSize: Size, xMin: Float,xMax: Float,yMin: Float,yMax: Float) =
            Offset(
                x.div(xMax-xMin).times(canvasSize.width),
                yMax.minus(y).div(yMax-yMin).times(canvasSize.height)
            )
    }

    data class Limits(
        var min: Float,
        var max: Float
    ) {
        fun size() = max - min
    }


}