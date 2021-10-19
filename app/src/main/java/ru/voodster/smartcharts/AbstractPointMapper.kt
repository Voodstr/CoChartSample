import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.*
import ru.voodster.smartcharts.PointListMapper
import kotlin.math.roundToInt

abstract class AbstractPointMapper(xList:Array<Float>, yList:Array<Float>) {



    enum class Axis{
        Vertical,
        Horizontal
    }

    abstract fun checkErrors()

    init{
        checkErrors()
    }

    data class Point(
        val x: Float,
        val y: Float
    ) {
        var pointOffset = Offset(0f,0f)
        /**
         * Point offset -
         * Calculates point position on canvas
         * @param canvasSize
         * @param xMin
         * @param xMax
         * @param yMin
         * @param yMax
         */
        fun pointOffset(canvasSize: Size, xMin: Float, xMax: Float, yMin: Float, yMax: Float):Offset {
            pointOffset = Offset(
                (x - xMin).div(xMax - xMin).times(canvasSize.width),
                (yMax - y).div(yMax - yMin).times(canvasSize.height)
            )
            return pointOffset
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

    @Composable
    fun offsetList123(
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
    fun offsetList(    rectSize: Size,
                 xMinLim: Float, xMaxLim: Float,
                 yMinLim: Float, yMaxLim: Float):ArrayList<Offset>{
        val result = arrayListOf<Offset>()
        LaunchedEffect(key1 = xMinLim){
            pointsOnCanvas(xMinLim,xMaxLim).forEach {
                val data = async { it.pointOffset(rectSize,xMinLim, xMaxLim,yMinLim,yMaxLim) }
                result.add(data.await())
            }
        }
        return result
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
    fun gridList(maxOfLabels:Int,min:Float,max:Float,axis:Axis):List<Point>{
        val labelStep = (max-min)/maxOfLabels
        return when (axis){
            Axis.Vertical->{
                List(maxOfLabels){Point(0f,min+it*labelStep)}
            }
            Axis.Horizontal->{
                List(maxOfLabels){Point(min+it*labelStep,0f)}
            }
        }
    }


}