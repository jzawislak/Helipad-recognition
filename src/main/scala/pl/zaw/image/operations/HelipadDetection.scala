package pl.zaw.image.operations

import java.awt.BasicStroke
import java.awt.image.BufferedImage
import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.image.util.ColorHelper

/**
 * Created on 2015-05-23.
 *
 * @author Jakub Zawislak
 */
object HelipadDetection {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def detectHelipad(bufferedImage: BufferedImage, path: Int): BufferedImage = {
    //prepare copy of an image
    val cm = bufferedImage.getColorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = bufferedImage.copyData(null)
    val resultImage = new BufferedImage(cm, raster, isAlphaPremultiplied, null)
    val graphics = resultImage.createGraphics()
    graphics.setStroke(new BasicStroke(10))

    val segFound = if (path == 1) {
      path1(bufferedImage)
    } else if (path == 2) {
      path2(bufferedImage)
    } else {
      path3(bufferedImage)
    }

    for {
      segment <- segFound
    } {
      graphics.setColor(ColorHelper.getRandomColor)
      val diameter = (segment(17) * 1.2).toInt
      graphics.drawOval(segment(23).toInt - diameter, segment(22).toInt - diameter, diameter * 2, diameter * 2)
    }

    resultImage
  }

  /**
   * Detects H marks.
   * @param bufferedImage image to process
   * @return list of segments containing H marks
   */
  def path1(bufferedImage: BufferedImage) = {
    logger.info("Applying threshold.")
    val thresholdImage = Threshold.convertAbsolute(bufferedImage, redLimit = (150, 255), greenLimit = (150, 255), blueLimit = (150, 255))
    // logger.info("Applying minimum filter.")
    // val minimumRankImage = Filter.filter(thresholdImage, MinimumRankFilter3)
    logger.info("Calculating segments.")
    val tup = Segmentation.getSegments(thresholdImage)
    //TODO change to false
    val mParams = Moments.calculateParams(tup, logParams = true)
    val segFound = detectHFromParams(mParams)

    if (false) {
      logger.info("Saving mParams to file")

      val logFile = new File("Moments_LOG.txt")
      val out = new PrintWriter(logFile)
      var segmentCount: Int = 0
      out.write(s"Segment\tM1\tM2\tM3\tM4\tM5\tM6\tM7\tM8\tM9\tM10\tW1\tW2\tW3\tW7\tW8\tW9\t" +
        s"DistMin\tDistMax\tRowMin\tRowMax\tColMin\tColMax\tCentralRow\tCentralCol\t" +
        s"Area\tPerimeter\n")

      try {
        for {
          segment <- segFound
        } {
          segmentCount = segmentCount + 1
          out.write(s"Segment $segmentCount\t")
          for {
            param <- segment
          } {
            out.write(f"${param}%.20f\t")
          }
          out.write("\n")
        }
      } finally {
        out.close()
      }
    }

    segFound
  }

  /**
   * Detects circles.
   * @param bufferedImage image to process
   * @return list of segments containing H marks
   */
  def path2(bufferedImage: BufferedImage) = {
    logger.info("Applying threshold.")
    val thresholdImage = Threshold.convertAbsolute(bufferedImage, redLimit = (150, 255), greenLimit = (150, 255), blueLimit = (0, 170))
    logger.info("Applying minimum filter.")
    // val minimumRankImage = Filter.filter(thresholdImage, MinimumRankFilter3)
    logger.info("Calculating segments.")
    val tup = Segmentation.getSegments(thresholdImage)
    //TODO change to false
    val mParams = Moments.calculateParams(tup, logParams = false)
    val segFound = detectCircleFromParams(mParams)

    if (true) {
      logger.info("Saving mParams to file")

      val logFile = new File("Moments_LOG.txt")
      val out = new PrintWriter(logFile)
      var segmentCount: Int = 0
      out.write(s"Segment\tM1\tM2\tM3\tM4\tM5\tM6\tM7\tM8\tM9\tM10\tW1\tW2\tW3\tW7\tW8\tW9\t" +
        s"DistMin\tDistMax\tRowMin\tRowMax\tColMin\tColMax\tCentralRow\tCentralCol\t" +
        s"Area\tPerimeter\n")

      try {
        for {
          segment <- segFound
        } {
          segmentCount = segmentCount + 1
          out.write(s"Segment $segmentCount\t")
          for {
            param <- segment
          } {
            out.write(f"${param}%.20f\t")
          }
          out.write("\n")
        }
      } finally {
        out.close()
      }
    }

    segFound
  }

  /**
   * Detects helipads.
   * @param bufferedImage image to process
   * @return list of segments containing H marks
   */
  def path3(bufferedImage: BufferedImage) = {
    val hFound = path1(bufferedImage)
    val circleFound = path2(bufferedImage)


    val heliFound = circleFound.filter { circle =>
      val relatedH = hFound.filter { h =>
        //distance between central moments < 10% of max distance in H sign
        if (Math.sqrt(Math.pow(circle(22) - h(22), 2) + Math.pow(circle(23) - h(23), 2)) < 0.15 * h(17)) {
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

        if (conCount > 3) {
          true
        } else {
          conCount = 0
          //dla przechylonych bokiem do kamery
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

            //dla przechylonych nozkami do kamery
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
    val circleFound = mParams.filter(segment => {
      var conCount = 0

      if (segment(24) > 50) /* area */ {
        if (segment(0) > 0.9 && segment(0) < 1.1) conCount = conCount + 1 /* M1 */
        if (truncateAt(segment(4), 5) == 0) conCount = conCount + 1 /* M5 */
        if (segment(6) > 0.14 && segment(6) < 0.024) conCount = conCount + 1 /* M7 */
        if ((segment(13) > 0.8 && segment(13) < 0.9)
          || (segment(13) > 0.55 && segment(13) < 0.9)) conCount = conCount + 1 /* W7 */
        if (segment(14) > 0.11 && segment(14) < 0.15) conCount = conCount + 1 /* W8 */
        if (segment(15) > 0.15 && segment(15) < 0.25) conCount = conCount + 1 /* W9 */

        if (conCount > 3) {
          true
        } else {
          var conCount = 0

          //dla splaszczonych mocno
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

            //dla splaszczonych srednio
            if (segment(0) > 0.9 && segment(0) < 1.1) conCount = conCount + 1 /* M1 */
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
