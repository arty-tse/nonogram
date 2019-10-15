package ca.arthurtse.nonogram.view

import ca.arthurtse.nonogram.model._
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos.Center
import scalafx.scene.control.Button
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape.{Line, Rectangle, Shape}
import scalafx.scene.{Group, Node}

class GridView(val model: GridModel) {
  private val grid: Array[Array[Tile]] = Array.ofDim[Tile](model.rows, model.cols)
  private val selected: ObjectProperty[TileState] = ObjectProperty(Filled)
  private var dragFill: Option[TileState] = None
  private var dragRow: Option[Int] = None
  private var dragCol: Option[Int] = None
  private var dragged: Set[(Int, Int)] = Set()
  private var dragDir: Option[Direction] = None
  private val tileSize = 30

  private val gridView = new Pane
  for (i <- 0 until model.rows) {
    for (j <- 0 until model.cols) {
      grid(i)(j) = new UnknownTile(i, j)
      gridView.children.add(grid(i)(j))
    }
  }

  private val fillButton: Button = new Button {
    prefHeight = 60
    prefWidth = 60
    text = "Fill"
    onMouseClicked = _ => selected() = Filled
  }

  private val emptyButton: Button = new Button {
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

  private def updateNode(row: Int, col: Int, state: TileState): Unit = {
    model.update(row, col, state)
    val tile: Tile = grid(row)(col)
    val rep: Tile = state match {
        case Filled => new FilledTile(row, col)
        case Empty => new EmptyTile(row, col)
        case Unknown => new UnknownTile(row, col)
      }
    grid(row)(col) = rep
    gridView.children.set(tile.index, rep)
  }

  trait Tile extends Node {
    val row: Int
    val col: Int
    val index: Int = row * model.cols + col
    val tWidth: Int = tileSize
    val tHeight: Int = tileSize
    val tX: Int = row * tileSize
    val tY: Int = col * tileSize
    val tStroke: Color = Black
    val tStrokeWidth: Int = 1

    onMouseClicked = _ => {
      selected() match {
        case Filled =>
          model.status(row, col)() match {
            case Filled => updateNode(row, col, Unknown)
            case Empty =>
            case Unknown => updateNode(row, col, Filled)
          }
        case Empty =>
          model.status(row, col)() match {
            case Filled =>
            case Empty => updateNode(row, col, Unknown)
            case Unknown => updateNode(row, col, Empty)
          }
        case Unknown =>
      }
    }
    onDragDetected = _ => {
      startFullDrag
      selected() match {
        case Filled =>
          model.status(row, col)() match {
            case Filled =>
              dragFill = Some(Unknown)
              dragRow = Some(row)
              dragCol = Some(col)
            case Empty =>
            case Unknown =>
              dragFill = Some(Filled)
              dragRow = Some(row)
              dragCol = Some(col)
          }
        case Empty =>
          model.status(row, col)() match {
            case Filled =>
            case Empty =>
              dragFill = Some(Unknown)
              dragRow = Some(row)
              dragCol = Some(col)
            case Unknown =>
              dragFill = Some(Empty)
              dragRow = Some(row)
              dragCol = Some(col)
          }
        case Unknown =>
      }
    }
    onMouseDragOver = _ => {
      dragDir match {
        case None =>
          if (row == dragRow.get) {
            if (col < dragCol.get) dragDir = Some(Left) else dragDir = Some(Right)
            if (model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) dragged += ((row, col))
          } else if (dragCol.get == col) {
            if (row < dragRow.get) dragDir = Some(Up) else dragDir = Some(Down)
            if (model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) dragged += ((row, col))
          }
        case _ =>
          if (row == dragRow.get) {
            if (col < dragCol.get) {
              if (dragDir.contains(Left) && model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) {
                dragged += ((row, col))
              } else if (!dragDir.contains(Left)) {
                dragged = Set()
                for (i <- col until dragCol.get) {
                  if (model.status(dragRow.get, dragCol.get)() == model.status(row, i)()) dragged += ((row, col))
                }
                dragDir = Some(Left)
              }
            } else if (col > dragCol.get) {
              if (dragDir.contains(Right) && model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) {
                dragged += ((row, col))
              } else if (!dragDir.contains(Right)) {
                dragged = Set()
                for (i <- col + 1 to dragCol.get) {
                  if (model.status(dragRow.get, dragCol.get)() == model.status(row, i)()) dragged += ((row, col))
                }
                dragDir = Some(Right)
              }
            }
          } else if (col == dragCol.get) {
            if (row < dragRow.get) {
              if (dragDir.contains(Up) && model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) {
                dragged += ((row, col))
              } else if (!dragDir.contains(Up)) {
                dragged = Set()
                for (i <- row until dragRow.get) {
                  if (model.status(dragRow.get, dragCol.get)() == model.status(row, i)()) dragged += ((row, col))
                }
                dragDir = Some(Up)
              }
            } else if (row > dragRow.get) {
              if (dragDir.contains(Down) && model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) {
                dragged += ((row, col))
              } else if (!dragDir.contains(Down)) {
                dragged = Set()
                for (i <- row + 1 to dragRow.get) {
                  if (model.status(dragRow.get, dragCol.get)() == model.status(row, i)()) dragged += ((row, col))
                }
                dragDir = Some(Down)
              }
            }
          }
      }
    }
    onMouseDragReleased = _ => {
      dragFill match {
        case None =>
        case Some(Filled) => updateNode(dragRow.get, dragCol.get, Filled)
        case Some(Empty) => updateNode(dragRow.get, dragCol.get, Empty)
        case Some(Unknown) => updateNode(dragRow.get, dragCol.get, Unknown)
      }
      for (t <- dragged) {
        dragFill match {
          case None =>
          case Some(Filled) => updateNode(t._1, t._2, Filled)
          case Some(Empty) => updateNode(t._1, t._2, Empty)
          case Some(Unknown) => updateNode(t._1, t._2, Unknown)
        }
      }
      dragFill = None
      dragRow = None
      dragCol = None
      dragged = Set()
      dragDir = None
    }
  }

  class UnknownTile(override val row: Int, override val col: Int) extends Rectangle with Tile {
    x = tX
    y = tY
    width = tWidth
    height = tHeight
    stroke = tStroke
    strokeWidth = tStrokeWidth
    fill = White
  }

  class FilledTile(override val row: Int, override val col: Int) extends Rectangle with Tile {
    x = tX
    y = tY
    width = tWidth
    height = tHeight
    stroke = tStroke
    strokeWidth = tStrokeWidth
    fill = Black
  }

  class EmptyTile(override val row: Int, override val col: Int) extends Group with Tile {
    private val box: Rectangle = new Rectangle {
      x = tX
      y = tY
      width = tWidth
      height = tHeight
      stroke = tStroke
      strokeWidth = tStrokeWidth
      fill = White
    }
    private val cross: Shape = Shape.union(Line(tX, tY, tX + tWidth, tY + tHeight), Line(tX, tY + tHeight, tX + tWidth, tY))
    children = List(box, cross)
  }

  sealed trait Direction

  case object Up extends Direction

  case object Down extends Direction

  case object Left extends Direction

  case object Right extends Direction

}
