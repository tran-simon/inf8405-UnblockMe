package com.inf8405.tp1.model

import com.inf8405.tp1.model.utils.Vector

enum class Orientation {
    HORIZONTAL, VERTICAL
}

/**
 * A game piece
 *
 * @param position The position of the bottom-left point of the piece
 * @param size Size of the piece
 * @param orientation If the piece is horizontal or vertical
 */
data class GamePiece(var position: Vector, val size: Int, val orientation: Orientation = Orientation.HORIZONTAL, val isMain: Boolean = false) {
    companion object {
        fun createMain(): GamePiece {
            return GamePiece(Vector(0, 3), 2, Orientation.HORIZONTAL, true)
        }
    }
}
