package pl.zaw.image.operations

import java.awt.image.BufferedImage
import java.awt.{BasicStroke, Font}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.image.operations.filter.{AverageFilter3, Filter}
import pl.zaw.image.util.ColorHelper

/**
 * Created on 2015-05-23.
 *
 * @author Jakub Zawislak
 */
object HelipadDetection {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def detectHelipad(bufferedImage: BufferedImage, path: (BufferedImage) => Array[Array[Double]]): BufferedImage = {
    //prepare copy of an image
    val cm = bufferedImage.getColorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = bufferedImage.copyData(null)
    val resultImage = new BufferedImage(cm, raster, isAlphaPremultiplied, null)
    val graphics = resultImage.createGraphics()
    graphics.setStroke(new BasicStroke(10))
    graphics.setFont(new Font("TimesRoman", Font.BOLD, 80))

    val segFound = path(bufferedImage)
    val distances = detectHeliDistance(segFound)

    for {
      (segment, distance) <- segFound.zip(distances)
    } {
      graphics.setColor(ColorHelper.getRandomColor)
      val diameter = (segment(17) * 1.2).toInt
      graphics.drawOval(segment(23).toInt - diameter, segment(22).toInt - diameter, diameter * 2, diameter * 2)
      graphics.drawString(f"$distance%.0fcm", segment(23).toInt, segment(22).toInt)
    }

    resultImage
  }

  /**
   * Detects H marks.
   * @param bufferedImage image to process
   * @return list of segments containing H marks
   */
  def pathThresholdH(bufferedImage: BufferedImage) = {
    val thresholdImage = Threshold.convertAbsolute(bufferedImage, redLimit = (150, 255), greenLimit = (150, 255), blueLimit = (150, 255))
    //Minimum Rank filter is not used, because it takes a lot of time
    //and does not improve the results significantly.
    //    logger.info("Applying minimum filter.")
    //    val minimumRankImage = Filter.filter(thresholdImage, MinimumRankFilter3)
    val tup = Segmentation.getSegments(thresholdImage)
    val mParams = Moments.calculateParams(tup, logParams = false)
    val segFound = detectHFromParams(mParams)

    //SegmentationUtil.logSegmentsToFile(segFound)

    segFound
  }

  /**
   * Detects circles.
   * @param bufferedImage image to process
   * @return list of segments containing H marks
   */
  def pathThresholdCircle(bufferedImage: BufferedImage) = {
    val thresholdImage = Threshold.convertAbsolute(bufferedImage, redLimit = (150, 255), greenLimit = (150, 255), blueLimit = (0, 170))
    //Minimum Rank filter is not used, because it takes a lot of time
    //and does not improve the results significantly.
    //    logger.info("Applying minimum filter.")
    //    val minimumRankImage = Filter.filter(thresholdImage, MinimumRankFilter3)
    val tup = Segmentation.getSegments(thresholdImage)
    val mParams = Moments.calculateParams(tup, logParams = false)
    val segFound = detectCircleFromParams(mParams)

    //SegmentationUtil.logSegmentsToFile(segFound)

    segFound
  }

  /**
   * Detects helipads.
   * @param bufferedImage image to process
   * @return list of segments containing H marks
   */
  def pathThresholdHeli(bufferedImage: BufferedImage) = {
    logger.info("Searching for H marks.")
    val hFound = pathThresholdH(bufferedImage)
    logger.info("Searching for Circles.")
    val circleFound = pathThresholdCircle(bufferedImage)
    val heliFound = detectHeliFromParams(circleFound, hFound)

    heliFound
  }

  /**
   * Detects helipads.
   * @param bufferedImage image to process
   * @return list of segments containing H marks
   */
  def pathColorHeli(bufferedImage: BufferedImage) = {
    var filteredImage = bufferedImage
    for {
      i <- 1 to 3
    } {
      logger.info(s"Average filter number $i.")
      filteredImage = Filter.filter(filteredImage, AverageFilter3)
    }
    val tup = Segmentation.getSegmentsColor(filteredImage)
    val mParams = Moments.calculateParams(tup, logParams = false)
    val circleFound = detectCircleFromParams(mParams)
    val hFound = detectHFromParams(mParams)
    val heliFound = detectHeliFromParams(circleFound, hFound)
    heliFound
  }

  def detectHeliDistance(heliFound: Array[Array[Double]]) = {
    for {
      heli <- heliFound
    } yield {
      //constants for Samsung Galaxy S3 full resolution
      190.61 / heli(17) * 100
    }
  }

  def detectHeliFromParams(circleParams: Array[Array[Double]], hParams: Array[Array[Double]]) = {
    logger.info("Detecting Heli.")
    val heliFound = circleParams.filter { circle =>
      val relatedH = hParams.filter { h =>
        //Distance between central moments < 10% of max distance in H sign
        if ((Math.sqrt(Math.pow(circle(22) - h(22), 2) + Math.pow(circle(23) - h(23), 2)) < 0.2 * h(17)
          //Distance between average of min and max < 10% of max distance in H sign
          || Math.sqrt(Math.pow((circle(18) + circle(19) - h(18) - h(19)) / 2, 2) + Math.pow((circle(20) + circle(21) - h(20) - h(21)) / 2, 2)) < 0.2 * h(17))
          //max distance
          && 1.1 * h(17) < circle(17)) {
          true
        } else {
          false
        }
      }
      if (relatedH.length == 1) true else false
    }
    logger.info(s"Detected ${
      heliFound.length
    } helipad marks.")
    heliFound
  }

