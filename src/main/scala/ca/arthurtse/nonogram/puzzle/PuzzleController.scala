package ca.arthurtse.nonogram.puzzle

import ca.arthurtse.nonogram.NonogramApplication
import scalafx.Includes._
import scalafx.animation.Timeline
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, ToggleGroup}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Line, Shape, StrokeLineCap}
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml

@sfxml
class PuzzleController(private val tileGrid: GridPane, private val colHints: HBox, private val rowHints: VBox,
                       private val buttonGroup: ToggleGroup, private val model: PuzzleModel) {
  private val tileInterval: Int = 5
  private val tileSize: Int = 30
  private var selState: TileState = Filled
  private val grid: Array[Array[Tile]] = Array.ofDim(model.rows, model.cols)
  private var dragFill: Option[TileState] = None
  private var dragStart: Option[Tile] = None
  private var dragged: Set[Tile] = Set()
  private var dragDir: Option[Direction] = None

  buttonGroup.selectedToggle.onChange((_, _, tog) =>
    tog.getUserData match {
      case "Filled" => selState = Filled
      case "Empty" => selState = Empty
      case _ => selState = Unknown
    })

  for (i <- 0 until model.rows) {
    for (j <- 0 until model.cols) {
      val tile: Tile = new Tile(i, j)
      grid(i)(j) = tile
      tileGrid.add(tile, j, i)
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

  class Tile(val row: Int, val col: Int) extends StackPane {
    val x: Int = col * tileSize
    val y: Int = row * tileSize
    val tile: TileView = new TileView(row, col)
    var cross: Shape = Shape.union(new CrossLine(x + 10, y + 10, x + tileSize - 10, y + tileSize - 10),
      new CrossLine(x + 10, y + tileSize - 10, x + tileSize - 10, y + 10))
    var state: ObjectProperty[TileState] = ObjectProperty(Unknown)
    state <== model.status(row, col)
    state.onChange((_, _, newVal) => {
      tile.changeState(newVal)
      if (newVal != Empty) children -= cross else children += cross
    })
    children = tile

    onMouseClicked = _ => {
      if (selState == state() && selState != Unknown) {
        updateNode(Unknown)
      } else if (selState == Filled && state() == Unknown) {
        updateNode(Filled)
      } else if (selState == Empty && state() == Unknown) {
        updateNode(Empty)
      }
      checkSoln()
    }
    onDragDetected = _ => {
      startFullDrag
      if (selState == state() && selState != Unknown) {
        dragFill = Some(Unknown)
        dragStart = Some(this)
        dragged += this
      } else if (selState == Filled && state() == Unknown) {
        dragFill = Some(Filled)
        dragStart = Some(this)
        dragged += this
      } else if (selState == Empty && state() == Unknown) {
        dragFill = Some(Empty)
        dragStart = Some(this)
        dragged += this
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
                for (i <- col to dragStart.get.col)
                  if (dragStart.get.state() == grid(row)(i).state()) dragged += grid(row)(i)
              }
            } else if (col > dragStart.get.col) {
              if (dragDir.contains(Right) && state() == dragStart.get.state()) {
                dragged += this
              } else if (!dragDir.contains(Right)) {
                dragDir = Some(Right)
                for (i <- col + 1 to dragStart.get.col)
                  if (dragStart.get.state() == grid(row)(i).state()) dragged += grid(row)(i)
              }
            }
          } else if (col == dragStart.get.col) {
            if (row < dragStart.get.row) {
              if (dragDir.contains(Up) && state() == dragStart.get.state()) {
                dragged += this
              } else if (!dragDir.contains(Up)) {
                dragDir = Some(Up)
                for (i <- row to dragStart.get.row)
                  if (dragStart.get.state() == grid(i)(col).state()) dragged += grid(i)(col)
              }
            } else if (row > dragStart.get.row) {
              if (dragDir.contains(Down) && state() == dragStart.get.state()) {
                dragged += this
              } else if (!dragDir.contains(Down)) {
                dragDir = Some(Down)
                for (i <- row + 1 to dragStart.get.row)
                  if (dragStart.get.state() == grid(i)(col).state()) dragged += grid(i)(col)
              }
            }
          }
      }
    }
    onMouseDragReleased = _ => {
      for (t <- dragged) dragFill.foreach(f => t.updateNode(f))
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

    class TileView(private val row: Int, private val col: Int, private var state: TileState = Unknown) extends Region {
      styleClass = ObservableBuffer("tile")
      prefWidth = tileSize
      prefHeight = tileSize
      addStateToStyle()

      private var top: Boolean = false
      private var bot: Boolean = false
      private var left: Boolean = false
      private var right: Boolean = false

      if (row % tileInterval == 0) top = true
      if ((row + 1) % tileInterval == 0 || row == model.rows - 1) bot = true
      if (col % tileInterval == 0) left = true
      if ((col + 1) % tileInterval == 0 || col == model.cols - 1) right = true

      private val borderType: String = if (top) if (bot) if (left) if (right) "allBorder" else "topBotLeft"
      else if (right) "topBotRight" else "topBot"
      else if (left) if (right) "topLeftRight" else "topLeft"
      else if (right) "topRight" else "top"
      else if (bot) if (left) if (right) "botLeftRight" else "botLeft"
      else if (right) "botRight" else "bot"
      else if (left) if (right) "leftRight" else "left"
      else if (right) "right" else "none"
      addBorderToStyle()

      private def addStateToStyle(): Unit = {
        state match {
          case Filled => styleClass += "filled"
          case Empty => styleClass += "empty"
          case Unknown => styleClass += "unknown"
        }
      }

      private def addBorderToStyle(): Unit = {
        if (borderType != "none") styleClass += borderType
      }

      def changeState(newState: TileState): Unit = {
        state = newState
        styleClass = ObservableBuffer("tile")
        addStateToStyle()
        addBorderToStyle()
      }
    }

    class CrossLine(sX: Double, sY: Double, eX: Double, eY: Double) extends Line {
      startX = sX
      startY = sY
      endX = eX
      endY = eY
      stroke = Color.web("#141414")
      strokeLineCap = StrokeLineCap.Round
      strokeWidth = 5
    }

  }

  sealed trait Direction

  case object Up extends Direction

  case object Down extends Direction

  case object Left extends Direction

  case object Right extends Direction

}
