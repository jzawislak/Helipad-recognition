package pl.zaw.window

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.image._

import scala.swing.BorderPanel.Position._
import scala.swing._

/**
 * Created on 2015-05-12.
 *
 * @author Jakub Zawislak
 */

class MainWindow extends SimpleSwingApplication {
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
      contents += new Menu("File") {
        contents += new MenuItem(
          Action("Open File") {
            chooseFile()
          })
        contents += new MenuItem(Action("Exit") {
          sys.exit(0)
        })
      }
      contents += new Menu("Filters") {
        contents += new Menu("Low Pass") {
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
        contents += new Menu("High Pass") {
          contents += new DefaultFilterMenuItem(MeanRemovalFilter3)
          contents += new DefaultFilterMenuItem(HP1Filter3)
          contents += new DefaultFilterMenuItem(HP2Filter3)
          contents += new DefaultFilterMenuItem(HP3Filter3)
        }
        contents += new Menu("Edge detection") {
          contents += new Menu("Directional/Gradient Directional") {
            contents += new DefaultFilterMenuItem(HorizontalFilter3)
            contents += new DefaultFilterMenuItem(VerticalFilter3)
            contents += new DefaultFilterMenuItem(UpperLeftFilter3)
            contents += new DefaultFilterMenuItem(UpperRightFilter3)
            contents += new DefaultFilterMenuItem(NorthWestFilter3)
            contents += new DefaultFilterMenuItem(EastFilter3)
            contents += new DefaultFilterMenuItem(SouthFilter3)
          }
          contents += new Menu("Embossing") {
            contents += new DefaultFilterMenuItem(NorthEmbossingFilter3)
            contents += new DefaultFilterMenuItem(SouthEastEmbossingFilter3)
            contents += new DefaultFilterMenuItem(WestEmbossingFilter3)
          }
          contents += new Menu("Laplace") {
            contents += new DefaultFilterMenuItem(Laplace1Filter3)
            contents += new DefaultFilterMenuItem(Laplace2Filter3)
            contents += new DefaultFilterMenuItem(Laplace3Filter3)
            contents += new DefaultFilterMenuItem(Laplace4Filter3)
          }
        }
        contents += new Menu("Ranking") {
          contents += new DefaultFilterMenuItem(MedianRankFilter3)
          contents += new DefaultFilterMenuItem(MinimumRankFilter3)
          contents += new DefaultFilterMenuItem(MaximumRankFilter3)
        }
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

  class DefaultFilterMenuItem(filterType: FilterType) extends MenuItem(
    Action(filterType.title) {
      additionalWindow.bufferedImage = Filter.filter(imagePanel.bufferedImage, filterType)
      logger.info(s"Applied: ${filterType.title}")
    }
  )

}