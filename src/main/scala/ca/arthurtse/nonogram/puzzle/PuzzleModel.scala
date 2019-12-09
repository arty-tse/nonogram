package ca.arthurtse.nonogram.puzzle

import ca.arthurtse.nonogram.NonogramApplication
import ca.arthurtse.nonogram.menu.PuzzleData.{Legend, Puzzle}
import ca.arthurtse.nonogram.puzzle.SaveFile.PuzzleSave
import ca.arthurtse.nonogram.puzzle.SaveFile.PuzzleSave.Rows
import ca.arthurtse.nonogram.puzzle.SaveFile.PuzzleSave.Rows.State._
import scalafx.beans.property.ObjectProperty

class PuzzleModel(private val puzzle: Puzzle, private val legend: Legend,
                  private val saveState: Option[SaveFile.PuzzleSave]) {
  val name: String = puzzle.name
  val rows: Int = puzzle.rows
  val cols: Int = puzzle.cols
  val rowHints: Array[Array[Int]] = puzzle.rowHints
  val colHints: Array[Array[Int]] = puzzle.colHints
  val grid: Array[Array[ObjectProperty[TileState]]] = saveState.fold(Array.fill(rows, cols)(ObjectProperty(Unknown: TileState)))(buildFromSaveFile)

  def status(row: Int, col: Int): ObjectProperty[TileState] = grid(row)(col)

  def update(row: Int, col: Int, state: TileState): Unit = {
    grid(row)(col)() = state
    NonogramApplication.updateSave(name, toSaveFile)
  }

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

  private def buildFromSaveFile(save: PuzzleSave): Array[Array[ObjectProperty[TileState]]] = {
    save.grid.toArray.map(_.states.toArray.map({
      case FILLED => ObjectProperty(Filled)
      case EMPTY => ObjectProperty(Empty)
      case UNKNOWN => ObjectProperty(Unknown)
    }))
  }

  def toSaveFile: PuzzleSave = {
    PuzzleSave().withGrid(grid.map(row => Rows().withStates(row.map(t => {
        t() match {
          case Filled => FILLED
          case Empty => EMPTY
          case Unknown => UNKNOWN
        }
      }).toSeq)).toSeq)
  }
}
