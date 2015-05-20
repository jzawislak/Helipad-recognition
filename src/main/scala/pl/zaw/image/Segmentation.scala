package pl.zaw.image

import java.awt.Color
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

/**
 * Created on 2015-05-20.
 *
 * @author Jakub Zawislak
 */
object Segmentation {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  val random = new scala.util.Random(1)

  /**
   * Performs segmentation of black and white image.
   * @param bufferedImage image for segmentation
   */
  def getSegments(bufferedImage: BufferedImage) = {
    var segmentCount = 0
    val allSegments = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    val allSegmentsArray = Array.ofDim[(Int, Color)](allSegments.getWidth, allSegments.getHeight)
    val candidatesList = new ListBuffer[(Int, Int)]
    var newColor = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))

    //copy old image and initialize array of segments
    for {
      row <- 0 to allSegments.getHeight - 1
      col <- 0 to allSegments.getWidth - 1
      color = new Color(bufferedImage.getRGB(col, row))
    } {
      allSegments.setRGB(col, row, color.getRGB)
      //only black pixels can be candidates
      allSegmentsArray(col)(row) = (if (color.getRGB == ColorHelper.BLACK) -1 else -2) -> color
    }

    for {
      row <- 0 to allSegments.getHeight - 1
      col <- 0 to allSegments.getWidth - 1
      color = allSegmentsArray(col)(row)._2
      status = allSegmentsArray(col)(row)._1
    } {
      if (status == -1) {
        candidatesList.append((col, row))
        segmentCount = segmentCount + 1
        newColor = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))
      }
      while (candidatesList.size != 0) {
        val head = candidatesList.remove(0)
        val status = allSegmentsArray(head._1)(head._2)._1
        val color = allSegmentsArray(head._1)(head._2)._2
        //pixel can be added to segment
        if (status == -1) {
          allSegmentsArray(head._1)(head._2) = segmentCount -> color
          allSegments.setRGB(head._1, head._2, newColor.getRGB)

          //add surrounding candidates
          if (head._1 + 1 < allSegments.getWidth - 1) {
            candidatesList.append((head._1 + 1, head._2))
            if (head._2 + 1 < allSegments.getHeight - 1) candidatesList.append((head._1 + 1, head._2 + 1))
            if (head._2 - 1 > 0) candidatesList.append((head._1 + 1, head._2 - 1))
          }
          if (head._1 - 1 > 0) {
            candidatesList.append((head._1 - 1, head._2))
            if (head._2 + 1 < allSegments.getHeight - 1) candidatesList.append((head._1 - 1, head._2 + 1))
            if (head._2 - 1 > 0) candidatesList.append((head._1 - 1, head._2 - 1))
          }
          if (head._2 - 1 > 0) candidatesList.append((head._1, head._2 - 1))
          if (head._2 + 1 < allSegments.getHeight - 1) candidatesList.append((head._1, head._2 + 1))
        }
      }
    }
    allSegments
  }
}
