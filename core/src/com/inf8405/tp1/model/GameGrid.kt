package com.inf8405.tp1.model

import com.inf8405.tp1.model.utils.Grid
import com.inf8405.tp1.model.utils.Point

private const val DEFAULT_GRID_WIDTH = 6
private const val DEFAULT_GRID_HEIGHT = 6

class GameGrid(width: Int = DEFAULT_GRID_WIDTH, height: Int = DEFAULT_GRID_HEIGHT) {
    private val pieces = arrayListOf<GamePiece>()
    private val grid = Grid<GamePiece>(width, height)

    private fun getPointsForPiece(piece: GamePiece): Pair<ArrayList<Point>, Boolean> {
        val points = arrayListOf<Point>()
        var isFree = true
        var (x, y) = piece.position

        for (i in 0 until piece.size) {
            if (grid.getAt(Point(x, y)) != null) {
                isFree = false
            }

            points.add(Point(x, y))

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
        println(grid)
    }
}
