package com.inf8405.tp1.model

import com.inf8405.tp1.model.utils.Vector2D

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
data class GamePiece(val position: Vector2D, val size: Int, val orientation: Orientation = Orientation.HORIZONTAL)
