package pl.zaw.image.operations

import java.awt.Color
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Created on 2015-06-17.
 *
 * @author Jakub Zawislak
 */
object Gamma {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def applyCorrection(bufferedImage: BufferedImage, gamma: Double): BufferedImage = {
    if (bufferedImage == null) {
      return null
    }
    logger.info("Applying gamma.")
    val lookupTable = generateLookupTable(gamma)

    val newImage = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    for {
      row <- 0 to bufferedImage.getHeight - 1
      col <- 0 to bufferedImage.getWidth - 1
      color = new Color(bufferedImage.getRGB(col, row))
    } {
      val newRed = lookupTable.get(color.getRed).get
      val newGreen = lookupTable.get(color.getGreen).get
      val newBlue = lookupTable.get(color.getBlue).get
      newImage.setRGB(col, row, new Color(newRed, newGreen, newBlue).getRGB)
    }
    newImage
  }

  private def generateLookupTable(gamma: Double) = {
    val lookupList = for {
      i <- 0D to 255D by 1D
    } yield {
        val newValue = Math.round(255D * Math.pow(i / 255D, 1D / gamma)).toInt
        i -> (if (newValue > 255) 255 else newValue)
      }
    Map(lookupList: _*)
  }

}
