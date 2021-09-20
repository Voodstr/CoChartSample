package ru.voodster.smartcharts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.voodster.smartcharts.PointListMapper.Companion.hoursList
import ru.voodster.smartcharts.PointListMapper.Companion.tempList
import ru.voodster.smartcharts.ui.theme.SmartChartsTheme

class MainActivity : ComponentActivity() {
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

                            PolygonChart(hoursList,tempList,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .requiredHeightIn(200.dp, 300.dp),
                                labelFontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

