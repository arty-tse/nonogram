package ca.arthurtse.nonogram

import ca.arthurtse.nonogram.model._
import scalafx.beans.binding.Bindings
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos.Center
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.paint.{Color, Paint}
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle

class GridView(val model: GridModel) {
  private val grid: Array[Array[Tile]] = Array.ofDim[Tile](model.rows, model.cols)
  private val selected: ObjectProperty[TileState] = ObjectProperty(Filled)
  private var dragFill: Option[TileState] = None
  private var dragRow: Option[Int] = None
  private var dragCol: Option[Int] = None
  private var dragged: Set[(Int, Int)] = Set()
  private var dragDir: Option[Direction] = None
  private val tileSize = 30

  val gridView = new Pane
  for (i <- 0 until model.rows) {
    for (j <- 0 until model.cols) {
      val t = new Tile(i, j)
      //      grid(i)(j) = t
      gridView.children.add(t.view)
    }
  }

  val fillButton: Button = new Button {
    prefHeight = 60
    prefWidth = 60
    text = "Fill"
    onMouseClicked = _ => selected() = Filled
  }

  val emptyButton: Button = new Button {
    prefHeight = 60
    prefWidth = 60
    text = "Clear"
    onMouseClicked = _ => selected() = Empty
  }

  val view: VBox = new VBox {
    spacing = 30
    children = List(gridView, new HBox {
      spacing = 10
      children = List(fillButton, emptyButton)
    })
    alignment = Center
  }

  def colour(state: TileState): ObjectProperty[Paint] =
    state match {
      case Unknown => ObjectProperty[Paint](Red)
      case Filled => ObjectProperty[Paint](Black)
      case Empty => ObjectProperty[Paint](White)
    }

  class Tile(row: Int, col: Int, cell: TileState = Unknown) {
    val view: Rectangle = new Rectangle {
      fill <== colour(model.status(row, col))
      width = tileSize
      height = tileSize
      x = row * tileSize
      y = col * tileSize
      stroke = Black
      strokeWidth = 1
      onMouseClicked = _ => {
        printf("Clicked: (%d, %d)\n", row, col)
        selected() match {
          case Empty =>
            cell match {
              case Empty => model.update(row, col, Unknown)
              case Unknown => model.update(row, col, Empty)
            }
          case Filled =>
            cell match {
              case Filled => model.update(row, col, Unknown)
              case Unknown => model.update(row, col, Filled)
            }
        }
      }
      onDragDetected = _ => {
        printf("Drag Start: (%d, %d)\n", row, col)
        startFullDrag
        selected() match {
          case Empty =>
            cell match {
              case Empty =>
                dragged += ((row, col))
                dragFill = Some(Unknown)
                dragRow = Some(row)
                dragCol = Some(col)
              case Unknown =>
                dragged += ((row, col))
                dragFill = Some(Empty)
                dragRow = Some(row)
                dragCol = Some(col)
            }
          case Filled =>
            cell match {
              case Filled =>
                dragged += ((row, col))
                dragFill = Some(Unknown)
                dragRow = Some(row)
                dragCol = Some(col)
              case Unknown =>
                dragged += ((row, col))
                dragFill = Some(Filled)
                dragRow = Some(row)
                dragCol = Some(col)
            }
        }
      }
      onMouseDragOver = _ => {
        printf("Drag Over: (%d, %d)\n", row, col)
        dragDir match {
          case None =>
            if (row == dragRow.get) {
              if (col < dragCol.get) dragDir = Some(Up) else dragDir = Some(Down)
              if (cell == model.status(row, col)) dragged += ((row, col))
            } else if (dragCol.get == col) {
              if (row < dragRow.get) dragDir = Some(Left) else dragDir = Some(Right)
              if (cell == model.status(row, col)) dragged += ((row, col))
            }
          case _ =>
            if (row == dragRow.get) {
              if (col < dragCol.get) {
                if (dragDir.contains(Left) && cell == model.status(row, col)) {
                  dragged += ((row, col))
                } else {
                  dragged = Set()
                  for (i <- col until dragCol.get) {
                    if (model.status(dragRow.get, dragCol.get) == model.status(row, i)) dragged += ((row, col))
                  }
                  dragDir = Some(Left)
                }
              } else if (col > dragCol.get) {
                if (dragDir.contains(Right) && cell == model.status(row, col)) {
                  dragged += ((row, col))
                } else {
                  dragged = Set()
                  for (i <- col + 1 to dragCol.get) {
                    if (model.status(dragRow.get, dragCol.get) == model.status(row, i)) dragged += ((row, col))
                  }
                  dragDir = Some(Right)
                }
              }
            } else if (col == dragCol.get) {
              if (row < dragRow.get) {
                if (dragDir.contains(Up) && cell == model.status(row, col)) {
                  dragged += ((row, col))
                } else {
                  dragged = Set()
                  for (i <- row until dragRow.get) {
                    if (model.status(dragRow.get, dragCol.get) == model.status(row, i)) dragged += ((row, col))
                  }
                  dragDir = Some(Up)
                }
              } else if (row > dragRow.get) {
                if (dragDir.contains(Down) && cell == model.status(row, col)) {
                  dragged += ((row, col))
                } else {
                  dragged = Set()
                  for (i <- row + 1 to dragRow.get) {
                    if (model.status(dragRow.get, dragCol.get) == model.status(row, i)) dragged += ((row, col))
                  }
                  dragDir = Some(Down)
                }
              }
            }
        }
      }
      onMouseDragReleased = _ => {
        printf("Drag End: (%d, %d)\n", row, col)
        dragFill match {
          case Some(Unknown) => model.update(dragRow.get, dragCol.get, Unknown)
          case Some(Filled) => model.update(dragRow.get, dragCol.get, Unknown)
          case Some(Empty) => model.update(dragRow.get, dragCol.get, Empty)
        }
        for (t <- dragged) {
          dragFill match {
            case Some(Unknown) => model.update(t._1, t._2, Unknown)
            case Some(Filled) => model.update(t._1, t._2, Unknown)
            case Some(Empty) => model.update(t._1, t._2, Empty)
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
