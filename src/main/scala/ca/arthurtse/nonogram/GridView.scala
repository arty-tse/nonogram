package ca.arthurtse.nonogram

import scalafx.scene.control.Button
import scalafx.scene.layout.GridPane

class GridView(val model: GridModel) {
  val grid = new GridPane
  for (i <- 0 to model.rows) {
    for (j <- 0 to model.cols) {
      grid.add(new Button {
        prefWidth = 30
        prefHeight = 30
      }, i, j)
    }
  }
}
