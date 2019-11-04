package ca.arthurtse.nonogram.menu

import ca.arthurtse.nonogram.menu.PuzzleData.{Legend, Puzzle}

class PuzzleData {
  val legend: Legend = null
  val puzzles: Array[Puzzle] = null
}

object PuzzleData {

  class Legend {
    val filled: Char = ' '
    val empty: Char = ' '
  }

  class Puzzle {
    val name: String = null
    val rows: Int = 0
    val cols: Int = 0
    val rowHints: Array[Array[Int]] = null
    val colHints: Array[Array[Int]] = null
    val solution: Array[Array[Char]] = null
  }

}
