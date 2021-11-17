package ru.voodster.smartcharts

import AbstractPointMapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.voodster.smartcharts.ui.theme.SmartChartsTheme

class MainActivity : ComponentActivity() {

    companion object{
        private const val listSize = 100
        val tempList = Array(listSize){
            (0..20).random().toFloat()
        }
        val hoursList = Array(listSize){index->
            index.times(0.5).toFloat()
        }
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
                            val pointList = AbstractPointMapper(hoursList, tempList)
                            PolygonChart(pointList,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .requiredHeightIn(200.dp, 300.dp),
                                    grid = true, 40f
                            )
                        }
                    }
                }
            }
        }
    }
}
