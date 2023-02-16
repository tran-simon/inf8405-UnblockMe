package com.inf8405.tp1.model

import com.inf8405.tp1.model.utils.Vector
import com.inf8405.tp1.view.PieceActor

/**
 * Represents a piece move
 */
data class Move(val pieceActor: PieceActor, val previousGridPosition: Vector)

