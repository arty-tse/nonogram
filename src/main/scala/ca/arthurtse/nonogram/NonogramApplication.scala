package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.menu.PuzzleData
import com.squareup.moshi.Moshi
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{DependenciesByType, FXMLView}

import scala.reflect.runtime.universe.typeOf
import scala.io._

object NonogramApplication extends JFXApp {
  private val stylesheetName: String = "/styles.css"
  JFXApp.userAgentStylesheet = getClass.getResource(stylesheetName).toExternalForm

  private val fileName: String = "/puzzle_data.json"
  private val source: BufferedSource = Source.fromInputStream(getClass.getResourceAsStream(fileName))
  private val json: String = try source.getLines.mkString finally source.close

  private val menu = FXMLView(getClass.getResource("/menu.fxml"),
    new DependenciesByType(Map(typeOf[PuzzleData] ->
      new Moshi.Builder().build().adapter(classOf[PuzzleData]).fromJson(json))))
  private val menuScene = new Scene(menu)

  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 600
    height = 600
    scene = menuScene
  }

  def changeScene(scene: Scene): Unit = {
    stage.scene = scene
  }

  def goToMenu(): Unit = {
    stage.scene = menuScene
  }
}
