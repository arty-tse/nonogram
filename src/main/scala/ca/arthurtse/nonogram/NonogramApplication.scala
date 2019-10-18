package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.model.PuzzleData.Puzzle
import ca.arthurtse.nonogram.model._
import ca.arthurtse.nonogram.view.{GridView, MenuView}
import com.squareup.moshi.Moshi
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

import scala.io._

object NonogramApplication extends JFXApp {
  private val fileName: String = "/puzzle_data.json"
  private val source: BufferedSource = Source.fromInputStream(getClass.getResourceAsStream(fileName))
  private val json: String = try source.getLines.mkString finally source.close
  private val puzzleData: PuzzleData = new Moshi.Builder().build().adapter(classOf[PuzzleData]).fromJson(json)

  private val menuView = new MenuView(puzzleData, openPuzzle)
  private val menuScene = new Scene {
    content = menuView.view
  }

  private def openPuzzle(puzzle: Puzzle): Unit = {
    val gridModel = new GridModel(puzzle, puzzleData.legend)
    val gridView = new GridView(gridModel, returnToMenu)
    stage.scene = new Scene {
      content = gridView.view
    }
  }

  private def returnToMenu(): Unit = {
    stage.scene = menuScene
  }

  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 400
    height = 400
    scene = menuScene
  }
}
