package ca.arthurtse.nonogram.view

import ca.arthurtse.nonogram.model._
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Insets
import scalafx.geometry.Pos._
import scalafx.scene.control.{RadioButton, ToggleGroup}
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape.{Line, Rectangle, Shape}
import scalafx.scene.text.Text
import scalafx.scene.{Group, Node}

class GridView(val model: GridModel) {
  private val grid: Array[Array[Tile]] = Array.ofDim[Tile](model.rows, model.cols)
  private val selState: ObjectProperty[TileState] = ObjectProperty(Filled)
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

  private val rowHints = new VBox {
    style = "-fx-background-color: red"
    alignment = BottomRight
  }
  for (hint <- model.rowHints) {
    val rowBox = new HBox {
      prefHeight = tileSize
      spacing = 10
      alignment = CenterRight
      padding = Insets(0, 10, 0, 10)
    }
    for (clue <- hint) rowBox.children.add(new Text(clue.toString))
    rowHints.children.add(rowBox)
  }

  private val colHints = new HBox {
    style = "-fx-background-color: green"
    alignment = BottomCenter
  }
  for (hint <- model.colHints) {
    val colBox = new VBox {
      prefWidth = tileSize
      spacing = 10
      alignment = BottomCenter
      padding = Insets(10, 0, 10, 0)
    }
    for (clue <- hint) colBox.children.add(new Text(clue.toString))
    colHints.children.add(colBox)
  }

  private val btnGroup = new ToggleGroup {
    selectedToggle.onChange((_, _, newValue) => selState() = newValue.getUserData.asInstanceOf[TileState])
  }

  private val fillButton: RadioButton = new RadioButton {
    prefHeight = 40
    prefWidth = 55
    text = "Fill"
    userData = Filled
    toggleGroup = btnGroup
  }

  private val emptyButton: RadioButton = new RadioButton {
    prefHeight = 40
    prefWidth = 55
    text = "Clear"
    userData = Empty
    toggleGroup = btnGroup
  }

  val view: Pane = new VBox {
    spacing = 20
    alignment = CenterRight
    children = List(new HBox {
      alignment = BottomCenter
      children = List(rowHints, new VBox {
        alignment = Center
        children = List(colHints, gridView)
      })
    },
      new HBox {
        style = "-fx-background-color: yellow"
        spacing = 20
        children = List(fillButton, emptyButton)
        alignment = Center
      })
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

  private def checkSoln(): Unit = {

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
      selState() match {
        case Filled =>
          model.status(row, col)() match {
            case Filled =>
              updateNode(row, col, Unknown)
              checkSoln()
            case Empty =>
            case Unknown =>
              updateNode(row, col, Filled)
              checkSoln()
          }
        case Empty =>
          model.status(row, col)() match {
            case Filled =>
            case Empty =>
              updateNode(row, col, Unknown)
              checkSoln()
            case Unknown =>
              updateNode(row, col, Empty)
              checkSoln()
          }
        case Unknown =>
      }
    }
    onDragDetected = _ => {
      startFullDrag
      selState() match {
        case Filled =>
          model.status(row, col)() match {
            case Filled =>
              dragFill = Some(Unknown)
              dragRow = Some(row)
              dragCol = Some(col)
              dragged += ((row, col))
            case Empty =>
            case Unknown =>
              dragFill = Some(Filled)
              dragRow = Some(row)
              dragCol = Some(col)
              dragged += ((row, col))
          }
        case Empty =>
          model.status(row, col)() match {
            case Filled =>
            case Empty =>
              dragFill = Some(Unknown)
              dragRow = Some(row)
              dragCol = Some(col)
              dragged += ((row, col))
            case Unknown =>
              dragFill = Some(Empty)
              dragRow = Some(row)
              dragCol = Some(col)
              dragged += ((row, col))
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
                dragged = Set((dragRow.get, dragCol.get))
                for (i <- col until dragCol.get) {
                  if (model.status(dragRow.get, dragCol.get)() == model.status(row, i)()) dragged += ((row, col))
                }
                dragDir = Some(Left)
              }
            } else if (col > dragCol.get) {
              if (dragDir.contains(Right) && model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) {
                dragged += ((row, col))
              } else if (!dragDir.contains(Right)) {
                dragged = Set((dragRow.get, dragCol.get))
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
                dragged = Set((dragRow.get, dragCol.get))
                for (i <- row until dragRow.get) {
                  if (model.status(dragRow.get, dragCol.get)() == model.status(row, i)()) dragged += ((row, col))
                }
                dragDir = Some(Up)
              }
            } else if (row > dragRow.get) {
              if (dragDir.contains(Down) && model.status(row, col)() == model.status(dragRow.get, dragCol.get)()) {
                dragged += ((row, col))
              } else if (!dragDir.contains(Down)) {
                dragged = Set((dragRow.get, dragCol.get))
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
      checkSoln()
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
