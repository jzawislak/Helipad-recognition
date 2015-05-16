package pl.zaw.window

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import pl.zaw.image.Filter

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
        contents += new MenuItem(
          Action("Filter 1") {
            additionalWindow.bufferedImage = imagePanel.applyFilter(Filter.filter)
          })
        contents += new Separator()
        contents += new MenuItem(
          Action("Filter 2") {
            //chooseFile()
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

}