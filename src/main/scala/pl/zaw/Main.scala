package pl.zaw
import  pl.zaw.window._
/**
 * Created on 2015-05-12.
 *
 * @author Jakub Zawislak
 */
object Main {
  def main(args: Array[String]) {
    val mainWindow = MainWindow
    val startupArray = Array[String]()
    mainWindow.startup(startupArray)
  }
}
