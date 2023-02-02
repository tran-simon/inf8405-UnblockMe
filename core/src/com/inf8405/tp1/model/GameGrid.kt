package com.inf8405.tp1.model

import com.badlogic.gdx.Gdx
import com.inf8405.tp1.model.utils.Grid
import com.inf8405.tp1.model.utils.Vector2D

private const val DEFAULT_GRID_WIDTH = 6
private const val DEFAULT_GRID_HEIGHT = 6

class GameGrid(val width: Int = DEFAULT_GRID_WIDTH, val height: Int = DEFAULT_GRID_HEIGHT) {
    val pieces = arrayListOf<GamePiece>()
    private val grid = Grid<GamePiece>(width, height)

    private fun getPointsForPiece(piece: GamePiece): Pair<ArrayList<Vector2D>, Boolean> {
        val points = arrayListOf<Vector2D>()
        var isFree = true
        var (x, y) = piece.position

        for (i in 0 until piece.size) {
            if (grid.getAt(Vector2D(x, y)) != null) {
                isFree = false
            }

            points.add(Vector2D(x, y))

            if (piece.orientation == Orientation.HORIZONTAL) {
                x++
            } else {
                y++
            }
        }

        return Pair(points, isFree)
    }

    fun addPiece(piece: GamePiece) {
        val (points, free) = getPointsForPiece(piece)
        if (!free) {
            throw Exception("Failed to add a piece to the grid: position not empty")
        }

        for (point in points) {
            grid.setAt(point, piece)
        }

        pieces.add(piece)
    }

    fun removePiece(piece: GamePiece) {
        val didRemove = pieces.remove(piece)

        if (!didRemove) {
            throw Exception("Failed to remove a piece: The grid does not contain the piece")
        }

        val (points) = getPointsForPiece(piece)

        for (point in points) {
            grid.setAt(point, null)
        }
    }

    fun print() {
        Gdx.app.debug("UnblockMe", grid.toString())
    }
}
