package pl.zaw.image.util

import java.awt.Color

import scala.collection.mutable.ListBuffer

/**
 * Created on 2015-05-16.
 *
 * @author Jakub Zawislak
 */
case class ColorHelper(var red: Double = 0, var green: Double = 0, var blue: Double = 0, var maskSum: Double = 0) {
  var _colorList = new ListBuffer[Color]

  def addColor(color: Color, maskWeight: Int): Unit = {
    red += color.getRed * maskWeight
    green += color.getGreen * maskWeight
    blue += color.getBlue * maskWeight
    maskSum += maskWeight
  }

  def addToColorList(color: Color): Unit = {
    _colorList.append(color)
  }

  def colorList = _colorList

  def colorList_=(colorList: ListBuffer[Color]) = _colorList = colorList

  def getColor = {
    new Color(ColorHelper.standardize(red, maskSum), ColorHelper.standardize(green, maskSum), ColorHelper.standardize(blue, maskSum))
  }
}

object ColorHelper {
  val BLACK = 0xFF000000
  val WHITE = 0xFFFFFFFF
  val maxColorVal = 255
  val random = new scala.util.Random(1)

  def standardize(colorVal: Double, maskSum: Double): Int = {
    val resultColor = if (maskSum == 0) colorVal.toInt else (colorVal / maskSum).toInt

    if (resultColor < 0) {
      0
    } else if (resultColor > maxColorVal) {
      maxColorVal
    } else {
      resultColor
    }
  }

  def getRandomColor = {
    new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))
  }
}
