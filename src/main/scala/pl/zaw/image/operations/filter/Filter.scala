package pl.zaw.image.operations.filter

import java.awt.Color
import java.awt.image.BufferedImage

import pl.zaw.image.util.ColorHelper

/**
 * Created on 2015-05-14.
 *
 * @author Jakub Zawislak
 */
object Filter {
  def filter(bufferedImage: BufferedImage, filterType: FilterType): BufferedImage = {
    if (bufferedImage == null) {
      return null
    }
    val newImage = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    val maskSize = filterType.size

    for {
      row <- 0 to bufferedImage.getHeight - 1
      col <- 0 to bufferedImage.getWidth - 1
    } {
      if (row < maskSize / 2
        || col < maskSize / 2
        || row > bufferedImage.getHeight - 1 - maskSize / 2
        || col > bufferedImage.getWidth - 1 - maskSize / 2) {
        //copy edges
        newImage.setRGB(col, row, bufferedImage.getRGB(col, row))
      } else {
        val colorHelper = new ColorHelper
        for {
          maskLoopRow <- row - maskSize / 2 to row + maskSize / 2
          maskLoopCol <- col - maskSize / 2 to col + maskSize / 2
          maskRow = maskLoopRow - row + maskSize / 2
          maskCol = maskLoopCol - col + maskSize / 2
        } yield {
          filterType match {
            case maskFilter: MaskFilter =>
              colorHelper.addColor(new Color(bufferedImage.getRGB(maskLoopCol, maskLoopRow)), maskFilter.mask(maskRow)(maskCol))
            case rankFilter: RankFilter =>
              colorHelper.addToColorList(new Color(bufferedImage.getRGB(maskLoopCol, maskLoopRow)))
          }
        }
        filterType match {
          case maskFilter: MaskFilter =>
            newImage.setRGB(col, row, colorHelper.getColor.getRGB)
          case rankFilter: RankFilter =>
            newImage.setRGB(col, row, new Color(
              colorHelper.colorList.sortBy(c => c.getRed).lift(rankFilter.chooseElement(colorHelper.colorList.length)).get.getRed,
              colorHelper.colorList.sortBy(c => c.getGreen).lift(rankFilter.chooseElement(colorHelper.colorList.length)).get.getGreen,
              colorHelper.colorList.sortBy(c => c.getBlue).lift(rankFilter.chooseElement(colorHelper.colorList.length)).get.getBlue
            ).getRGB)
        }
      }
    }
    newImage
  }
}
