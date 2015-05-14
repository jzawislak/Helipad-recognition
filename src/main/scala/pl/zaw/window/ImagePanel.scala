package pl.zaw.window

import java.awt.image.BufferedImage
import java.awt.{Dimension, Graphics2D}
import java.io.File
import javax.imageio.ImageIO

import scala.swing.Panel

/**
 * Created on 2015-05-13.
 *
 * @author Jakub Zawislak
 */
class ImagePanel extends Panel {
  private var _path = ""
  private var bufferedImage: BufferedImage = null

  def path_=(newPath: String) = {
    _path = newPath
    bufferedImage = ImageIO.read(new File(_path))
    repaint()
  }

  def path = _path

  override def paintComponent(g: Graphics2D) = {
    if (null != bufferedImage) g.drawImage(bufferedImage, 0, 0, size.getWidth.toInt, size.getHeight.toInt, null)
  }
}
