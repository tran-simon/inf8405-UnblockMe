package com.inf8405.tp1.presenter

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.utils.SnapshotArray
import com.badlogic.gdx.utils.XmlReader
import com.inf8405.tp1.interfaces.Launcher
import com.inf8405.tp1.model.GameGrid
import com.inf8405.tp1.model.GamePiece
import com.inf8405.tp1.model.Move
import com.inf8405.tp1.model.Orientation
import com.inf8405.tp1.model.utils.Vector
import com.inf8405.tp1.view.GameView
import com.inf8405.tp1.view.PieceActor
import kotlin.math.*

class Presenter(val launcher: Launcher) {
    var grid: GameGrid? = null

    var gameView: GameView? = null

    private var mainPieceActor: PieceActor? = null
    private var selectedPieceActor: PieceActor? = null
    private var dragStartPosition: Vector2? = null

    var active: Boolean = true

    var moves = arrayListOf<Move>()

    var bestScore: String? = null

    var level = 0

    init {
        gameView = GameView(this)
    }

    fun loadLevel(level: Int = 1) {
        this.level = level

        // Cleanup stage before loading new level
        val actors = SnapshotArray<Actor>(gameView!!.stage!!.actors)
        for (actor in actors) {
            actor.remove()
        }

        grid = GameGrid()
        active = true
        moves.clear()
        mainPieceActor = addPiece(GamePiece.createMain())
        selectedPieceActor = null

        // TODO: Add more levels
        val xml = XmlReader()
        val xmlRoot = xml.parse(Gdx.files.internal("level$level.xml"))
        val pieces = xmlRoot.getChildrenByName("piece")
        for (piece in pieces) {
            addPiece(GamePiece.fromXmlElement(piece))
        }
        bestScore = xmlRoot.getAttribute("best-score")

        launcher.updateUI()
    }

    private fun addPiece(piece: GamePiece): PieceActor {
        grid!!.addPiece(piece)

        val pieceActor = PieceActor(this, piece)
        gameView!!.stage!!.addActor(pieceActor)

        return pieceActor
    }

    private fun getScale(): Vector2 {
        return Vector2(gameView!!.stage!!.viewport.worldWidth / grid!!.width, gameView!!.stage!!.viewport.worldHeight / grid!!.height)
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
            val move = Move(pieceActor, pieceActor.piece.position)
            moves.add(move)
            pieceActor.piece.position = gridCoordinates
            launcher.updateUI()
        }

        grid!!.addPiece(pieceActor.piece)

        selectedPieceActor = null
        dragStartPosition = null
    }

    fun undoMove() {
        val move = moves.removeLastOrNull() ?: return

        val (pieceActor, previousGridPosition) = move
        grid!!.removePiece(pieceActor.piece)

        val worldCoordinates = toWorldCoordinates(previousGridPosition)
        pieceActor.setPosition(worldCoordinates.x, worldCoordinates.y)
        pieceActor.piece.position = previousGridPosition
        grid!!.addPiece(pieceActor.piece)
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

        val move = Move(mainPieceActor!!, mainPieceActor!!.piece.position)
        moves.add(move)
        launcher.updateUI()

        active = false

        val moveAction = MoveToAction()
        val endPosition = toWorldCoordinates(Vector(6, 3))
        moveAction.setPosition(endPosition.x, endPosition.y)
        moveAction.duration = 2f

        val completeAction: Action = object : Action() {
            override fun act(delta: Float): Boolean {
                launcher.win()
                return true
            }
        }
        mainPieceActor!!.addAction(SequenceAction(moveAction, completeAction))
    }
}
