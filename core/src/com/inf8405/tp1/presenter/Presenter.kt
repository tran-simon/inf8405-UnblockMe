package com.inf8405.tp1.presenter

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.utils.XmlReader
import com.inf8405.tp1.model.GameGrid
import com.inf8405.tp1.model.GamePiece
import com.inf8405.tp1.model.Orientation
import com.inf8405.tp1.model.utils.Vector
import com.inf8405.tp1.view.PieceActor
import kotlin.math.*

class Presenter(private val stage: Stage) {
    var grid: GameGrid? = null

    private var mainPieceActor: PieceActor? = null
    private var selectedPieceActor: PieceActor? = null
    private var dragStartPosition: Vector2? = null

    var active: Boolean = true
    var moves = 0

    init {
        loadLevel(1)
    }

    fun loadLevel(level: Int = 1) {
        // Cleanup stage before loading new level
        for (actor in stage.actors) {
            actor.remove()
        }

        grid = GameGrid()
        active = true
        moves = 0
        mainPieceActor = addPiece(GamePiece.createMain())

        // TODO: Add more levels
        val xml = XmlReader()
        val xmlRoot = xml.parse(Gdx.files.internal("level$level.xml"))
        val pieces = xmlRoot.getChildrenByName("piece")
        for (piece in pieces) {
            addPiece(GamePiece.fromXmlElement(piece))
        }
    }

    private fun addPiece(piece: GamePiece): PieceActor {
        grid!!.addPiece(piece)

        val pieceActor = PieceActor(this, piece)
        stage.addActor(pieceActor)

        return pieceActor
    }

    private fun getScale(): Vector2 {
        return Vector2(stage.viewport.worldWidth / grid!!.width, stage.viewport.worldHeight / grid!!.height)
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
        if (!active) return
        if (selectedPieceActor != null) return

        grid!!.removePiece(pieceActor.piece)
        selectedPieceActor = pieceActor
        dragStartPosition = touchPosition
    }

    fun unselectPieceActor(pieceActor: PieceActor) {
        if (!active) return
        if (pieceActor != selectedPieceActor) return

        val gridCoordinates = toGridCoordinates(pieceActor.getPosition())
        val worldCoordinates = toWorldCoordinates(gridCoordinates)
        pieceActor.setPosition(worldCoordinates.x, worldCoordinates.y)

        if (gridCoordinates != pieceActor.piece.position) {
            moves++
            pieceActor.piece.position = gridCoordinates
        }

        grid!!.addPiece(pieceActor.piece)

        selectedPieceActor = null
        dragStartPosition = null
    }

    fun movePieceActor(pieceActor: PieceActor, touchPosition: Vector2) {
        if (!active) return
        if (pieceActor != selectedPieceActor) return

        val delta = touchPosition.sub(dragStartPosition)
        val direction: Float
        val scale = getScale()

        val size = pieceActor.piece.size
        val orientation = pieceActor.piece.orientation

        if (orientation == Orientation.HORIZONTAL) {
            delta.y = 0f
            if (delta.x.absoluteValue > scale.x) {
                delta.x = scale.x * delta.x.sign
            }

            direction = delta.x.sign
        } else {
            delta.x = 0f
            if (delta.y.absoluteValue > scale.y) {
                delta.y = scale.y * delta.y.sign
            }
            direction = delta.y.sign
        }

        if (direction == 0f) {
            return
        }

        val coordinateConversionFunction = if (direction < 0f) CoordinateConversionFunction.FLOOR else CoordinateConversionFunction.CEIL

        val worldPosition = pieceActor.getPosition()

        val futurePosition = worldPosition.cpy().add(delta)

        val futureGridPosition = toGridCoordinates(futurePosition, coordinateConversionFunction)

        val (_, isFree) = grid!!.getPointsForPiece(GamePiece(futureGridPosition, size, orientation))


        if (isFree) {
            pieceActor.setPosition(futurePosition.x, futurePosition.y)
        } else {
            // If the future grid position isn't free, snap the piece to the previous valid grid position
            val gridPosition = toGridCoordinates(worldPosition, coordinateConversionFunction)
            pieceActor.setPosition(toWorldCoordinates(gridPosition))
        }
    }

    fun win() {
        if (!active) return

        active = false

        val moveAction = MoveToAction()
        val endPosition = toWorldCoordinates(Vector(6, 3))
        moveAction.setPosition(endPosition.x, endPosition.y)
        moveAction.duration = 2f

        val completeAction: Action = object : Action() {
            override fun act(delta: Float): Boolean {
                // TODO: Move to next puzzle
                Gdx.app.debug("UnblockMe", "Won")
                return true
            }
        }
        mainPieceActor!!.addAction(SequenceAction(moveAction, completeAction))
    }
}
