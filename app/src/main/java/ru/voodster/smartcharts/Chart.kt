package ru.voodster.smartcharts

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.voodster.smartcharts.ui.theme.SmartChartsTheme


@Composable
fun PolygonChart(
    xList: List<Float>, yList: List<Float>,
    modifier: Modifier,
    labelFontSize: TextUnit
) {
    //val scope = rememberCoroutineScope()
    val cornerOffset = 10.dp
    val surfaceColor = MaterialTheme.colors.surface
    Surface(
        modifier = modifier.clip(RoundedCornerShape(cornerOffset)),
        color = surfaceColor,
        elevation = 8.dp
    ) {
        Drawing(xList, yList, modifier = modifier)
        /*
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

            }
            Bottom(
                Modifier
                    .fillMaxWidth()
                    .weight(0.1f, fill = true),
                xList,
                labelFontSize
            )
        }
        */

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
fun Drawing(xList: List<Float>, yList: List<Float>, modifier: Modifier) {
    val dotColor = MaterialTheme.colors.primary
    Box(
        modifier.padding(10.dp),
        contentAlignment = Alignment.Center,
        true
    ) {
        val lineColor = MaterialTheme.colors.onSurface
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        //pointList.xLimits.max.div(zoom)
                        // pointList.yLimits.max.div(zoom)
                        /* val scaledOffset =
                             Offset(offset.x.div(size.width / 10), offset.y.div(size.height / 10))
                         yLim =  Limits(yLim.min.minus(scaledOffset.y) / zoom, yLim.max.plus(scaledOffset.y) / zoom)
                         */
                    }
                }
        ) {
            val offsetList = PointList(xList, yList).offsetList(size)
                drawPoints(
                points = offsetList,
                pointMode = PointMode.Polygon,
                lineColor,
                strokeWidth = 5.0f
            )
            offsetList.forEach {
                drawCircle(
                    Brush.linearGradient(
                        colors = listOf(dotColor, dotColor)
                    ),
                    radius = 2.0f,
                    center = it,
                    style = Stroke(width = size.width * 0.015f)
                )
            }
        }
    }
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

