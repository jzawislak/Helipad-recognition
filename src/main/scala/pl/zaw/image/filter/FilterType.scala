package pl.zaw.image.filter

/**
 * Created on 2015-05-16.
 * Based on http://www.algorytm.org/przetwarzanie-obrazow/filtrowanie-obrazow.html
 * @author Jakub Zawislak
 */
sealed trait FilterType {
  def size: Int

  def title: String
}

sealed trait MaskFilter extends FilterType {
  def mask: Array[Array[Int]]
}

sealed trait RankFilter extends FilterType {
  def chooseElement(size: Int): Int
}

//Low pass Filters
case object AverageFilter3 extends MaskFilter {
  val title = "3x3 Average"
  val size = 3
  val mask = Array(
    Array(1, 1, 1),
    Array(1, 1, 1),
    Array(1, 1, 1)
  )
}

case object LP1Filter3 extends MaskFilter {
  val title = "3x3 LP1"
  val size = 3
  val mask = Array(
    Array(1, 1, 1),
    Array(1, 2, 1),
    Array(1, 1, 1)
  )
}

case object LP2Filter3 extends MaskFilter {
  val title = "3x3 LP2"
  val size = 3
  val mask = Array(
    Array(1, 1, 1),
    Array(1, 4, 1),
    Array(1, 1, 1)
  )
}

case object LP3Filter3 extends MaskFilter {
  val title = "3x3 LP3"
  val size = 3
  val mask = Array(
    Array(1, 1, 1),
    Array(1, 12, 1),
    Array(1, 1, 1)
  )
}

case object Gauss1Filter3 extends MaskFilter {
  val title = "3x3 Gauss 1"
  val size = 3
  val mask = Array(
    Array(1, 2, 1),
    Array(2, 4, 2),
    Array(1, 2, 1)
  )
}

case object AverageFilter5 extends MaskFilter {
  val title = "5x5 Average"
  val size = 5
  val mask = Array(
    Array(1, 1, 1, 1, 1),
    Array(1, 1, 1, 1, 1),
    Array(1, 1, 1, 1, 1),
    Array(1, 1, 1, 1, 1),
    Array(1, 1, 1, 1, 1)
  )
}

case object CircularFilter5 extends MaskFilter {
  val title = "5x5 Circular"
  val size = 5
  val mask = Array(
    Array(0, 1, 1, 1, 0),
    Array(1, 1, 1, 1, 1),
    Array(1, 1, 1, 1, 1),
    Array(1, 1, 1, 1, 1),
    Array(0, 1, 1, 1, 0)
  )
}

case object PyramidFilter5 extends MaskFilter {
  val title = "5x5 Pyramid"
  val size = 5
  val mask = Array(
    Array(1, 2, 3, 2, 1),
    Array(2, 4, 6, 4, 2),
    Array(3, 6, 9, 6, 3),
    Array(2, 4, 6, 4, 2),
    Array(1, 2, 3, 2, 1)
  )
}

case object ConeFilter5 extends MaskFilter {
  val title = "5x5 Pyramid"
  val size = 5
  val mask = Array(
    Array(0, 0, 1, 0, 0),
    Array(0, 2, 2, 2, 0),
    Array(1, 2, 5, 2, 1),
    Array(0, 2, 2, 2, 0),
    Array(0, 0, 1, 0, 0)
  )
}

case object Gauss2Filter5 extends MaskFilter {
  val title = "5x5 Gauss 2"
  val size = 5
  val mask = Array(
    Array(1, 1, 2, 1, 1),
    Array(1, 2, 4, 2, 1),
    Array(2, 4, 8, 4, 2),
    Array(1, 2, 4, 2, 1),
    Array(1, 1, 2, 1, 1)
  )
}

case object Gauss3Filter5 extends MaskFilter {
  val title = "5x5 Gauss 3"
  val size = 5
  val mask = Array(
    Array(0, 1, 2, 1, 0),
    Array(1, 4, 8, 4, 1),
    Array(2, 8, 16, 8, 2),
    Array(1, 4, 8, 4, 1),
    Array(0, 1, 2, 1, 0)
  )
}

case object Gauss4Filter5 extends MaskFilter {
  val title = "5x5 Gauss 4"
  val size = 5
  val mask = Array(
    Array(1, 4, 7, 4, 1),
    Array(4, 16, 26, 16, 4),
    Array(7, 26, 41, 26, 7),
    Array(4, 26, 16, 26, 4),
    Array(1, 4, 7, 4, 1)
  )
}

case object Gauss5Filter7 extends MaskFilter {
  val title = "7x7 Gauss 5"
  val size = 7
  val mask = Array(
    Array(1, 1, 2, 2, 2, 1, 1),
    Array(1, 2, 2, 4, 2, 2, 1),
    Array(2, 2, 4, 8, 4, 2, 2),
    Array(2, 4, 8, 16, 8, 4, 2),
    Array(2, 2, 4, 8, 4, 2, 2),
    Array(1, 2, 2, 4, 2, 2, 1),
    Array(1, 1, 2, 2, 2, 1, 1)
  )
}

