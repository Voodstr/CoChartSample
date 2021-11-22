package ru.voodster.smartcharts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.voodster.smartcharts.ui.theme.SmartChartsTheme
import kotlin.math.pow

class MainActivity : ComponentActivity() {

    companion object{
        private const val listSize = 41
        val xMath = Array(listSize){
            (0.minus(listSize/8)+it/4).toFloat()
        }
        val yMath = Array(listSize){ index->
            ((5-xMath[index])*(5+xMath[index])).pow(0.5f)
        }
        val timeList = Array(listSize){ it.toLong() }
        val tempList = Array(listSize){(0..20).random().toFloat()}
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartChartsTheme {
                Scaffold(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Column() {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
/*
                            Log.d("Points","y = ${yMath[1]} ${yMath[2]}")
                            val pointList = ru.voodster.smartcharts.PointMapper(xMath, yMath)
                            MathChart(pointList,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .requiredHeightIn(200.dp, 300.dp),
                                    grid = true, 40f
                            )


 */

                           TimeSeriesChart(
                               modifier = Modifier
                                   .fillMaxSize()
                                   .padding(10.dp)
                                   .requiredHeightIn(200.dp, 300.dp),
                               timeList, tempList,
                               grid = true, 50f
                           )

                        }
                    }
                }
            }
        }
    }
}
