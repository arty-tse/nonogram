package ca.arthurtse.nonogram.model

import scalafx.beans.property.ObjectProperty

class GridModel(val rows: Int, val cols: Int, var rowHints: Array[Array[Int]], var colHints: Array[Array[Int]]) {
  var grid: Array[Array[ObjectProperty[TileState]]] = Array.fill(rows, cols)(ObjectProperty(Unknown))

  def status(row: Int, col: Int): ObjectProperty[TileState] = grid(row)(col)

  def update(row: Int, col: Int, state: TileState): Unit = grid(row)(col)() = state
}
