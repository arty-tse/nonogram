package ca.arthurtse.nonogram.view

import ca.arthurtse.nonogram.model.PuzzleData
import ca.arthurtse.nonogram.model.PuzzleData.Puzzle
import scalafx.geometry.Insets
import scalafx.geometry.Orientation._
import scalafx.geometry.Pos.Center
import scalafx.scene.layout.{FlowPane, Pane, VBox}
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

class MenuView(puzzleData: PuzzleData, goToPuzzle: Puzzle => Unit) {
  val view: Pane = new FlowPane {
    id = "menu-pane"
    prefWrapLength = 600
  }
  for (puzzle <- puzzleData.puzzles) {
    val img = new Rectangle {
      width = 150
      height = 150
      styleClass += "puzzle-pic"
    }
    val name = new Text(puzzle.name) {
      styleClass += "puzzle-text"
    }
    val item = new VBox {
      spacing = 10
      alignment = Center
      children = List(img, name)
      onMouseClicked = _ => goToPuzzle(puzzle)
    }
    view.children.add(item)
  }
}
