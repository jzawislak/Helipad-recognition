package pl.zaw.image.operations

import java.awt.Color
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.core.config.ConfigUtil
import pl.zaw.core.config.Implicits._
import pl.zaw.image.util.ColorHelper

import scala.collection.mutable.ListBuffer

/**
 * Created on 2015-05-20.
 *
 * @author Jakub Zawislak
 */
object Segmentation {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  /**
   * Performs segmentation of black and white image.
   * @param bufferedImage image for segmentation
   */
  def getSegments(bufferedImage: BufferedImage) = {
    logger.info("Calculating segments from black&white.")
    var segmentCount = -1
    val allSegments = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    val allSegmentsArray = Array.ofDim[(Int, Color)](allSegments.getHeight, allSegments.getWidth)
    //list of (col, row)
    val candidatesList = new ListBuffer[(Int, Int)]
    var newColor = ColorHelper.getRandomColor

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
        newColor = ColorHelper.getRandomColor
      }
      while (candidatesList.nonEmpty) {
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
    logger.info(s"Found ${segmentCount + 1} segments")
    (allSegments, allSegmentsArray, segmentCount + 1)
  }

  /**
   * Performs segmentation of black and white image.
   * @param bufferedImage image for segmentation
   */
  def getSegmentsColor(bufferedImage: BufferedImage) = {
    logger.info("Calculating segments from color.")
    var segmentCount = -1
    val allSegments = new BufferedImage(bufferedImage.getWidth, bufferedImage.getHeight, bufferedImage.getType)
    val allSegmentsArray = Array.ofDim[(Int, Color)](allSegments.getHeight, allSegments.getWidth)
    //list of (col, row)
    val candidatesList = new ListBuffer[((Int, Int), Color)]
    var newColor = ColorHelper.getRandomColor

    //copy old image and initialize array of segments
    for {
      row <- 0 to allSegments.getHeight - 1
      col <- 0 to allSegments.getWidth - 1
      color = new Color(bufferedImage.getRGB(col, row))
    } {
      allSegments.setRGB(col, row, color.getRGB)
      //all pixels can be candidates
      allSegmentsArray(row)(col) = -1 -> color
    }

    for {
      row <- 0 to allSegments.getHeight - 1
      col <- 0 to allSegments.getWidth - 1
    } {
      val status = allSegmentsArray(row)(col)._1
      if (status == -1) {
        candidatesList.append((col, row) -> allSegmentsArray(row)(col)._2)
        segmentCount = segmentCount + 1
        newColor = ColorHelper.getRandomColor
      }
      while (candidatesList.nonEmpty) {
        val head = candidatesList.remove(0)
        val headPosition = head._1
        val status2 = allSegmentsArray(headPosition._2)(headPosition._1)._1
        val color = allSegmentsArray(headPosition._2)(headPosition._1)._2
        val parentColor = head._2
        //pixel can be added to segment
        if (status2 == -1
          && Math.abs(parentColor.getRed - color.getRed) <= ConfigUtil.get[Int]("color.segmentation_rgb_diff").getOrElse(2)
          && Math.abs(parentColor.getGreen - color.getGreen) <= ConfigUtil.get[Int]("color.segmentation_rgb_diff").getOrElse(2)
          && Math.abs(parentColor.getBlue - color.getBlue) <= ConfigUtil.get[Int]("color.segmentation_rgb_diff").getOrElse(2)
        ) {
          allSegmentsArray(headPosition._2)(headPosition._1) = segmentCount -> color
          allSegments.setRGB(headPosition._1, headPosition._2, newColor.getRGB)

          //add surrounding candidates
          if (headPosition._1 + 1 <= allSegments.getWidth - 1) {
            candidatesList.append((headPosition._1 + 1, headPosition._2) -> color)
            if (headPosition._2 + 1 <= allSegments.getHeight - 1) candidatesList.append((headPosition._1 + 1, headPosition._2 + 1) -> color)
            if (headPosition._2 - 1 >= 0) candidatesList.append((headPosition._1 + 1, headPosition._2 - 1) -> color)
          }
          if (headPosition._1 - 1 >= 0) {
            candidatesList.append((headPosition._1 - 1, headPosition._2) -> color)
            if (headPosition._2 + 1 <= allSegments.getHeight - 1) candidatesList.append((headPosition._1 - 1, headPosition._2 + 1) -> color)
            if (headPosition._2 - 1 >= 0) candidatesList.append((headPosition._1 - 1, headPosition._2 - 1) -> color)
          }
          if (headPosition._2 - 1 >= 0) candidatesList.append((headPosition._1, headPosition._2 - 1) -> color)
          if (headPosition._2 + 1 <= allSegments.getHeight - 1) candidatesList.append((headPosition._1, headPosition._2 + 1) -> color)
        }
      }
    }
    logger.info(s"Found ${segmentCount + 1} segments")
    (allSegments, allSegmentsArray, segmentCount + 1)
  }
}