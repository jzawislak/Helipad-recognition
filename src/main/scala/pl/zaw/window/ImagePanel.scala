package pl.zaw.window

import java.awt.Graphics2D
import java.awt.image.BufferedImage
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
  private var _bufferedImage: BufferedImage = null

  def path_=(newPath: String) = {
    _path = newPath
    _bufferedImage = ImageIO.read(new File(_path))
    repaint()
  }

  def path = _path

  def applyFilter(filter: BufferedImage => BufferedImage): BufferedImage = {
    //_bufferedImage = filter(_bufferedImage)
    //repaint()
    filter(_bufferedImage)
  }

  def bufferedImage_=(bufferedImage: BufferedImage) = {
    _bufferedImage = bufferedImage
    repaint()
  }

  def bufferedImage = _bufferedImage

  override def paintComponent(g: Graphics2D) = {
    if (null != _bufferedImage) g.drawImage(_bufferedImage, 0, 0, size.getWidth.toInt, size.getHeight.toInt, null)
  }
}
