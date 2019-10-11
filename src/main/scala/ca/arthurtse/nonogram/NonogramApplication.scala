package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.model.GridModel
import ca.arthurtse.nonogram.view.GridView
import javafx.scene.paint.Color
import javafx.scene.paint.Color._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox
import scalafx.scene.shape.Rectangle

object NonogramApplication extends JFXApp {
  private val gridModel = new GridModel(10, 10, Array.empty, Array.empty)
  private val gridView = new GridView(gridModel)

//  val list: Array[ObjectProperty[Color]] = Array.fill(2)(ObjectProperty(RED))
//
//  val obj = list(0)
//
//  val rec1 = new Rectangle {
//    width = 60
//    height = 60
//    fill <== list(0)
//  }
//
//  val rec2 = new Rectangle {
//    width = 60
//    height = 60
//    fill <== list(1)
//  }
//
//  val button = new Button {
//    prefWidth = 60
//    prefHeight = 60
//    onMouseClicked = _ => {
//      println("before: " + obj)
//      list(0)() = BLUE
//      println("first col: " + obj)
//    }
//  }

  stage = new PrimaryStage {
    title = "Nonogram Puzzler"
    width = 370
    height = 450
    scene = new Scene {
//      content = new HBox(rec1, rec2, button)
      content = gridView.view
    }
  }
}
