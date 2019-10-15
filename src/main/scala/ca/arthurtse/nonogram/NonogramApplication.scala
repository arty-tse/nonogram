package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.model.GridModel
import ca.arthurtse.nonogram.view.GridView
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object NonogramApplication extends JFXApp {
  private val gridModel = new GridModel(10, 10, Array.empty, Array.empty)
  private val gridView = new GridView(gridModel)

  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 370
    height = 450
    scene = new Scene {
      content = gridView.view
    }
  }
}
