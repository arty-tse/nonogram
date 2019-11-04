package ca.arthurtse.nonogram.puzzle

import ca.arthurtse.nonogram.NonogramApplication
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Group
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, RadioButton, ToggleGroup}
import scalafx.scene.layout.{HBox, Pane, Region, VBox}
import scalafx.scene.shape.{Line, Rectangle, Shape}
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml

@sfxml
class PuzzleController(private val tileGrid: Pane, private val colHints: HBox, private val rowHints: VBox,
                       private val btnGroup: ToggleGroup,
                       private val fillButton: RadioButton, private val emptyButton: RadioButton,
                       private val model: PuzzleModel) {
  private var selState: TileState = Filled
  btnGroup.selectedToggle.onChange((_, _, tog) =>
    tog.getUserData match {
      case "Filled" => selState = Filled
      case "Empty" => selState = Empty
      case _ => selState = Unknown
    })
  private val grid: Array[Array[Tile]] = Array.ofDim(model.rows, model.cols)
  private var dragFill: Option[TileState] = None
  private var dragStart: Option[Tile] = None
  private var dragged: Set[Tile] = Set()
  private var dragDir: Option[Direction] = None
  private val tileSize: Int = 30
  for (i <- 0 until model.rows) {
    for (j <- 0 until model.cols) {
      val tile: Tile = new Tile(i, j)
      grid(i)(j) = tile
      tileGrid.children.add(tile)
    }
  }

  for (hint <- model.rowHints) {
    val rowBox = new HBox {
      styleClass += "rowHint"
    }
    for (clue <- hint) rowBox.children.add(new Text(clue.toString) {
      styleClass += "hintText"
    })
    rowHints.children.add(rowBox)
  }
  for (hint <- model.colHints) {
    val colBox = new VBox {
      styleClass += "colHint"
    }
    for (clue <- hint) colBox.children.add(new Text(clue.toString) {
      styleClass += "hintText"
    })
    colHints.children.add(colBox)
  }

  class Tile(val row: Int, val col: Int) extends Group {
    val tX: Int = col * tileSize
    val tY: Int = row * tileSize
    var state: ObjectProperty[TileState] = ObjectProperty(Unknown)
    state <== model.status(row, col)
    state.onChange((_, _, newValue) =>
      newValue match {
        case Filled =>
          children = new TileView(tX, tY) {
            styleClass = ObservableBuffer("filled", "tile")
          }
        case Empty =>
          children = List(new TileView(tX, tY) {
            styleClass = ObservableBuffer("empty", "tile")
          }, Shape.union(new Line {
            styleClass = ObservableBuffer("emptyCross")
            startX = tX
            startY = tY
            endX = tX + tileSize
            endY = tY + tileSize
          }, new Line {
            styleClass = ObservableBuffer("emptyCross")
            startX = tX
            startY = tY + tileSize
            endX = tX + tileSize
            endY = tY
          }))
        case Unknown =>
          children = new TileView(tX, tY) {
            styleClass = ObservableBuffer("unknown", "tile")
          }
      })
    children = new TileView(tX, tY) {
      styleClass = ObservableBuffer("unknown", "tile")
    }

    onMouseClicked = _ => {
      selState match {
        case Filled =>
          state() match {
            case Filled =>
              updateNode(Unknown)
              checkSoln()
            case Empty =>
            case Unknown =>
              updateNode(Filled)
              checkSoln()
          }
        case Empty =>
          state() match {
            case Filled =>
            case Empty =>
              updateNode(Unknown)
              checkSoln()
            case Unknown =>
              updateNode(Empty)
              checkSoln()
          }
        case Unknown =>
      }
    }
    onDragDetected = _ => {
      startFullDrag
      selState match {
        case Filled =>
          state() match {
            case Filled =>
              dragFill = Some(Unknown)
              dragStart = Some(this)
              dragged += this
            case Empty =>
            case Unknown =>
              dragFill = Some(Filled)
              dragStart = Some(this)
              dragged += this
          }
        case Empty =>
          model.status(row, col)() match {
            case Filled =>
            case Empty =>
              dragFill = Some(Unknown)
              dragStart = Some(this)
              dragged += this
            case Unknown =>
              dragFill = Some(Empty)
              dragStart = Some(this)
              dragged += this
          }
        case Unknown =>
      }
    }
    onMouseDragOver = _ => {
      dragDir match {
        case None =>
          if (row == dragStart.get.row) {
            if (col < dragStart.get.col) dragDir = Some(Left) else dragDir = Some(Right)
          } else if (dragStart.get.col == col) {
            if (row < dragStart.get.row) dragDir = Some(Up) else dragDir = Some(Down)
          }
        case _ =>
          if (row == dragStart.get.row) {
            if (col < dragStart.get.col) {
              if (dragDir.contains(Left) && state() == dragStart.get.state()) {
                dragged += this
              } else if (!dragDir.contains(Left)) {
                dragDir = Some(Left)
                for (i <- col to dragStart.get.col) {
                  if (dragStart.get.state() == grid(row)(i).state()) dragged += grid(row)(i)
                }
              }
            } else if (col > dragStart.get.col) {
              if (dragDir.contains(Right) && state() == dragStart.get.state()) {
                dragged += this
              } else if (!dragDir.contains(Right)) {
                dragDir = Some(Right)
                for (i <- col + 1 to dragStart.get.col) {
                  if (dragStart.get.state() == grid(row)(i).state()) dragged += grid(row)(i)
                }
              }
            }
          } else if (col == dragStart.get.col) {
            if (row < dragStart.get.row) {
              if (dragDir.contains(Up) && state() == dragStart.get.state()) {
                dragged += this
              } else if (!dragDir.contains(Up)) {
                dragDir = Some(Up)
                for (i <- row to dragStart.get.row) {
                  if (dragStart.get.state() == grid(i)(col).state()) dragged += grid(i)(col)
                }
              }
            } else if (row > dragStart.get.row) {
              if (dragDir.contains(Down) && state() == dragStart.get.state()) {
                dragged += this
              } else if (!dragDir.contains(Down)) {
                dragDir = Some(Down)
                for (i <- row + 1 to dragStart.get.row) {
                  if (dragStart.get.state() == grid(i)(col).state()) dragged += grid(i)(col)
                }
              }
            }
          }
      }
    }
    onMouseDragReleased = _ => {
      for (t <- dragged) {
        dragFill.foreach(f => t.updateNode(f))
      }
      dragFill = None
      dragStart = None
      dragged = Set()
      dragDir = None
      checkSoln()
    }

    private def updateNode(newState: TileState): Unit = {
      if (state() != newState) model.update(row, col, newState)
    }

    private def checkSoln(): Unit = {
      if (model.checkSolution()) {
        val alert = new Alert(AlertType.Information) {
          title = "Puzzle Completed!"
          headerText = None
          contentText = "Congratulations! You finished this puzzle!"
        }
        val res: Option[ButtonType] = alert.showAndWait
        if (res.contains(ButtonType.OK)) {
          NonogramApplication.goToMenu()
        }
      }
    }

    class TileView(val tvX: Int, val tvY: Int) extends Rectangle {
      x = tvX
      y = tvY
      width = tileSize
      height = tileSize
    }

  }

  sealed trait Direction

  case object Up extends Direction

  case object Down extends Direction

  case object Left extends Direction

  case object Right extends Direction

}
