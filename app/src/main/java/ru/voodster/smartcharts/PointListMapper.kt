package ru.voodster.smartcharts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.math.RoundingMode
import java.nio.channels.FileLock
import kotlin.math.roundToInt
import kotlin.math.roundToLong


/**
 * Point list
 *
 * @property xList
 * @property yList
 * @property xMinLim
 * @property xMaxLim
 * @property yMinLim
 * @property yMaxLim
 * @constructor Create empty Point list
 */
data class PointListMapper(
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
        xMaxLim = xList.maxOrNull() ?: 100f,
        yMinLim = yList.minOrNull() ?: 0f,
        yMaxLim = yList.maxOrNull() ?: 100f
    )

    companion object{
        private const val listSize = 50
        val tempList = List(listSize){
            (0..20).random().toFloat()
        }
        val hoursList = List(listSize){index->
            index.times(0.5).toFloat()
        }
    }


    fun pointsList() = List(xList.size) {
        Point(x = xList[it], y = yList[it])
    }

    private fun pointsOnCanvas(xMin: Float, xMax: Float)=
        pointsList().filter { (xMax.plus((xMax-xMin).div(10))>it.x&&it.x>xMin.minus((xMax-xMin).div(10))) }


    fun xLabelsList(textSize:Float, precise:Float , rectSize: Size,
                    xMinLim: Float, xMaxLim: Float):List<Point>{
        val strLengthValue = "111.111".length
        val textWidth =  (textSize*strLengthValue).roundToInt()
        val maxOfLabels = (rectSize.width/textWidth).roundToInt()
        val labelStep = ((xMaxLim-xMinLim)/maxOfLabels)

        return List(maxOfLabels){ Point((round(xMinLim,precise)+(round(labelStep/2,precise))+(round(labelStep,precise)*it)),1f) }
    }

    fun yLabelsList(textSize:Float , rectSize: Size,yMinLim: Float,yMaxLim: Float):List<Point>{
        val maxOfLabels = (rectSize.height/(textSize*3)).roundToInt()
        val labelStep = ((yMaxLim-yMinLim)/maxOfLabels)
        return List(maxOfLabels){Point(1f,yMinLim+(it*labelStep))}
    }

    private fun round(value:Float, precise: Float)=value.minus(value%precise)

    fun valuableX(textSize:Float, rectSize: Size,xMinLim: Float, xMaxLim: Float):List<Point>{
        val strLengthValue = "111.111".length
        val textWidth =  (textSize*strLengthValue).roundToInt()
        val maxOfLabels = (rectSize.width/textWidth).roundToInt()
        val labelCounter = pointsOnCanvas(xMinLim, xMaxLim).size/maxOfLabels
        return pointsOnCanvas(xMinLim, xMaxLim).filterIndexed{index, _ -> (index % (labelCounter+1))==0  }
    }

    fun valuableY(textSize:Float, rectSize: Size,xMinLim: Float, xMaxLim: Float):List<Point>{
        val maxOfLabels = (rectSize.height/(textSize*3)).roundToInt()
        val labelCounter = pointsOnCanvas(xMinLim, xMaxLim).size/maxOfLabels
        return pointsOnCanvas(xMinLim, xMaxLim).filterIndexed{index, _ -> (index % (labelCounter+1))==0  }
    }


    fun offsetList(rectSize: Size,
                   xMinLim: Float, xMaxLim: Float,
                   yMinLim: Float, yMaxLim: Float
    ) = MutableList(pointsOnCanvas(xMinLim, xMaxLim).size) {
        pointsOnCanvas(xMinLim, xMaxLim)[it].pointOffset(rectSize, xMinLim, xMaxLim, yMinLim, yMaxLim)
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
         * Calculate point position on canvas
         * @param canvasSize
         * @param xMin
         * @param xMax
         * @param yMin
         * @param yMax
         */
        fun pointOffset(canvasSize: Size, xMin: Float,xMax: Float,yMin: Float,yMax: Float) =
            Offset(
                (x-xMin).div(xMax-xMin).times(canvasSize.width),
                (yMax-y).div(yMax-yMin).times(canvasSize.height)
            )
    }



}