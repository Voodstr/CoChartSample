package ru.voodster.smartcharts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size


/**
 * Point list
 *
 * @property xList
 * @property yList
 * @property xLimits
 * @property yLimits
 * @constructor Create empty Point list
 */

data class PointList(
    private val xList: List<Float>,
    private val yList: List<Float>,
    var xLimits: Limits,
    var yLimits: Limits
) {


    constructor(xList: List<Float>, yList: List<Float>) : this(
        xList, yList, xLimits = Limits(
            xList.minOrNull() ?: 0f,
            xList.maxOrNull() ?: 100f
        ),
        yLimits = Limits(
            yList.minOrNull() ?: 0f,
            yList.maxOrNull() ?: 100f
        )
    )


    private val pointsList = MutableList(xList.size) {
        Point(x = xList[it], y = yList[it])
    }

    fun offsetList(rectSize: Size) = MutableList(xList.size) {
        pointsList[it].pointOffset(rectSize, xLimits, yLimits)
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
         * @param xLimits x direction (Width) limits (min:Float,max:Float)
         * @param yLimits y direction (Height) limits (min:Float,max:Float)
         */
        fun pointOffset(canvasSize: Size, xLimits: Limits, yLimits: Limits) =
            Offset(
                x.div(xLimits.size()).times(canvasSize.width),
                yLimits.max.minus(y).div(yLimits.size()).times(canvasSize.height)
            )
    }

    data class Limits(
        var min: Float,
        var max: Float
    ) {
        fun size() = max - min
    }


}