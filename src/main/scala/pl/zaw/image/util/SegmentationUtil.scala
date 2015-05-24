package pl.zaw.image.util

import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Created on 2015-05-24.
 *
 * @author Jakub Zawislak
 */
object SegmentationUtil {
  val LOG_FILENAME = "Moments_LOG.txt"
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  /**
   * Clears old content of a log file and saves new values.
   * @param segments segments/parameters to be written in file
   */
  def logSegmentsToFile(segments: Array[Array[Double]]) = {
    logger.info("Saving segments to file.")

    val logFile = new File(LOG_FILENAME)
    val out = new PrintWriter(logFile)
    var segmentCount: Int = 0
    out.write(s"Segment\tM1\tM2\tM3\tM4\tM5\tM6\tM7\tM8\tM9\tM10\tW1\tW2\tW3\tW7\tW8\tW9\t" +
      s"DistMin\tDistMax\tRowMin\tRowMax\tColMin\tColMax\tCentralRow\tCentralCol\t" +
      s"Area\tPerimeter\n")

    try {
      for {
        segment <- segments
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
}
