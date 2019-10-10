package ca.arthurtse.nonogram.model

sealed trait TileState
case object Unknown extends TileState
case object Filled extends TileState
case object Empty extends TileState
