package pl.zaw.image.operations

import java.awt.Color
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.image.util.SegmentationUtil

/**
 * Object that can calculate various segments parameters.
 * Created on 2015-05-21.
 *
 * @author Jakub Zawislak
 */
object Moments {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def calculateParams(segment: (BufferedImage, Array[Array[(Int, Color)]], Int), logParams: Boolean) = segment match {
    case (_, allSegmentsArray: Array[Array[(Int, _)]], segmentsCount: Int) =>
      //m parameters 2D array for all segments
      val mArray = Array.fill(segmentsCount)(
        Array(
          Array(0D, 0D, 0D, 0D),
          Array(0D, 0D, 0D, 0D),
          Array(0D, 0D, 0D, 0D),
          Array(0D, 0D, 0D, 0D)
        ))
      val areaArray = Array.fill(segmentsCount)(0D)
      val perimeterArray = Array.fill(segmentsCount)(0D)
      val minMaxDistArray = Array.fill(segmentsCount)(Double.MaxValue, 0D)
      //min row, max row, min col, max col
      val minMaxRowColArray = Array.fill(segmentsCount)(Double.MaxValue, 0D, Double.MaxValue, 0D)
      //helpers values
      val width = allSegmentsArray(0).length
      val height = allSegmentsArray.length

      //calculating area, perimeter and the smallest m
      for {
        row <- 0 until height
        col <- 0 until width
        value = allSegmentsArray(row)(col)._1
      } {
        //check if pixel belongs to segment
        if (value >= 0) {
          //area
          areaArray(value) = areaArray(value) + 1
          //perimeter
          //check surrounding pixels
          if (checkIfEdgePixel(allSegmentsArray, row, col)) {
            //this is edge pixel
            perimeterArray(value) = perimeterArray(value) + 1
          }

          val mParameters = mArray(value)
          for {
            mRow <- 0 until mParameters.length
            mCol <- 0 until mParameters(0).length
          } {
            mParameters(mRow)(mCol) = mParameters(mRow)(mCol) + Math.pow(row, mRow) * Math.pow(col, mCol)
          }
        }
      }

      //calculating middle m and central moments
      val mMiddle = for {
        m <- mArray
      } yield {
          val i = m(1)(0) / m(0)(0)
          val j = m(0)(1) / m(0)(0)

          Array(
            Array(m(0)(0),
              0,
              m(0)(2) - m(0)(1) * m(0)(1) / m(0)(0),
              m(0)(3) - 3 * m(0)(2) * j + 2 * m(0)(1) * j * j),
            Array(0,
              m(1)(1) - m(1)(0) * m(0)(1) / m(0)(0),
              m(1)(2) - 2 * m(1)(1) * j - m(0)(2) * i + 2 * m(1)(0) * j * j,
              0),
            Array(m(2)(0) - m(1)(0) * m(1)(0) / m(0)(0),
              m(2)(1) - 2 * m(1)(1) * i - m(2)(0) * j + 2 * m(0)(1) * i * i,
              0,
              0),
            Array(m(3)(0) - 3 * m(2)(0) * i + 2 * m(1)(0) * i * i,
              i, //i (row) central moment as 3,1
              j, //j (column) central moment as 3,2
              0)
          )
        }

      //iterate over segments again to calculate
      //distances between edges and central moments
      for {
        row <- 0 until height
        col <- 0 until width
        value = allSegmentsArray(row)(col)._1
        if (value >= 0 && checkIfEdgePixel(allSegmentsArray, row, col))
      } {
        val dist = Math.sqrt(Math.pow(mMiddle(value)(3)(1) - row, 2) + Math.pow(mMiddle(value)(3)(2) - col, 2))
        minMaxDistArray(value) = Math.min(minMaxDistArray(value)._1, dist) -> Math.max(minMaxDistArray(value)._2, dist)

        minMaxRowColArray(value) = (
          Math.min(minMaxRowColArray(value)._1, row),
          Math.max(minMaxRowColArray(value)._2, row),
          Math.min(minMaxRowColArray(value)._3, col),
          Math.max(minMaxRowColArray(value)._4, col)
          )
      }

