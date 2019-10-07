package ca.arthurtse.nonogram

import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos.Center
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color._

import scala.collection.mutable.ArrayBuffer

class GridView(val model: GridModel) {
  private var grid: Array[Array[Tile]] = Array.ofDim[Tile](model.rows, model.cols)
  private var selected: ObjectProperty[model.Cell] = ObjectProperty(model.Box)
  private var dragX: Option[Int] = None
  private var dragY: Option[Int] = None
  private var dragFill: Option[Boolean] = None
  private var dragged: ArrayBuffer[(Int, Int)] = ArrayBuffer()
  private var dragDir: Option[Direction] = None
  private val tileSize = 30

  val gridView = new Pane
  for (i <- 0 until model.rows) {
    for (j <- 0 until model.cols) {
      val t = new Tile(i, j)
      grid(i)(j) = t
      gridView.children.add(t.view)
    }
  }

  val fillButton: Button = new Button {
    prefHeight = 60
    prefWidth = 60
    text = "Fill"
    onMouseClicked = _ => selected() = model.Box
  }

  val emptyButton: Button = new Button {
    prefHeight = 60
    prefWidth = 60
    text = "Clear"
    onMouseClicked = _ => selected() = model.Empty
  }

  val view: VBox = new VBox {
    spacing = 30
    children = List(gridView, new HBox {
      spacing = 10
      children = List(fillButton, emptyButton)
    })
    alignment = Center
  }

  class Tile(row: Int, col: Int, cell: model.Cell = model.Unknown) {
    val view: Rectangle = new Rectangle {
      fill = White
      width = tileSize
      height = tileSize
      x = row * tileSize
      y = col * tileSize
      stroke = Black
      strokeWidth = 1
      onMousePressed = _ => {
        printf("Pressed: (%d, %d)\n", row, col)
        selected() match {
          case model.Empty =>
            cell match {
              case model.Empty =>
                dragged += ((row, col))
                dragFill = Some(false)
              case model.Unknown =>
                dragged += ((row, col))
                dragFill = Some(true)
            }
          case model.Box =>
            cell match {
              case model.Box =>
                dragged += ((row, col))
                dragFill = Some(false)
              case model.Unknown =>
                dragged += ((row, col))
                dragFill = Some(true)
            }
        }
      }
      onMouseClicked = _ => {
        printf("Clicked: (%d, %d)\n", row, col)
        selected.value match {
          case model.Empty => fill = Grey
          case model.Box => fill = Black
          case model.Unknown => fill = White
        }
      }
      onDragDetected = _ => {
        printf("Drag Start: (%d, %d)\n", row, col)
        startFullDrag
      }
      onMouseDragOver = _ => {
        printf("Drag Over: (%d, %d)\n", row, col)
        dragDir match {
          case None =>
            if (dragX.get == row) {
              if (col < dragY.get) dragDir = Some(Up) else dragDir = Some(Down)
              dragged += ((row, col))
            } else if (dragY.get == col) {
              if (row < dragX.get) dragDir = Some(Left) else dragDir = Some(Right)
              dragged += ((row, col))
            }
          case Some(Up) =>
            if (row == dragX.get && col < dragY.get) dragged += ((row, col))
          case Some(Down) =>
            if (row == dragX.get && col > dragY.get) dragged += ((row, col))
          case Some(Left) =>
            if (col == dragY.get && row < dragX.get) dragged += ((row, col))
          case Some(Right) =>
            if (col == dragY.get && row < dragX.get) dragged += ((row, col))
        }
      }
      onMouseDragReleased = _ => {
        printf("Drag End: (%d, %d)\n", row, col)
      }
    }
  }

  sealed trait Direction

  case object Up extends Direction

  case object Down extends Direction

  case object Left extends Direction

  case object Right extends Direction

}
