package ca.arthurtse.nonogram.model

import scalafx.collections.ObservableBuffer

class GridModel(val rows: Int, val cols: Int, var rowHints: Array[Array[Int]], var colHints: Array[Array[Int]]) {
  var grid: ObservableBuffer[ObservableBuffer[TileState]] = ObservableBuffer.fill(rows)(ObservableBuffer.fill(cols)(Unknown))

  def status(row:Int, col:Int): TileState = grid(row)(col)

  def update(row: Int, col: Int, state: TileState): Unit = grid(row)(col) = state
}
