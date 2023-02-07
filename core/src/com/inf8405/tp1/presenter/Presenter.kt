package com.inf8405.tp1.presenter

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.inf8405.tp1.model.GameGrid
import com.inf8405.tp1.model.GamePiece
import com.inf8405.tp1.model.Orientation
import com.inf8405.tp1.model.utils.Vector
import com.inf8405.tp1.view.PieceActor
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

class Presenter(private val stage: Stage) {
    val grid = GameGrid()

    private var selectedPieceActor: PieceActor? = null
    private var dragStartPosition: Vector2? = null

    init {
        addPiece(GamePiece.createMain())

        // TODO: Remove these test pieces
        addPiece(GamePiece(Vector(1, 2), 3))
        addPiece(GamePiece(Vector(4, 2), 3, Orientation.VERTICAL))

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

    enum class CoordinateConversionFunction {
        FLOOR, CEIL, ROUND
    }

    fun toGridCoordinates(
        worldCoordinates: Vector2,
        coordinateConversionFunction: CoordinateConversionFunction = CoordinateConversionFunction.ROUND
    ): Vector {
        val scale = getScale()
        return Vector.fromVector2(Vector2(worldCoordinates.x / scale.x, worldCoordinates.y / scale.y)) { value ->
            when (coordinateConversionFunction) {
                CoordinateConversionFunction.FLOOR -> floor(value)
                CoordinateConversionFunction.CEIL -> ceil(value)
                CoordinateConversionFunction.ROUND -> round(value)
            }.toInt()
        }
    }

    fun selectPieceActor(pieceActor: PieceActor, touchPosition: Vector2) {
        if (selectedPieceActor != null) return

        grid.removePiece(pieceActor.piece)
        selectedPieceActor = pieceActor
        dragStartPosition = touchPosition
    }

    fun unselectPieceActor(pieceActor: PieceActor) {
        if (pieceActor != selectedPieceActor) return

        val gridCoordinates = toGridCoordinates(pieceActor.getPosition())
        val worldCoordinates = toWorldCoordinates(gridCoordinates)
        pieceActor.setPosition(worldCoordinates.x, worldCoordinates.y)
        pieceActor.piece.position = gridCoordinates
        grid.addPiece(pieceActor.piece)

        selectedPieceActor = null
        dragStartPosition = null
    }

    fun movePieceActor(pieceActor: PieceActor, touchPosition: Vector2) {
        if (pieceActor != selectedPieceActor) return

        val delta = touchPosition.sub(dragStartPosition)
        val scalarDelta: Float

        val size = pieceActor.piece.size
        val orientation = pieceActor.piece.orientation

        if (orientation == Orientation.HORIZONTAL) {
            delta.y = 0f
            scalarDelta = delta.x
        } else {
            delta.x = 0f
            scalarDelta = delta.y
        }

        if (scalarDelta == 0f) {
            return
        }

        val coordinateConversionFunction = if (scalarDelta < 0f) CoordinateConversionFunction.FLOOR else CoordinateConversionFunction.CEIL

        val worldPosition = pieceActor.getPosition()

        val futurePosition = worldPosition.cpy().add(delta) // TODO add only one block

        val futureGridPosition = toGridCoordinates(futurePosition, coordinateConversionFunction)

        val (_, isFree) = grid.getPointsForPiece(GamePiece(futureGridPosition, size, orientation))


        if (isFree) {
            pieceActor.setPosition(futurePosition.x, futurePosition.y)
        } else {
            // If the future grid position isn't free, snap the piece to the previous valid grid position
            val gridPosition = toGridCoordinates(worldPosition, coordinateConversionFunction)
            pieceActor.setPosition(toWorldCoordinates(gridPosition))
        }
    }
}