  def detectHFromParams(mParams: Array[Array[Double]]) = {
    logger.info("Detecting H.")
    val hFound = mParams.filter(segment => {
      var conCount = 0

      //There are some comments indicating values that may be useful for algorithm tuning in the future.
      if (segment(24) > 100) /* area */ {
        if (segment(0) > 0.28 && segment(0) < 0.38) conCount = conCount + 1 /* M1 */
        //30-38
        //if (truncateAt(segment(3), 6) == 0) conCount = conCount + 1 /* M4 */
        if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
        //if (truncateAt(segment(5), 6) == 0) conCount = conCount + 1 /* M6 */
        if (segment(6) > 0.023 && segment(6) < 0.03) conCount = conCount + 1 /* M7 */
        //0.024-0.03
        if (segment(13) > 0.10 && segment(13) < 0.15) conCount = conCount + 1 /* W7, maybe decrease lower bound */
        if (segment(14) > 0.17 && segment(14) < 0.22) conCount = conCount + 1 /* W8 */
        //maybe increase lower bound to 0.18
        if (segment(15) > 0.4 && segment(15) < 0.52) conCount = conCount + 1 /* W9 */
        //maybe decrease lower bound to 0.35

        if (conCount > 3) {
          true
        } else {
          conCount = 0
          //H leaned to camera with its side.
          //From this point of view ->H.
          if (segment(0) >= 0.38 && segment(0) < 0.6) conCount = conCount + 1 /* M1 */
          if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
          if (segment(6) > 0.014 && segment(6) <= 0.023) conCount = conCount + 1 /* M7 */
          if (segment(13) > 0.05 && segment(13) <= 0.11) conCount = conCount + 1 /* W7 */
          if (segment(14) > 0.17 && segment(14) < 0.24) conCount = conCount + 1 /* W8 */
          if (segment(15) > 0.25 && segment(15) <= 0.4) conCount = conCount + 1 /* W9 */

          if (conCount > 3) {
            true
          } else {
            conCount = 0

            //H leaned to camera with its bottom.
            //From this point of view H.
            //                        ^
            if (segment(0) > 0.22 && segment(0) <= 0.28) conCount = conCount + 1 /* M1 */
            if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
            if (segment(6) > 0.015 && segment(6) <= 0.023) conCount = conCount + 1 /* M7 */
            if (segment(13) >= 0.15 && segment(13) <= 0.2) conCount = conCount + 1 /* W7 */
            if (segment(14) > 0.17 && segment(14) < 0.22) conCount = conCount + 1 /* W8 */
            if (segment(15) > 0.35 && segment(15) <= 0.45) conCount = conCount + 1 /* W9 */

            if (conCount > 3) true else false
          }
        }
      } else {
        //always false if area of segment is too small
        false
      }
    })
    logger.info(s"Detected ${
      hFound.length
    } H marks.")
    hFound
  }

  def detectCircleFromParams(mParams: Array[Array[Double]]) = {
    logger.info("Detecting Circle.")
    val circleFound = mParams.filter(segment => {
      var conCount = 0

      //M1 could be set as a must
      if (segment(24) > 250) /* area */ {
        if (segment(0) > 0.9 && segment(0) < 1.2) conCount = conCount + 1 /* M1 */
        if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
        if (segment(6) > 0.14 && segment(6) < 0.024) conCount = conCount + 1 /* M7 */
        if ((segment(13) > 0.8 && segment(13) < 0.9)
          || (segment(13) > 0.55 && segment(13) < 0.9)) conCount = conCount + 1 /* W7 */
        if (segment(14) > 0.09 && segment(14) < 0.15) conCount = conCount + 1 /* W8 */
        if (segment(15) > 0.14 && segment(15) < 0.25) conCount = conCount + 1 /* W9 */

        if (conCount > 3) {
          true
        } else {
          var conCount = 0

          //very flattened circles
          if (segment(0) > 0.6 && segment(0) <= 0.9) conCount = conCount + 1 /* M1 */
          if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
          if (segment(6) > 0.02 && segment(6) <= 0.14) conCount = conCount + 1 /* M7 */
          if (segment(13) > 0.2 && segment(13) <= 0.35) conCount = conCount + 1 /* W7 */
          if (segment(14) >= 0.15 && segment(14) < 0.22) conCount = conCount + 1 /* W8 */
          if (segment(15) > 0.25 && segment(15) < 0.32) conCount = conCount + 1 /* W9 */

          if (conCount > 3) {
            true
          } else {
            var conCount = 0

            //medium flattened circles
            if (segment(0) > 0.9 && segment(0) < 1.2) conCount = conCount + 1 /* M1 */
            if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
            if (segment(6) > 0.14 && segment(6) < 0.24) conCount = conCount + 1 /* M7 */
            if (segment(13) > 0.35 && segment(13) <= 0.55) conCount = conCount + 1 /* W7 */
            if (segment(14) > 0.11 && segment(14) < 0.16) conCount = conCount + 1 /* W8 */
            if (segment(15) > 0.15 && segment(15) < 0.25) conCount = conCount + 1 /* W9 */

            if (conCount > 3) true else false
          }
        }
      } else {
        //always false if area of segment is too small
        false
      }
    })
    logger.info(s"Detected ${
      circleFound.length
    } circle marks.")
    circleFound
  }

  def truncateAt(value: Double, power: Int): Double = {
    val position = math pow(10, power)
    if (value > 0) (math floor (value * position)) / position
    else (math ceil (value * position)) / position
  }
}
