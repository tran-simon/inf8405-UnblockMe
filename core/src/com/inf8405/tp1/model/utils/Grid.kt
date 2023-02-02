package com.inf8405.tp1.model.utils

class Grid<T>(private val width: Int, private val height: Int) {
    private val grid = MutableList(height) { MutableList<T?>(width) { null } }

    private fun checkBounds(position: Vector2D) {
        if (position.x < 0 || position.x >= width || position.y < 0 || position.y >= height) {
            throw GridBoundsException(width, height, position)
        }
    }

    fun setAt(position: Vector2D, piece: T?) {
        checkBounds(position)
        val (x, y) = position.toIntVector()
        grid[y][x] = piece
    }

    fun getAt(position: Vector2D): T? {
        checkBounds(position)
        val (x, y) = position.toIntVector()
        return grid[y][x]
    }

    /**
     * Stringify the grid.
     * '0' is used for an empty point, '1' if it is not empty
     */
    override fun toString(): String {
        var string = ""
        for (i in grid.lastIndex downTo 0) {
            for (value in grid[i]) {
                string += if (value != null) "1" else "0"
            }
            string += "\n"
        }

        return string
    }
}

class GridBoundsException(width: Int, height: Int, point: Vector2D) : Exception("Point $point is out of bounds (width=$width, height=$height)")
