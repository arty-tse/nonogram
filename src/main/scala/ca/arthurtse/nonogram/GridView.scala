package ca.arthurtse.nonogram

import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos.Center
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color._

import scala.collection.mutable.ArrayBuffer

class GridView(val model: GridModel) {
  private val grid: Array[Array[Tile]] = Array.ofDim[Tile](model.rows, model.cols)
  private val selected: ObjectProperty[model.Cell] = ObjectProperty(model.Box)
  private var dragFill: Option[model.Cell] = None
  private var dragRow: Option[Int] = None
  private var dragCol: Option[Int] = None
  private var dragged: Set[(Int, Int)] = Set()
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
      onMouseClicked = _ => {
        printf("Clicked: (%d, %d)\n", row, col)
        selected() match {
          case model.Empty =>
            cell match {
              case model.Empty => fill = White
              case model.Unknown => fill = Red
            }
          case model.Box =>
            cell match {
              case model.Box => fill = White
              case model.Unknown => fill = Black
            }
        }
      }
      onDragDetected = _ => {
        printf("Drag Start: (%d, %d)\n", row, col)
        startFullDrag
        selected() match {
          case model.Empty =>
            cell match {
              case model.Empty =>
                dragged += ((row, col))
                dragFill = Some(model.Unknown)
                dragRow = Some(row)
                dragCol = Some(col)
              case model.Unknown =>
                dragged += ((row, col))
                dragFill = Some(model.Empty)
                dragRow = Some(row)
                dragCol = Some(col)
            }
          case model.Box =>
            cell match {
              case model.Box =>
                dragged += ((row, col))
                dragFill = Some(model.Unknown)
                dragRow = Some(row)
                dragCol = Some(col)
              case model.Unknown =>
                dragged += ((row, col))
                dragFill = Some(model.Box)
                dragRow = Some(row)
                dragCol = Some(col)
            }
        }
      }
      onMouseDragOver = _ => {
        printf("Drag Over: (%d, %d)\n", row, col)
        dragDir match {
          case None =>
            if (dragRow.get == row) {
              if (col < dragCol.get) dragDir = Some(Up) else dragDir = Some(Down)
              if ((cell == model.Unknown && !dragFill.contains(model.Unknown)) ||
                (cell != model.Unknown && dragFill.contains(model.Unknown)))
                dragged += ((row, col))
            } else if (dragCol.get == col) {
              if (row < dragRow.get) dragDir = Some(Left) else dragDir = Some(Right)
              if ((cell == model.Unknown && !dragFill.contains(model.Unknown)) ||
                (cell != model.Unknown && dragFill.contains(model.Unknown)))
                dragged += ((row, col))
            }
          case Some(Up) =>
            if (row == dragRow.get && col < dragCol.get &&
              ((cell == model.Unknown && !dragFill.contains(model.Unknown)) ||
              (cell != model.Unknown && dragFill.contains(model.Unknown))))
              dragged += ((row, col))
          case Some(Down) =>
            if (row == dragRow.get && col > dragCol.get &&
              ((cell == model.Unknown && !dragFill.contains(model.Unknown)) ||
                (cell != model.Unknown && dragFill.contains(model.Unknown))))
              dragged += ((row, col))
          case Some(Left) =>
            if (col == dragCol.get && row < dragRow.get &&
              ((cell == model.Unknown && !dragFill.contains(model.Unknown)) ||
                (cell != model.Unknown && dragFill.contains(model.Unknown))))
              dragged += ((row, col))
          case Some(Right) =>
            if (col == dragCol.get && row < dragRow.get &&
              ((cell == model.Unknown && !dragFill.contains(model.Unknown)) ||
                (cell != model.Unknown && dragFill.contains(model.Unknown))))
              dragged += ((row, col))
        }
      }
      onMouseDragReleased = _ => {
        printf("Drag End: (%d, %d)\n", row, col)
        for (t <- dragged) {
          dragFill match {
            case Some(model.Box) => grid(t._1)(t._2).view.fill = Black
            case Some(model.Empty) => grid(t._1)(t._2).view.fill = White
            case Some(model.Unknown) => grid(t._1)(t._2).view.fill = Red
          }
        }
        dragFill = None
        dragRow = None
        dragCol = None
        dragged = Set()
        dragDir = None
      }
    }
  }

  sealed trait Direction

  case object Up extends Direction

  case object Down extends Direction

  case object Left extends Direction

  case object Right extends Direction

}
