package ca.arthurtse.nonogram

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color._

object NonogramApplication extends JFXApp {
  private val gridModel = new GridModel(10, 10, Array.empty, Array.empty)
  private val gridView = new GridView(gridModel)

  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 400
    height = 400
    scene = new Scene {
      fill = LightGreen
      content = gridView.grid
    }
  }
}
