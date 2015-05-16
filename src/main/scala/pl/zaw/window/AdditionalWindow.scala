package pl.zaw.window

import java.awt.image.BufferedImage
import java.awt.{Dimension, Point}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.swing.BorderPanel.Position._
import scala.swing._

/**
 * Created on 2015-05-14.
 *
 * @author Jakub Zawislak
 */
class AdditionalWindow(mainWindow: MainWindow) extends SimpleSwingApplication {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  val preferredDimension = new Dimension(640, 480)

  val imagePanel = new ImagePanel {
    preferredSize = preferredDimension
  }

  def top = new MainFrame {
    title = "Helipad recognition helper"
    preferredSize = preferredDimension
    location = new Point(640, 0)
    visible = false

    //Components layout
    contents = new BorderPanel {
      layout(imagePanel) = Center
    }

    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(
          Action("Set as default") {
            mainWindow.bufferedImage = imagePanel.bufferedImage
          })
        contents += new MenuItem(Action("Close") {
          visible = false
        })
      }
    }
  }

  def bufferedImage_=(newBufferedImage: BufferedImage) = {
    imagePanel.bufferedImage = newBufferedImage
    //top.visible
  }

  def bufferedImage = imagePanel.bufferedImage
}
