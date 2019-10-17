package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.model.GridModel
import ca.arthurtse.nonogram.view.GridView
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

object NonogramApplication extends JFXApp {
  private val gridModel = new GridModel(5, 5,
    Array(Array(5), Array(2, 2), Array(1, 1), Array(2, 2), Array(5)),
    Array(Array(5), Array(2, 2), Array(1, 1), Array(2, 2), Array(5)))
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
