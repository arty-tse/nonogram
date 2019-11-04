package ca.arthurtse.nonogram.puzzle

import ca.arthurtse.nonogram.menu.PuzzleData.{Legend, Puzzle}
import scalafx.beans.property.ObjectProperty

class PuzzleModel(private val puzzle: Puzzle, private val legend: Legend) {
  val rows: Int = puzzle.rows
  val cols: Int = puzzle.cols
  val rowHints: Array[Array[Int]] = puzzle.rowHints
  val colHints: Array[Array[Int]] = puzzle.colHints
  val grid: Array[Array[ObjectProperty[TileState]]] = Array.fill(rows, cols)(ObjectProperty(Unknown))

  def status(row: Int, col: Int): ObjectProperty[TileState] = grid(row)(col)

  def update(row: Int, col: Int, state: TileState): Unit = grid(row)(col)() = state

  def checkSolution(): Boolean = {
    for (i <- 0 until rows) {
      for (j <- 0 until cols) {
        grid(i)(j)() match {
          case Filled => if (puzzle.solution(i)(j) != legend.filled) return false
          case _ => if (puzzle.solution(i)(j) != legend.empty) return false
        }
      }
    }
    true
  }
}
