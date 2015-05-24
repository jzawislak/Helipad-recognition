package pl.zaw.image.operations

import java.awt.BasicStroke
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.image.operations.filter.{Filter, MinimumRankFilter3}
import pl.zaw.image.util.ColorHelper

/**
 * Created on 2015-05-23.
 *
 * @author Jakub Zawislak
 */
object HelipadDetection {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def detectHelipad(bufferedImage: BufferedImage): BufferedImage = {
    //prepare copy of an image
    val cm = bufferedImage.getColorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = bufferedImage.copyData(null)
    val resultImage = new BufferedImage(cm, raster, isAlphaPremultiplied, null)
    val graphics = resultImage.createGraphics()
    graphics.setStroke(new BasicStroke(10))

    val segFound = path1(bufferedImage)

    for {
      segment <- segFound
    } {
      graphics.setColor(ColorHelper.getRandomColor)
      graphics.drawOval(segment(23).toInt - segment(17).toInt, segment(22).toInt - segment(17).toInt, segment(17).toInt * 2, segment(17).toInt * 2)
    }
    logger.info(s"Found ${segFound.length} helipads.")
    resultImage
  }

  def path1(bufferedImage: BufferedImage) = {
    val thresholdImage = Threshold.convertAbsolute(bufferedImage, redLimit = (150, 255), greenLimit = (150, 255), blueLimit = (150, 255))
    val minimumRankImage = Filter.filter(thresholdImage, MinimumRankFilter3)
    val tup = Segmentation.getSegments(minimumRankImage)
    //TODO change to false
    val mParams = Moments.calculateParams(tup, logParams = true)
    val segFound = detectHFromParams(mParams)

    segFound
  }

  def detectHFromParams(mParams: Array[Array[Double]]) = {
    val hFound = mParams.filter(segment => {
      var conCount = 0

      if (segment(24) > 50) /* area */ {
        if (segment(0) > 0.28 && segment(0) < 0.38) conCount = conCount + 1 /* M1 */
        //30-38
        //if (truncateAt(segment(3), 6) == 0) conCount = conCount + 1 /* M4 */
        if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
        //if (truncateAt(segment(5), 6) == 0) conCount = conCount + 1 /* M6 */
        if (segment(6) > 0.023 && segment(6) < 0.03) conCount = conCount + 1 /* M7 */
        //0.024-0.03
        if (segment(13) > 0.10 && segment(13) < 0.15) conCount = conCount + 1 /* W7, maybe decrease lower bound */
        //slabe
        if (segment(14) > 0.17 && segment(14) < 0.22) conCount = conCount + 1 /* W8 */
        //moze podniesc dolne do 0.18
        if (segment(15) > 0.4 && segment(15) < 0.52) conCount = conCount + 1 /* W9 */
        //zmniejszyc dolne do 0.35

        if (conCount >= 3) {
          true
        } else {
          conCount = 0
          //dla przechylonych bokiem do kamery
          if (segment(0) >= 0.38 && segment(0) < 0.6) conCount = conCount + 1 /* M1 */
          if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
          if (segment(6) > 0.015 && segment(6) <= 0.023) conCount = conCount + 1 /* M7 */
          if (segment(13) > 0.05 && segment(13) <= 0.1) conCount = conCount + 1 /* W7 */
          if (segment(14) > 0.17 && segment(14) < 0.22) conCount = conCount + 1 /* W8 */
          if (segment(15) > 0.25 && segment(15) <= 0.4) conCount = conCount + 1 /* W9 */

          if (conCount >= 3) {
            true
          } else {
            conCount = 0

            //dla przechylonych nozkami do kamery
            if (segment(0) > 0.22 && segment(0) <= 0.28) conCount = conCount + 1 /* M1 */
            if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
            if (segment(6) > 0.015 && segment(6) <= 0.023) conCount = conCount + 1 /* M7 */
            if (segment(13) >= 0.15 && segment(13) <= 0.2) conCount = conCount + 1 /* W7 */
            if (segment(14) > 0.17 && segment(14) < 0.22) conCount = conCount + 1 /* W8 */
            if (segment(15) > 0.35 && segment(15) <= 0.45) conCount = conCount + 1 /* W9 */

            if (conCount >= 3) true else false
          }
        }
      } else {
        //always false if area of segment is too small
        false
      }
    })
    logger.info(s"Detected ${hFound.length} H marks.")
    hFound
  }

  def truncateAt(value: Double, power: Int): Double = {
    val position = math pow(10, power)
    if (value > 0) (math floor (value * position)) / position
    else (math ceil (value * position)) / position
  }
}
