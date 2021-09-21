# SmartCharts - Jetpack Compose Charts widget
 
 Currently there's only PolygonChart - charts with line drawn from point to point
 
 ## Preview
  1. You can zoom and scroll


  <img src="/zoom.gif" width="300"> <img src="/scroll.gif" width="300">
  
  
  2. Double tap to resize to cover all Points, Long press to resize to default Limits
 
 <img src="/dbltap.gif" width="300"> <img src="/longPress.gif" width="300">

  3. Charts can be any size or orientation
 
 <img src="/resize.gif">


 ## How to use it
 
  It contains "PointListMapper" which is responsible of converting values into points on canvas
  
  
 1. Create 2 list of values with same list.size
 2. Create PointListMapper class with given lists as parameters
 3. Pass it to PolygonChart widget
 4. There is 2 parameters: `label=true`  - if you want axis values, `grid=true` - if you grid lines
 
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
   
  //example limits 
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
 
 # Licence
 
  ```
 
 Copyright 2021 Dmitriy Bychkov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 
License applies to PointLisMapper class and PolygonChart
 
  ```
