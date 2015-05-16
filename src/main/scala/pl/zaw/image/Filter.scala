package pl.zaw.image

import java.awt.Color
import java.awt.image.BufferedImage

/**
 * Created on 2015-05-14.
 *
 * @author Jakub Zawislak
 */
object Filter {
  def filter(bufferedImage: BufferedImage): BufferedImage = {
    if(bufferedImage == null) {
      return null
    }
    val newImage = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)

    for {
      row <- 0 to bufferedImage.getHeight - 1
      col <- 0 to bufferedImage.getWidth - 1
      color = new Color(bufferedImage.getRGB(col, row))
    } {
      newImage.setRGB(col, row, new Color(color.getRed, color.getGreen, 0).getRGB)
    }
    newImage
  }
}
