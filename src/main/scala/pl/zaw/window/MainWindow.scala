package pl.zaw.window

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ToolTipManager

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.core.config.ConfigUtil
import pl.zaw.core.config.Implicits._
import pl.zaw.image.operations._
import pl.zaw.image.operations.filter._
import pl.zaw.image.util.SegmentationUtil

import scala.swing.BorderPanel.Position._
import scala.swing._
import scala.swing.event.{MouseEntered, MouseExited}

/**
 * Created on 2015-05-12.
 *
 * @author Jakub Zawislak
 */

class MainWindow extends SimpleSwingApplication {
  ConfigUtil.init("Helipad")
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  val preferredDimension = new Dimension(640, 480)
  //Some components
  val imagePanel = new ImagePanel {
    preferredSize = preferredDimension
  }

  val additionalWindow = new AdditionalWindow(this)
  additionalWindow.startup(Array[String]())

  def top = new MainFrame {
    title = "Helipad recognition"
    preferredSize = preferredDimension

    //Some components

    //Components layout
    contents = new BorderPanel {
      layout(imagePanel) = Center
    }

    menuBar = new MenuBar {
      contents += new DefaultMenu("File") {
        contents += new DefaultMenuItem(
          Action("Open File") {
            chooseFile()
          })
        contents += new DefaultMenuItem(
          Action("Save File") {
            if (bufferedImage != null) {
              val file = new File("./temp.png")
              ImageIO.write(bufferedImage, "png", file)
            }
          }) {
          tooltip = "Save current image to ./temp.png."
        }
        contents += new DefaultMenuItem(Action("Exit") {
          sys.exit(0)
        })
      }
      contents += new DefaultMenu("Filters") {
        tooltip = "Apply various filters to image."
        contents += new DefaultMenu("Low Pass") {
          contents += new DefaultFilterMenuItem(AverageFilter3)
          contents += new DefaultFilterMenuItem(LP1Filter3)
          contents += new DefaultFilterMenuItem(LP2Filter3)
          contents += new DefaultFilterMenuItem(LP3Filter3)
          contents += new DefaultFilterMenuItem(Gauss1Filter3)
          contents += new Separator()
          contents += new DefaultFilterMenuItem(AverageFilter5)
          contents += new DefaultFilterMenuItem(CircularFilter5)
          contents += new DefaultFilterMenuItem(PyramidFilter5)
          contents += new DefaultFilterMenuItem(ConeFilter5)
          contents += new DefaultFilterMenuItem(Gauss2Filter5)
          contents += new DefaultFilterMenuItem(Gauss3Filter5)
          contents += new DefaultFilterMenuItem(Gauss4Filter5)
          contents += new Separator()
          contents += new DefaultFilterMenuItem(Gauss5Filter7)
        }
        contents += new DefaultMenu("High Pass") {
          contents += new DefaultFilterMenuItem(MeanRemovalFilter3)
          contents += new DefaultFilterMenuItem(HP1Filter3)
          contents += new DefaultFilterMenuItem(HP2Filter3)
          contents += new DefaultFilterMenuItem(HP3Filter3)
        }
        contents += new DefaultMenu("Edge detection") {
          contents += new DefaultMenu("Directional/Gradient Directional") {
            contents += new DefaultFilterMenuItem(HorizontalFilter3)
            contents += new DefaultFilterMenuItem(VerticalFilter3)
            contents += new DefaultFilterMenuItem(UpperLeftFilter3)
            contents += new DefaultFilterMenuItem(UpperRightFilter3)
            contents += new DefaultFilterMenuItem(NorthWestFilter3)
            contents += new DefaultFilterMenuItem(EastFilter3)
            contents += new DefaultFilterMenuItem(SouthFilter3)
          }
          contents += new DefaultMenu("Embossing") {
            contents += new DefaultFilterMenuItem(NorthEmbossingFilter3)
            contents += new DefaultFilterMenuItem(SouthEastEmbossingFilter3)
            contents += new DefaultFilterMenuItem(WestEmbossingFilter3)
          }
          contents += new DefaultMenu("Laplace") {
            contents += new DefaultFilterMenuItem(Laplace1Filter3)
            contents += new DefaultFilterMenuItem(Laplace2Filter3)
            contents += new DefaultFilterMenuItem(Laplace3Filter3)
            contents += new DefaultFilterMenuItem(Laplace4Filter3)
          }
        }
        contents += new DefaultMenu("Ranking") {
          contents += new DefaultFilterMenuItem(MedianRankFilter3)
          contents += new DefaultFilterMenuItem(MinimumRankFilter3)
          contents += new DefaultFilterMenuItem(MaximumRankFilter3)
        }
      }
      contents += new DefaultMenu("Threshold") {
        contents += new DefaultMenu("Absolute yellow") {
          tooltip = "Channels red and green between selected value and 255. Min=0, max=255."
          for (i <- 0 to 250 by 10) {
            contents += new AbsoluteThresholdMenuItem(s"Yellow $i", redLimit = (i, 255), greenLimit = (i, 255), blueLimit = (0, ConfigUtil.get[Int]("threshold.blue_threshold").getOrElse(170))) {
            }
          }
        }
        contents += new DefaultMenu("Absolute white") {
          tooltip = "All channels between selected value and 255. Min=0, max=255."
          for (i <- 0 to 250 by 10) {
            contents += new AbsoluteThresholdMenuItem(s"Yellow $i", redLimit = (i, 255), greenLimit = (i, 255), blueLimit = (i, 255)) {
            }
          }
        }
        /*
        contents += new Menu("Relative yellow") {
            contents += new RelativeThresholdMenuItem(s"Yellow 1.2-1.4", redLimit = (2.2, 10.4), greenLimit = (2.2, 10.4), blueLimit = (0, 20)) {
              tooltip =
                """<html> Sets relative threshold limit for channels red and green (summary yellow). <br>
                  | Firstly average brightness is calculated and then based on this average threshold is made.
                  | Do not use it because it does not work.
                  | </html>
                """.stripMargin
            }
        }
        */
      }
      contents += new DefaultMenu("Gamma correction") {
        for (i <- 0D to 4D by 0.2) {
          contents += new DefaultGammaMenuItem(i) {
          }
        }
      }
      contents += new DefaultMenu("Segmentation") {
        contents += new DefaultMenuItem(
          Action("Area growth") {
            additionalWindow.bufferedImage = Segmentation.getSegments(imagePanel.bufferedImage)._1
            logger.info(s"Segmentation applied")
          })
        contents += new DefaultMenuItem(
          Action("Area growth with m-params") {
            val tup = Segmentation.getSegments(imagePanel.bufferedImage)
            additionalWindow.bufferedImage = tup._1
            val mParams = Moments.calculateParams(tup, logParams = true)
            logger.info(s"Segmentation with m-params applied")
          }) {
          tooltip = s"<html> The difference are calculated params (shape descriptors) that are saved to ${SegmentationUtil.LOG_FILENAME} file. <br>" +
            "This should be used only for debugging.<html>"
        }
        contents += new DefaultMenuItem(
          Action("Area growth color") {
            additionalWindow.bufferedImage = Segmentation.getSegmentsColor(imagePanel.bufferedImage)._1
            logger.info(s"Color segmentation applied")
          }) {
          tooltip = s"Takes a long time and uses quite a lot of RAM memory!"
        }
      }
      contents += new DefaultMenu("Helipad detection") {
        contents += new DefaultMenuItem(
          Action("Detect H signs") {
            additionalWindow.bufferedImage = HelipadDetection.detectHelipad(imagePanel.bufferedImage, HelipadDetection.pathThresholdH)
            logger.info(s"H signs detected")
          })
        contents += new DefaultMenuItem(
          Action("Detect circles") {
            additionalWindow.bufferedImage = HelipadDetection.detectHelipad(imagePanel.bufferedImage, HelipadDetection.pathThresholdCircle)
            logger.info(s"Circles detected")
          })
        contents += new DefaultMenuItem(
          Action("Detect helipads") {
            additionalWindow.bufferedImage = HelipadDetection.detectHelipad(imagePanel.bufferedImage, HelipadDetection.pathThresholdHeli)
            logger.info(s"Helipads detected")
          })
        contents += new DefaultMenuItem(
          Action("Detect helipads with gamma 0.4 correction") {
            additionalWindow.bufferedImage = HelipadDetection.detectHelipad(imagePanel.bufferedImage, HelipadDetection.pathThresholdHeliWithGamma)
            logger.info(s"Helipads detected")
          })
        contents += new DefaultMenuItem(
          Action("Detect helipads with gamma 0.4 correction and single threshold") {
            additionalWindow.bufferedImage = HelipadDetection.detectHelipad(imagePanel.bufferedImage, HelipadDetection.pathSingleThresholdHeliWithGamma)
            logger.info(s"Helipads detected")
          })
        contents += new DefaultMenuItem(
          Action("Detect helipads color") {
            additionalWindow.bufferedImage = HelipadDetection.detectHelipad(imagePanel.bufferedImage, HelipadDetection.pathColorHeli)
            logger.info(s"Helipads from color detected")
          })
      }
    }
  }

