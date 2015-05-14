package pl.zaw.window

import java.awt.Dimension
import java.io.File

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.swing.BorderPanel.Position._
import scala.swing._

/**
 * Created on 2015-05-12.
 *
 * @author Jakub Zawislak
 */

object MainWindow extends SimpleSwingApplication {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  val preferredDimension = new Dimension(640, 480)
  //Some components
  val imagePanel = new ImagePanel {
    preferredSize = preferredDimension
  }

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
}