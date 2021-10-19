package ru.voodster.smartcharts

import AbstractPointMapper

class FloatListMapper(private val xList:Array<Float>, private val yList: Array<Float>)
    :AbstractPointMapper(xList, yList) {
    override fun checkErrors() {
        if(xList.size!=yList.size) throw IndexOutOfBoundsException("Arrays should be same size")
    }

}


class TimeSeriesListMapper(private val xList:Array<Long>, private val yList: Array<Float> )
    :AbstractPointMapper(Array(xList.size){xList[it].toFloat()},yList){
        override fun checkErrors() {
            TODO("Not yet implemented")
        }
    }

