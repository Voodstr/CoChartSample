package ru.voodster.smartcharts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.util.*
import kotlin.math.roundToInt


/**
 * Point list mapper class
 * Translates given values to points on Canvas
 * @property xList
 * @property yList
 * @property dateList
 * @property mode
 * @constructor Create empty Point list mapper
 */
class PointListMapper(
        private val xList: Array<Float>,
        private val yList: Array<Float>,
        private val multiList:Array<Array<Float>>,
        private val dateList: Array<Long>,
        val mode: ListMode
) {

    constructor(xList: Array<Float>, yList: Array<Float>)
            : this(xList, yList, arrayOf(), arrayOf(),ListMode.Float)

    constructor(yList: Array<Float>, dateList: Array<Long>)
            : this(arrayOf(), yList, arrayOf(), dateList,ListMode.SingleListOnDate)

    constructor(multiList: Array<Array<Float>>, dateList: Array<Long>)
            : this(arrayOf(), arrayOf(), multiList, dateList,ListMode.MultiListOnDate)


    init {
        checkErrors()
    }

    enum class ListMode {
        Float,
        SingleListOnDate,
        MultiListOnDate
    }

    val xMinLim = xList.minOrNull() ?: 0f
    val xMaxLim = xList.maxOrNull() ?: 100f
    val yMinLim = yList.minOrNull() ?: 0f
    val yMaxLim = yList.maxOrNull() ?: 100f
    val dateMinLim = dateList.minOrNull() ?: Calendar.getInstance().timeInMillis - 1000
    val dateMaxLim = dateList.maxOrNull() ?: Calendar.getInstance().timeInMillis


    private fun checkErrors() {
        when (mode) {
            ListMode.Float -> if (xList.size != yList.size)
                throw IndexOutOfBoundsException("Lists (xList and yList) should be same size")
            ListMode.SingleListOnDate -> if (xList.size != dateList.size)
                throw IndexOutOfBoundsException("Lists (xList and DateList) should be same size")
            ListMode.MultiListOnDate -> if (xList.size != dateList.size && yList.size != dateList.size)
                throw IndexOutOfBoundsException("Lists (xList and yList) should be same size as DateList")
        }
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
     * Float points list
     *
     */
    fun floatPointsList() = List(xList.size) {
        Point(x = xList[it], y = yList[it])
    }

    fun datePointsList() = List(dateList.size){
        Point(x= yList[it],y=dateList[it].toFloat())
    }

    /**
     * Points on canvas - currently showed points on canvas
     * @param min
     * @param max
     */
    private fun pointsOnCanvas(min: Float, max: Float) =
            floatPointsList().filter { (max.plus((max - min).div(10)) > it.x && it.x > min.minus((max - min).div(10))) }

    private fun extPointsOnCanvas(min: Float, max: Float,list: List<Point>) =
           list.filter { (max.plus((max - min).div(10)) > it.x && it.x > min.minus((max - min).div(10))) }

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

    fun offsetList(rectSize: Size,
                   xMinLim: Float, xMaxLim: Float,
                   yMinLim: Float, yMaxLim: Float
    ) = List(pointsOnCanvas(xMinLim, xMaxLim).size) {
        pointsOnCanvas(xMinLim, xMaxLim)[it].pointOffset(rectSize, xMinLim, xMaxLim, yMinLim, yMaxLim)
    }

    fun  extOffsetList(){}

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
    fun xGridList(textSize: Float, rectSize: Size,
                  xMinLim: Float, xMaxLim: Float): List<Point> {
        val precise = 0.001f
        val strLengthValue = "111.111".length
        val textWidth = (textSize * strLengthValue).roundToInt()
        val maxOfLabels = (rectSize.width / textWidth).roundToInt()
        val labelStep = ((xMaxLim - xMinLim) / maxOfLabels)

        return List(maxOfLabels) { Point((round(xMinLim, precise) + (round(labelStep / 2, precise)) + (round(labelStep, precise) * it)), 1f) }
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
        return List(maxOfLabels) { Point(1f, yMinLim + (it * labelStep)) }
    }

    /**
     * Round TODO - Round labels
     *
     * @param value
     * @param precise
     */
    private fun round(value: Float, precise: Float) = value.minus(value % precise)

    /*
    /**
     * Valuable x
     *
     * @param textSize
     * @param rectSize
     * @param xMinLim
     * @param xMaxLim
     * @return
     */
    fun valuableX(textSize:Float, rectSize: Size,xMinLim: Float, xMaxLim: Float):List<Point>{
        val strLengthValue = "111.111".length
        val textWidth =  (textSize*strLengthValue).roundToInt()
        val maxOfLabels = (rectSize.width/textWidth).roundToInt()
        val labelCounter = pointsOnCanvas(xMinLim, xMaxLim).size/maxOfLabels
        return pointsOnCanvas(xMinLim, xMaxLim).filterIndexed{index, _ -> (index % (labelCounter+1))==0  }
    }

    /**
     * Valuable y
     *
     * @param textSize
     * @param rectSize
     * @param xMinLim
     * @param xMaxLim
     * @return
     */
    fun valuableY(textSize:Float, rectSize: Size,xMinLim: Float, xMaxLim: Float):List<Point>{
        val maxOfLabels = (rectSize.height/(textSize*3)).roundToInt()
        val labelCounter = pointsOnCanvas(xMinLim, xMaxLim).size/maxOfLabels
        return pointsOnCanvas(xMinLim, xMaxLim).filterIndexed{index, _ -> (index % (labelCounter+1))==0  }
    }

     */

}