      //calculating last M parameters and W parameters
      //printing also helpful values at the end
      var segCount = -1
      val mLast = for {
        mc <- mMiddle
      } yield {
          segCount = segCount + 1
          Array(
            (mc(2)(0) + mc(0)(2)) / Math.pow(mc(0)(0), 2), //0
            (Math.pow(mc(2)(0) - mc(0)(2), 2) + 4 * Math.pow(mc(1)(1), 2)) / Math.pow(mc(0)(0), 4), //1
            (Math.pow(mc(3)(0) - 3 * mc(1)(2), 2) + Math.pow(3 * mc(2)(1) - mc(0)(3), 2)) / Math.pow(mc(0)(0), 5), //2
            (Math.pow(mc(3)(0) + mc(1)(2), 2) + Math.pow(mc(2)(1) + mc(0)(3), 2)) / Math.pow(mc(0)(0), 5), //3
            ((mc(3)(0) - 3 * mc(1)(2)) * (mc(3)(0) + mc(1)(2))
              * (Math.pow(mc(3)(0) + mc(1)(2), 2) - 3 * Math.pow(mc(2)(1) + mc(0)(3), 2)) + (3 * mc(2)(1) - mc(0)(3))
              * (mc(2)(1) + mc(0)(3)) * (3 * Math.pow(mc(3)(0) + mc(1)(2), 2) - Math.pow(mc(2)(1) + mc(0)(3), 2)))
              / Math.pow(mc(0)(0), 10), //4
            ((mc(2)(0) - mc(0)(2)) * (Math.pow(mc(3)(0) + mc(1)(2), 2) - Math.pow(mc(2)(1) + mc(0)(3), 2)) + 4 * mc(1)(1)
              * (mc(3)(0) + mc(1)(2)) * (mc(2)(1) + mc(0)(3)))
              / Math.pow(mc(0)(0), 7), //5
            (mc(2)(0) * mc(0)(2) - Math.pow(mc(1)(1), 2)) / Math.pow(mc(0)(0), 4), //6
            (mc(3)(0) * mc(1)(2) + mc(2)(1) * mc(0)(3) - Math.pow(mc(1)(2), 2) - Math.pow(mc(2)(1), 2)) / Math.pow(mc(0)(0), 5), //7
            (mc(2)(0) * (mc(2)(1) * mc(0)(3) - mc(1)(2) * mc(1)(2)) + mc(0)(2) * (mc(0)(3) * mc(1)(2) - Math.pow(mc(2)(1), 2)) - mc(1)(1)
              * (mc(3)(0) * mc(0)(3) - mc(2)(1) * mc(1)(2)))
              / Math.pow(mc(0)(0), 7), //8
            (Math.pow(mc(3)(0) * mc(0)(3) - mc(1)(2) * mc(2)(1), 2) - 4 * (mc(3)(0) * mc(1)(2) - Math.pow(mc(2)(1), 2))
              * (mc(0)(3) * mc(2)(1) - mc(1)(2)))
              / Math.pow(mc(0)(0), 10), //9
            //W Parameters
            //W1 as 10
            2 * Math.sqrt(areaArray(segCount) / Math.PI),
            //W2 as 11
            perimeterArray(segCount) / Math.PI,
            //W3 as 12
            perimeterArray(segCount) / (2 * Math.sqrt(Math.PI * areaArray(segCount))) - 1,
            //W7 as 13
            minMaxDistArray(segCount)._1 / minMaxDistArray(segCount)._2,
            //W8 as 14
            Math.max(minMaxRowColArray(segCount)._2 - minMaxRowColArray(segCount)._1, minMaxRowColArray(segCount)._4 - minMaxRowColArray(segCount)._3)
              / perimeterArray(segCount),
            //W9 as 15
            (2 * Math.sqrt(Math.PI * areaArray(segCount))) / perimeterArray(segCount),


            minMaxDistArray(segCount)._1,
            minMaxDistArray(segCount)._2, // as 17
            minMaxRowColArray(segCount)._1,
            minMaxRowColArray(segCount)._2,
            minMaxRowColArray(segCount)._3,
            minMaxRowColArray(segCount)._4,
            //row central moment as 22
            mc(3)(1),
            //column central moment as 23
            mc(3)(2),
            //area as 24
            areaArray(segCount),
            //perimeter as 25
            perimeterArray(segCount)
          )
        }

      if (logParams) {
        SegmentationUtil.logSegmentsToFile(mLast)
      }
      mLast
  }

  def checkIfEdgePixel(allSegmentsArray: Array[Array[(Int, Color)]], row: Int, col: Int): Boolean = {
    val width = allSegmentsArray(0).length
    val height = allSegmentsArray.length
    val value = allSegmentsArray(row)(col)._1

    if ((row + 1 <= height - 1 && (
      allSegmentsArray(row + 1)(col)._1 != value
        || (col + 1 <= width - 1 && allSegmentsArray(row + 1)(col + 1)._1 != value)
        || (col - 1 >= 0 && allSegmentsArray(row + 1)(col - 1)._1 != value)))
      ||
      (row - 1 >= 0 && (
        allSegmentsArray(row - 1)(col)._1 != value
          || (col + 1 <= width - 1 && allSegmentsArray(row - 1)(col + 1)._1 != value)
          || (col - 1 >= 0 && allSegmentsArray(row - 1)(col - 1)._1 != value)
        ))
      ||
      (col + 1 <= width - 1 && allSegmentsArray(row)(col + 1)._1 != value)
      || (col - 1 >= 0 && allSegmentsArray(row)(col - 1)._1 != value)
    ) true
    else
      false
  }
}
