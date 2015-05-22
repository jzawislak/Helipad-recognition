package pl.zaw.image.segmentation

import java.awt.Color
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.image.util.ColorHelper

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
    var segmentCount = -1
    val allSegments = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    val allSegmentsArray = Array.ofDim[(Int, Color)](allSegments.getHeight, allSegments.getWidth)
    //list of (col, row)
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
      allSegmentsArray(row)(col) = (if (color.getRGB == ColorHelper.BLACK) -1 else -2) -> color
    }

    for {
      row <- 0 to allSegments.getHeight - 1
      col <- 0 to allSegments.getWidth - 1
    } {
      val status = allSegmentsArray(row)(col)._1
      if (status == -1) {
        candidatesList.append((col, row))
        segmentCount = segmentCount + 1
        newColor = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255))
      }
      while (candidatesList.size != 0) {
        val head = candidatesList.remove(0)
        val status2 = allSegmentsArray(head._2)(head._1)._1
        val color = allSegmentsArray(head._2)(head._1)._2
        //pixel can be added to segment
        if (status2 == -1) {
          allSegmentsArray(head._2)(head._1) = segmentCount -> color
          allSegments.setRGB(head._1, head._2, newColor.getRGB)

          //add surrounding candidates
          if (head._1 + 1 <= allSegments.getWidth - 1) {
            candidatesList.append((head._1 + 1, head._2))
            if (head._2 + 1 <= allSegments.getHeight - 1) candidatesList.append((head._1 + 1, head._2 + 1))
            if (head._2 - 1 >= 0) candidatesList.append((head._1 + 1, head._2 - 1))
          }
          if (head._1 - 1 >= 0) {
            candidatesList.append((head._1 - 1, head._2))
            if (head._2 + 1 <= allSegments.getHeight - 1) candidatesList.append((head._1 - 1, head._2 + 1))
            if (head._2 - 1 >= 0) candidatesList.append((head._1 - 1, head._2 - 1))
          }
          if (head._2 - 1 >= 0) candidatesList.append((head._1, head._2 - 1))
          if (head._2 + 1 <= allSegments.getHeight - 1) candidatesList.append((head._1, head._2 + 1))
        }
      }
    }
    (allSegments, allSegmentsArray, segmentCount + 1)
  }
}