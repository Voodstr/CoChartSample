package ru.voodster.smartcharts

import android.content.res.Configuration
import android.graphics.Color
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
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.voodster.smartcharts.ui.theme.SmartChartsTheme
import kotlin.math.nextDown
import kotlin.math.roundToInt


@Composable
fun PolygonChartLabeled(
    xList: List<Float>, yList: List<Float>,
    modifier: Modifier,
    labelFontSize: TextUnit
) {
    val pointList = PointListMapper(xList, yList)
    //val scope = rememberCoroutineScope()
    val cornerOffset = 10.dp
    val surfaceColor = MaterialTheme.colors.surface
    Surface(
        modifier = modifier.clip(RoundedCornerShape(cornerOffset)),
        color = surfaceColor,
        elevation = 8.dp
    ) {

        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(5.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1.0f, fill = true), horizontalArrangement = Arrangement.Start
            ) {
                Start(
                    Modifier
                        .weight(0.1f, fill = true)
                        .fillMaxHeight(),
                    yList,
                    labelFontSize
                )
                Drawing(pointList, modifier = modifier)

            }
            Bottom(
                Modifier
                    .fillMaxWidth()
                    .weight(0.1f, fill = true),
                xList,
                labelFontSize
            )
        }
    }
}


@Composable
fun PolygonChart(
    xList: List<Float>, yList: List<Float>,
    modifier: Modifier,
    labelFontSize: TextUnit
) {
    //val scope = rememberCoroutineScope()
    val pointList = PointListMapper(xList, yList)
    val cornerOffset = 10.dp
    val surfaceColor = MaterialTheme.colors.surface
    Surface(
        modifier = modifier.clip(RoundedCornerShape(cornerOffset)),
        color = surfaceColor,
        elevation = 8.dp
    ) {
        Drawing(pointList, modifier = modifier)
    }
}


@Composable
fun Bottom(modifier: Modifier, xList: List<Float>, fontSize: TextUnit) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = xList.minOrNull().toString(), fontSize = fontSize)
        Text(text = xList.maxOrNull().toString(), fontSize = fontSize)
    }
}

@Composable
fun Start(modifier: Modifier, yList: List<Float>, fontSize: TextUnit) {
    Column(modifier, verticalArrangement = Arrangement.SpaceBetween) {
        Text(text = yList.maxOrNull().toString(), fontSize = fontSize)
        Text(text = yList.minOrNull().toString(), fontSize = fontSize)
    }
}


@Composable
fun Drawing(pointList: PointListMapper, modifier: Modifier) {
    val dotColor = MaterialTheme.colors.primary

    var xMinLim by remember { mutableStateOf(pointList.xMinLim) }
    var xMaxLim by remember { mutableStateOf(pointList.xMaxLim) }
    var yMinLim by remember { mutableStateOf(pointList.yMinLim) }
    var yMaxLim by remember { mutableStateOf(pointList.yMaxLim) }

    Box(
        modifier
            .padding(3.dp)
            .pointerInput(Unit){

                detectTapGestures(onDoubleTap = {
                    xMaxLim = pointList.xMaxLim
                    xMinLim = pointList.xMinLim
                    yMaxLim = pointList.yMaxLim
                    yMinLim = pointList.yMinLim
                })


            },
        contentAlignment = Alignment.Center,
        true
    ) {

        val lineColor = MaterialTheme.colors.onSurface
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {

                    detectTransformGestures { center, pan, zoom, _ ->
                        val xSize = xMaxLim - xMinLim
                        val ySize = yMaxLim - yMinLim
                        val xPoint = ((center.x / size.width) * xSize) + xMinLim
                        val yPoint = (((size.height - center.y) / size.height) * ySize) + yMinLim
                        xMaxLim =
                            (xPoint + ((xMaxLim - xPoint).div(zoom))) - (pan.x / size.width) * xSize
                        xMinLim =
                            (xPoint - ((xPoint - xMinLim).div(zoom))) - (pan.x / size.width) * xSize
                        yMaxLim =
                            (yPoint + ((yMaxLim - yPoint).div(zoom))) + (pan.y / size.height) * ySize
                        yMinLim =
                            (yPoint - ((yPoint - yMinLim).div(zoom))) + (pan.y / size.height) * ySize
                    }
                }
        ) {
            drawPoints(
                points = pointList
                    .offsetList(
                        size,
                        xMinLim,
                        xMaxLim,
                        yMinLim,
                        yMaxLim
                    ),
                pointMode = PointMode.Polygon,
                lineColor,
                strokeWidth = 5.0f
            )
            pointList.offsetList(size, xMinLim, xMaxLim, yMinLim, yMaxLim).forEach {
                drawCircle(
                    Brush.linearGradient(
                        colors = listOf(dotColor, dotColor)
                    ),
                    radius = 2.0f,
                    center = it,
                    style = Stroke(width = size.width * 0.015f)
                )
            }


            pointList.xLabelsList(getTextPaint().textSize,0.01f,size,xMinLim, xMaxLim).forEach {
                val strVal  = String.format("%.2f",it.x)
                drawContext.canvas.nativeCanvas.drawText(
                    "${strVal}",
                    it.pointOffset(size,xMinLim, xMaxLim, yMinLim, yMaxLim).x,
                    size.height+100f+getTextPaint().textSize,
                    getTextPaint()
                )
            }
            /*pointList.pointsOnCanvas(xMinLim, xMaxLim).filterIndexed{index,_ -> (index % (labelCounter+1))==0 }.forEach {
                drawContext.canvas.nativeCanvas.drawText(
                    "${it.x}",
                    it.pointOffset(size, xMinLim, xMaxLim, yMinLim, yMaxLim).x,
                    size.height+100f,
                    getTextPaint()
                )

            }

             */
        }
    }
}


fun getTextPaint(): Paint {
    val paint = Paint()
    paint.color = Color.BLACK
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 30f
    return paint
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
                val mockX = listOf(1.0f, 8.0f, 1.0f, 16.0f, 32.0f)
                val mockY = listOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
                PolygonChart(
                    mockY, mockX,
                    modifier = Modifier.size(300.dp),
                    labelFontSize = 10.sp
                )
            }
        }
    }
}

