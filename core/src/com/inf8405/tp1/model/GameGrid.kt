package com.inf8405.tp1.model

import com.badlogic.gdx.Gdx
import com.inf8405.tp1.model.utils.Grid
import com.inf8405.tp1.model.utils.GridBoundsException
import com.inf8405.tp1.model.utils.Vector

private const val DEFAULT_GRID_WIDTH = 6
private const val DEFAULT_GRID_HEIGHT = 6

class GameGrid(val width: Int = DEFAULT_GRID_WIDTH, val height: Int = DEFAULT_GRID_HEIGHT): Grid<GamePiece>(width, height) {
    private val pieces = arrayListOf<GamePiece>()

    fun getPointsForPiece(piece: GamePiece): Pair<ArrayList<Vector>, Boolean> {
        val points = arrayListOf<Vector>()
        var isFree = true
        var (x, y) = piece.position

        for (i in 0 until piece.size) {
            try {
                if (getAt(Vector(x, y)) != null) {
                    isFree = false
                }
            } catch (e: GridBoundsException) {
                isFree = false
            }
            points.add(Vector(x, y))

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
            throw Exception("Failed to add a piece to the grid: position not empty. Piece: $piece")
        }

        for (point in points) {
            setAt(point, piece)
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
            setAt(point, null)
        }
    }

    fun print() {
        Gdx.app.debug("UnblockMe", toString())
    }
}
