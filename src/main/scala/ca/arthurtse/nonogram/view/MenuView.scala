package ca.arthurtse.nonogram.view

import ca.arthurtse.nonogram.model.PuzzleData
import ca.arthurtse.nonogram.model.PuzzleData.Puzzle
import scalafx.scene.layout.{FlowPane, Pane, VBox}
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
    val name = new Text(nameChanger(puzzle.name)) {
      styleClass += "puzzle-text"
    }
    val item = new VBox {
      styleClass += "puzzle-item"
      children = List(img, name)
      onMouseClicked = _ => goToPuzzle(puzzle)
    }
    view.children.add(item)
  }

  def nameChanger(str: String): String = {
    str.split('-').map(_.capitalize).mkString(" ")
  }
}
