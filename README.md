# SmartCharts - Jetpack Compose Charts widget
 
 Currently there's only PolygonChart - charts with line drawn from point to point
  
 It contains "PointListMapper" which is responsible of converting values into points on canvas
 
 ### How to use it
 
 ```kotlin
 //example lists
   val mockX = listOf(1.0f, 8.0f, 1.0f, 16.0f, 32.0f)
   val mockY = listOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
   
   
   val pointList = PointListMapper(mockY, mockX)
   PolygonChart(
           pointList,
           modifier = Modifier.size(300.dp),
           grid = true, labels = true
   )        
 ```
 
 Also you can add some limits
 
   ```kotlin
 
 //example lists
   val mockX = listOf(1.0f, 8.0f, 1.0f, 16.0f, 32.0f)
   val mockY = listOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)
   
 val xMinLim = 1f
 val xMaxLim = 10f
 val yMinLim = 1f
 val yMaxLim = 20f
   
   
   val pointList = PointListMapper(mockY, mockX,xMinLim,xMaxLim,yMinLim,yMaxLim)

    PolygonChart(
           pointList,
           modifier = Modifier.size(300.dp),
           grid = true, labels = true
   ) 
 ```
 
