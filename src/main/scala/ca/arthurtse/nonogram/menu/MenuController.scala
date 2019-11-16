package ca.arthurtse.nonogram.menu

import ca.arthurtse.nonogram.NonogramApplication
import ca.arthurtse.nonogram.puzzle.PuzzleModel
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.layout.{FlowPane, VBox}
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml
import scalafxml.core.{DependenciesByType, FXMLView}

import scala.reflect.runtime.universe.typeOf

@sfxml
class MenuController(private val menuPane: FlowPane, private val puzzleData: PuzzleData) {
  for (puzzle <- puzzleData.puzzles) {
    menuPane.children.add(new VBox {
      styleClass = ObservableBuffer("puzzleItem")
      children = Seq(new Rectangle {
        styleClass = ObservableBuffer("puzzlePic")
        width = 150
        height = 150
      }, new Text(prettifyName(puzzle.name)) {
        styleClass = ObservableBuffer("puzzleText")
      })
      onMouseClicked = _ => goToPuzzle(puzzle)
    })
  }

  def prettifyName(str: String): String = {
    str.split('-').map(_.capitalize).mkString(" ")
  }

  def goToPuzzle(puzzle: PuzzleData.Puzzle): Unit = {
    val root = FXMLView(getClass.getResource("/puzzle.fxml"),
      new DependenciesByType(Map(typeOf[PuzzleModel] -> new PuzzleModel(puzzle, puzzleData.legend))))
    NonogramApplication.changeScene(new Scene(root))
  }
}
