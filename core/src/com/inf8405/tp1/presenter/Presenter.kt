package com.inf8405.tp1.presenter

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.inf8405.tp1.model.GameGrid
import com.inf8405.tp1.model.GamePiece
import com.inf8405.tp1.model.Orientation
import com.inf8405.tp1.model.utils.Vector
import com.inf8405.tp1.view.PieceActor

class Presenter(private val stage: Stage) {
    private val grid = GameGrid()

    private var selectedPieceActor: PieceActor? = null
    private var dragStartPosition: Vector2? = null

    init {
        // TODO: Remove these test pieces
        this.addPiece(GamePiece(Vector(1, 2), 3))
        this.addPiece(GamePiece(Vector(4, 2), 3, Orientation.VERTICAL))

        grid.print()
    }

    private fun addPiece(piece: GamePiece) {
        grid.addPiece(piece)

        val pieceActor = PieceActor(this, piece)
        stage.addActor(pieceActor)
    }

    private fun getScale(): Vector2 {
        return Vector2(stage.viewport.worldWidth / grid.width, stage.viewport.worldHeight / grid.height)
    }

    fun toWorldCoordinates(gridCoordinates: Vector): Vector2 {
        return gridCoordinates.toVector2().scl(getScale())
    }

    fun selectPieceActor(pieceActor: PieceActor, touchPosition: Vector2) {
        if(selectedPieceActor != null) return

        grid.removePiece(pieceActor.piece)
        selectedPieceActor = pieceActor
        dragStartPosition = touchPosition
    }

    fun unselectPieceActor(pieceActor: PieceActor) {
        if (pieceActor != selectedPieceActor) return

        grid.addPiece(selectedPieceActor!!.piece)
        selectedPieceActor = null
        dragStartPosition = null
    }

    fun movePieceActor(pieceActor: PieceActor, touchPosition: Vector2) {
        if (pieceActor != selectedPieceActor) return

        val delta = touchPosition.sub(dragStartPosition)
        if (pieceActor.piece.orientation == Orientation.HORIZONTAL) {
            delta.y = 0f
        } else {
            delta.x = 0f
        }

        val currentPosition = Vector2(pieceActor.x, pieceActor.y)
        val futurePosition = currentPosition.add(delta)

        // TODO: Check bounds

        pieceActor.setPosition(futurePosition.x, futurePosition.y)
    }
}
