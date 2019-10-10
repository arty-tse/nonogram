package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.model.GridModel
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color._

object NonogramApplication extends JFXApp {
  private val gridModel = new GridModel(10, 10, Array.empty, Array.empty)
  private val gridView = new GridView(gridModel)

  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 370
    height = 450
    scene = new Scene {
      fill = White
      content = gridView.view
    }
  }
}
