package ca.arthurtse.nonogram.menu

import ca.arthurtse.nonogram.NonogramApplication
import ca.arthurtse.nonogram.menu.PuzzleData.Legend
import ca.arthurtse.nonogram.puzzle.{PuzzleModel, SaveFile}
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
class MenuController(private val menuPane: FlowPane, private val puzzleData: PuzzleData, private val save: SaveFile) {
  private val legend: Legend = puzzleData.legend
  for (puzzle <- puzzleData.puzzles) {
    menuPane.children.add(new VBox {
      styleClass = ObservableBuffer("puzzleItem")
      children = Seq(new Rectangle {
        styleClass = ObservableBuffer("puzzlePic")
        width = 150
        height = 150
      }, new Text(puzzle.name.split('-').map(_.capitalize).mkString(" ")) {
        styleClass = ObservableBuffer("puzzleText")
      })
      onMouseClicked = _ => goToPuzzle(puzzle, save.saves.get(puzzle.name))
    })
  }

  def goToPuzzle(puzzle: PuzzleData.Puzzle, save: Option[SaveFile.PuzzleSave]): Unit = {
    val root = FXMLView(getClass.getResource("/puzzle.fxml"),
      new DependenciesByType(Map(typeOf[PuzzleData.Puzzle] -> puzzle,
        typeOf[PuzzleData.Legend] -> legend,
        typeOf[Option[SaveFile.PuzzleSave]] -> save)))
    NonogramApplication.changeScene(new Scene(root))
  }
}
