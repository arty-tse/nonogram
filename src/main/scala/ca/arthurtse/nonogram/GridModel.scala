package ca.arthurtse.nonogram

import Array._

class GridModel(val rows: Int, val cols: Int, var rowHints: Array[Array[Int]], var colHints: Array[Array[Int]]) {
  var grid: Array[Array[Cell]] = ofDim[Cell](rows, cols)
  for (i <- 0 until rows) {
      for (j <- 0 until cols) {
        grid(i)(j) = Unknown
      }
  }

  sealed trait Cell
  case object Unknown extends Cell
  case object Box extends Cell
  case object Empty extends Cell
}