  def chooseFile() = {
    val chooser = new FileChooser(new File(".")) {
      title = "Open File"
    }
    val result = chooser.showOpenDialog(null)
    if (result == FileChooser.Result.Approve) {
      logger.info("User has chosen file " + chooser.selectedFile)
      imagePanel.path = chooser.selectedFile.getPath
    }
  }

  def bufferedImage_=(newBufferedImage: BufferedImage) = {
    imagePanel.bufferedImage = newBufferedImage
  }

  def bufferedImage = imagePanel.bufferedImage

  trait NoDelayTrait {
    //hack for no delay in tooltip
    def removeDelay(publisher: Publisher) = {
      val defaultDismissTimeout = ToolTipManager.sharedInstance.getDismissDelay
      listenTo(publisher)
      reactions += {
        case MouseEntered(_, _, _) => ToolTipManager.sharedInstance().setInitialDelay(0)
        case MouseExited(_, _, _) => ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout)
      }
    }
  }

  abstract class DefaultMenu(title: String) extends Menu(title) with NoDelayTrait {
    removeDelay(mouse.moves)
  }

  class DefaultMenuItem(action: Action) extends MenuItem(action) with NoDelayTrait {
    removeDelay(mouse.moves)
  }

  class DefaultFilterMenuItem(filterType: FilterType) extends DefaultMenuItem(
    Action(filterType.title) {
      additionalWindow.bufferedImage = Filter.filter(imagePanel.bufferedImage, filterType)
      logger.info(s"Applied: ${filterType.title}")
    }
  )

  class DefaultGammaMenuItem(gamma: Double) extends DefaultMenuItem(
    Action(gamma.toString) {
      additionalWindow.bufferedImage = Gamma.applyCorrection(imagePanel.bufferedImage, gamma)
      logger.info(s"Applied gamma: $gamma")
    }
  )

  class AbsoluteThresholdMenuItem(title: String,
                                  redLimit: (Int, Int) = (0, 255),
                                  greenLimit: (Int, Int) = (0, 255),
                                  blueLimit: (Int, Int) = (0, 255)) extends DefaultMenuItem(
    Action(title) {
      additionalWindow.bufferedImage = Threshold.convertAbsolute(imagePanel.bufferedImage, redLimit, greenLimit, blueLimit)
      logger.info(s"Applied: $title")
    }
  )

  /**
   * Doesn't work at all.
   */
  /*
  @Deprecated
  class RelativeThresholdMenuItem(title: String,
                                  redLimit: (Double, Double) = (1, 1.1),
                                  greenLimit: (Double, Double) = (1, 1.1),
                                  blueLimit: (Double, Double) = (0, 2)) extends DefaultMenuItem(
    Action(title) {
      additionalWindow.bufferedImage = Threshold.convertRelative(imagePanel.bufferedImage, redLimit, greenLimit, blueLimit)
      logger.info(s"Applied: $title")
    }
  )
  */

}