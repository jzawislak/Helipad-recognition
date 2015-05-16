package pl.zaw.image

import java.awt.Color
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Created on 2015-05-16.
 *
 * @author Jakub Zawislak
 */
object Threshold {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def convertAbsolute(bufferedImage: BufferedImage,
                      redLimit: (Int, Int) = (0, 255),
                      greenLimit: (Int, Int) = (0, 255),
                      blueLimit: (Int, Int) = (0, 255)): BufferedImage = {
    if (bufferedImage == null) {
      return null
    }
    val newImage = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    for {
      row <- 0 to bufferedImage.getHeight - 1
      col <- 0 to bufferedImage.getWidth - 1
      color = new Color(bufferedImage.getRGB(col, row))
    } {
      if (color.getRed > redLimit._1 &&
        color.getGreen > greenLimit._1 &&
        color.getBlue > blueLimit._1 &&
        color.getRed < redLimit._2 &&
        color.getGreen < greenLimit._2 &&
        color.getBlue < blueLimit._2
      ) {
        newImage.setRGB(col, row, 0xfffffff)
      } else {
        newImage.setRGB(col, row, 0)
      }
    }
    newImage
  }

  /**
   * Doesn't work at all.
   */
  @Deprecated
  def convertRelative(bufferedImage: BufferedImage,
                      redLimit: (Double, Double) = (0, 3),
                      greenLimit: (Double, Double) = (0, 3),
                      blueLimit: (Double, Double) = (0, 3)): BufferedImage = {
    if (bufferedImage == null) {
      return null
    }
    val newImage = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    var average = 0D

    for {
      row <- 0 to bufferedImage.getHeight - 1
      col <- 0 to bufferedImage.getWidth - 1
      color = new Color(bufferedImage.getRGB(col, row))
    } {
      average = (average + color.getRed) / 2
      average = (average + color.getGreen) / 2
      average = (average + color.getBlue) / 2
    }
    logger.info(s"Average calculated $average")
    for {
      row <- 0 to bufferedImage.getHeight - 1
      col <- 0 to bufferedImage.getWidth - 1
      color = new Color(bufferedImage.getRGB(col, row))
    } {
      if (color.getRed.toDouble / average > redLimit._1 &&
        color.getGreen.toDouble / average > greenLimit._1 &&
        color.getBlue.toDouble / average > blueLimit._1 &&
        color.getRed.toDouble / average < redLimit._2 &&
        color.getGreen.toDouble / average < greenLimit._2 &&
        color.getBlue.toDouble / average < blueLimit._2
      ) {
        newImage.setRGB(col, row, 0)
      } else {
        newImage.setRGB(col, row, 0xfffffff)
      }
    }
    newImage
  }
}
