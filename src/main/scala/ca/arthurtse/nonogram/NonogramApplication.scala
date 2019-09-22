package ca.arthurtse.nonogram

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color._

object NonogramApplication extends JFXApp {
  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 600
    height = 450
    scene = new Scene {
      fill = LightGreen
      content = showGrid(10, 10)
    }
  }

  def showGrid(rows: Int, cols: Int): GridPane = {
    val grid = new GridPane
    for (i <- 0 to rows) {
      for (j <- 0 to cols) {
        grid.add(new Button {
          prefWidth = 30
          prefHeight = 30
        }, i, j)
      }
    }
    grid
  }
}
