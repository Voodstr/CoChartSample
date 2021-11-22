package ru.voodster.smartcharts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class PointMapper(private val xList: Array<Float>, private val yList: Array<Float>) {

    var canvasSize  = Size(10f,10f)


    enum class Axis {
        Vertical,
        Horizontal
    }

    private fun checkErrors() {
        if(xList.size!=yList.size) throw IndexOutOfBoundsException("Arrays should be same size")
    }

    init {
        checkErrors()
    }

    data class Point(
        val x: Float,
        val y: Float
    ) {
        var pOffset = Offset(0f, 0f)

        private fun xOffset(canvasSize: Size, xMin: Float, xMax: Float) =
            (x - xMin).div(xMax - xMin).times(canvasSize.width)

        private fun yOffset(canvasSize: Size, yMin: Float, yMax: Float) =
            (yMax - y).div(yMax - yMin).times(canvasSize.height)

        /**
         * Point offset -
         * Calculates point position on canvas
         * @param canvasSize
         * @param xMin
         * @param xMax
         * @param yMin
         * @param yMax
         */
        fun pointOffset(canvasSize: Size, xMin: Float, xMax: Float, yMin: Float, yMax: Float):Offset{
           pOffset =  Offset(
                xOffset(canvasSize, xMin, xMax),
                yOffset(canvasSize, yMin, yMax)
            )
            return pOffset
        }
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
            (xMax.plus((xMax - xMin).div(4)) > it.x && it.x > xMin.minus((xMax - xMin).div(4)))
        }


    /**
     * Grid list
     *
     * @param maxOfLabels
     * @param min
     * @param max
     * @param axis
     * @return
     */
    fun gridList(maxOfLabels: Int, min: Float, max: Float, axis: Axis): List<Point> {
        val labelStep = (max - min) / maxOfLabels
        return when (axis) {
            Axis.Vertical -> {
                List(maxOfLabels+1) { Point(0f, min + it * labelStep) }
            }
            Axis.Horizontal -> {
                List(maxOfLabels+1) { Point(min + it * labelStep, 0f) }
            }
        }
    }


}