//High pass Filters
case object MeanRemovalFilter3 extends MaskFilter {
  val title = "3x3 Mean Removal"
  val size = 3
  val mask = Array(
    Array(-1, -1, -1),
    Array(-1, 9, -1),
    Array(-1, -1, -1)
  )
}

case object HP1Filter3 extends MaskFilter {
  val title = "3x3 HP1"
  val size = 3
  val mask = Array(
    Array(0, -1, 0),
    Array(-1, 5, -1),
    Array(0, -1, 0)
  )
}

case object HP2Filter3 extends MaskFilter {
  val title = "3x3 HP2"
  val size = 3
  val mask = Array(
    Array(1, -2, 1),
    Array(-2, 5, -2),
    Array(1, -2, 1)
  )
}

case object HP3Filter3 extends MaskFilter {
  val title = "3x3 HP3"
  val size = 3
  val mask = Array(
    Array(0, -1, 0),
    Array(-1, 20, -1),
    Array(0, -1, 0)
  )
}

//Directional Filters
case object HorizontalFilter3 extends MaskFilter {
  val title = "3x3 Horizontal"
  val size = 3
  val mask = Array(
    Array(0, 0, 0),
    Array(-1, 1, 0),
    Array(0, 0, 0)
  )
}

case object VerticalFilter3 extends MaskFilter {
  val title = "3x3 Vertical"
  val size = 3
  val mask = Array(
    Array(0, -1, 0),
    Array(0, 1, 0),
    Array(0, 0, 0)
  )
}

case object UpperLeftFilter3 extends MaskFilter {
  val title = "3x3 Upper Left"
  val size = 3
  val mask = Array(
    Array(-1, 0, 0),
    Array(0, 1, 0),
    Array(0, 0, 0)
  )
}

case object UpperRightFilter3 extends MaskFilter {
  val title = "3x3 Upper Right"
  val size = 3
  val mask = Array(
    Array(0, 0, -1),
    Array(0, 1, 0),
    Array(0, 0, 0)
  )
}

case object EastFilter3 extends MaskFilter {
  val title = "3x3 Gradient East"
  val size = 3
  val mask = Array(
    Array(-1, 1, 1),
    Array(-1, -2, 1),
    Array(-1, 1, 1)
  )
}

case object NorthWestFilter3 extends MaskFilter {
  val title = "3x3 Gradient North West"
  val size = 3
  val mask = Array(
    Array(1, 1, 1),
    Array(1, -2, -1),
    Array(1, -1, -1)
  )
}

case object SouthFilter3 extends MaskFilter {
  val title = "3x3 Gradient South"
  val size = 3
  val mask = Array(
    Array(-1, -1, -1),
    Array(1, -2, 1),
    Array(1, 1, 1)
  )
}

//Embossing
case object NorthEmbossingFilter3 extends MaskFilter {
  val title = "3x3 North Embossing"
  val size = 3
  val mask = Array(
    Array(1, 1, 1),
    Array(0, 1, 0),
    Array(-1, -1, -1)
  )
}

case object SouthEastEmbossingFilter3 extends MaskFilter {
  val title = "3x3 South East Embossing"
  val size = 3
  val mask = Array(
    Array(-1, -1, 0),
    Array(-1, 1, 1),
    Array(0, 1, 1)
  )
}

case object WestEmbossingFilter3 extends MaskFilter {
  val title = "3x3 West Embossing"
  val size = 3
  val mask = Array(
    Array(1, 0, -1),
    Array(1, 1, -1),
    Array(1, 0, -1)
  )
}

//Laplace
case object Laplace1Filter3 extends MaskFilter {
  val title = "3x3 Laplace 1"
  val size = 3
  val mask = Array(
    Array(0, -1, 0),
    Array(-1, 4, -1),
    Array(0, -1, 0)
  )
}

case object Laplace2Filter3 extends MaskFilter {
  val title = "3x3 Laplace 2"
  val size = 3
  val mask = Array(
    Array(-1, -1, -1),
    Array(-1, 8, -1),
    Array(-1, -1, -1)
  )
}

case object Laplace3Filter3 extends MaskFilter {
  val title = "3x3 Laplace 3"
  val size = 3
  val mask = Array(
    Array(1, -2, 1),
    Array(-2, 4, -2),
    Array(1, -2, 1)
  )
}

case object Laplace4Filter3 extends MaskFilter {
  val title = "3x3 Laplace 4"
  val size = 3
  val mask = Array(
    Array(-1, 0, -1),
    Array(0, 4, 0),
    Array(-1, 0, -1)
  )
}

//Rank filters
case object MedianRankFilter3 extends RankFilter {
  val title = "3x3 Median"
  val size = 3

  override def chooseElement(size: Int): Int = {
    size / 2
  }
}

case object MinimumRankFilter3 extends RankFilter {
  val title = "3x3 Minimum"
  val size = 3

  override def chooseElement(size: Int): Int = {
    0
  }
}

case object MaximumRankFilter3 extends RankFilter {
  val title = "3x3 Maximum"
  val size = 3

  override def chooseElement(size: Int): Int = {
    size - 1
  }
}