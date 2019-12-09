package ca.arthurtse.nonogram

import java.io.{File, FileInputStream, FileOutputStream}

import ca.arthurtse.nonogram.menu.PuzzleData
import ca.arthurtse.nonogram.puzzle.SaveFile
import ca.arthurtse.nonogram.puzzle.SaveFile.PuzzleSave
import com.squareup.moshi.{JsonAdapter, Moshi}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{DependenciesByType, FXMLView}

import scala.io._
import scala.reflect.runtime.universe.typeOf

object NonogramApplication extends JFXApp {
  private val stylesheetName: String = "/styles.css"
  JFXApp.userAgentStylesheet = getClass.getResource(stylesheetName).toExternalForm

  private val fileName: String = "/puzzle_data.json"
  private val source: BufferedSource = Source.fromInputStream(getClass.getResourceAsStream(fileName))
  private val json: String = try source.getLines.mkString finally source.close

  private val moshi: Moshi = new Moshi.Builder().build()
  private val adapter: JsonAdapter[PuzzleData] = moshi.adapter(classOf[PuzzleData])

  private val saveName: String = "data.sav"
  private val saveFile = new File(saveName)
  private var save: SaveFile = SaveFile()
  if (saveFile.isFile && saveFile.canRead) {
    println("reading save file")
    save = SaveFile.parseFrom(new FileInputStream(saveFile))
  }

  private val menu = FXMLView(getClass.getResource("/menu.fxml"),
    new DependenciesByType(Map(typeOf[PuzzleData] -> adapter.fromJson(json),
      typeOf[SaveFile] -> save)))
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

  def updateSave(name: String, pSave: PuzzleSave): Unit = {
    save = save.update(_.saves(name) := pSave)
  }

  override def stopApp(): Unit = {
    println("does this work")
    save.writeTo(new FileOutputStream(saveName))
  }
}
