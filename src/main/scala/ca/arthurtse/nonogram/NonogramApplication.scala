package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.model.PuzzleData.Puzzle
import ca.arthurtse.nonogram.model._
import ca.arthurtse.nonogram.view.{GridView, MenuView}
import com.squareup.moshi.Moshi
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

import scala.io._

object NonogramApplication extends JFXApp {
  private val stylesheetName: String = "/styles.css"
  JFXApp.userAgentStylesheet = getClass.getResource(stylesheetName).toExternalForm

  private val fileName: String = "/puzzle_data.json"
  private val source: BufferedSource = Source.fromInputStream(getClass.getResourceAsStream(fileName))
  private val json: String = try source.getLines.mkString finally source.close
  private val puzzleData: PuzzleData = new Moshi.Builder().build().adapter(classOf[PuzzleData]).fromJson(json)

  private val menuView = new MenuView(puzzleData, openPuzzle)
  private val menuScene = new Scene(menuView.view)

  private def openPuzzle(puzzle: Puzzle): Unit = {
    val gridModel = new GridModel(puzzle, puzzleData.legend)
    val gridView = new GridView(gridModel, returnToMenu)
    stage.scene = new Scene(gridView.view, 600, 600)
  }

  private def returnToMenu(): Unit = {
    stage.scene = menuScene
  }

  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 600
    height = 600
    scene = menuScene
  }
}
