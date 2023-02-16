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

/**
 * In the Model-View-Presenter pattern, this class is the presenter that will control what to display on the view based on the model
 */
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

    /**
     * Load a new level.
     *
     * If the current level is passed as an argument, it will reset the current puzzle.
     */
    fun loadLevel(level: Int = 1) {
        this.level = level

        /* Cleanup stage before loading new level */
        val actors = SnapshotArray<Actor>(gameView!!.stage!!.actors)
        for (actor in actors) {
            actor.remove()
        }

        /* Reinitialize game data */
        grid = GameGrid()
        active = true
        moves.clear()
        mainPieceActor = addPiece(GamePiece.createMain())
        selectedPieceActor = null

        /* Load the new level */
        val xml = XmlReader()
        val xmlRoot = xml.parse(Gdx.files.internal("level$level.xml"))
        val pieces = xmlRoot.getChildrenByName("piece")
        for (piece in pieces) {
            addPiece(GamePiece.fromXmlElement(piece))
        }
        bestScore = xmlRoot.getAttribute("best-score")

        launcher.updateUI()
    }

    /**
     * Add a new piece on the board.
     *
     * It will create a PieceActor for the piece
     */
    private fun addPiece(piece: GamePiece): PieceActor {
        grid!!.addPiece(piece)

        val pieceActor = PieceActor(this, piece)
        gameView!!.stage!!.addActor(pieceActor)

        return pieceActor
    }

    /**
     * Returns a vector describing the scale of the world (The world size divided by the grid size)
     */
    private fun getScale(): Vector2 {
        return Vector2(gameView!!.stage!!.viewport.worldWidth / grid!!.width, gameView!!.stage!!.viewport.worldHeight / grid!!.height)
    }

    /**
     * Convert a grid coordinate to a world coordinate.
     */
    fun toWorldCoordinates(gridCoordinates: Vector): Vector2 {
        return gridCoordinates.toVector2().scl(getScale())
    }

    enum class CoordinateConversionFunction {
        FLOOR, CEIL, ROUND
    }

    /**
     * Convert a world coordinate to a grid coordinate
     *
     * The `coordinateConversationFunction` can be used to chose how to round the float values.
     */
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

    /**
     * Called when a user presses on a piece to select it
     */
    fun selectPieceActor(pieceActor: PieceActor, touchPosition: Vector2) {
        if (!active) return
        if (selectedPieceActor != null) return

        grid!!.removePiece(pieceActor.piece)
        pieceActor.selected = true
        selectedPieceActor = pieceActor
        dragStartPosition = touchPosition
    }

    /**
     * Called when the user stops pressing the piece
     */
    fun unselectPieceActor(pieceActor: PieceActor) {
        if (!active) return
        if (pieceActor != selectedPieceActor) return

        val gridCoordinates = toGridCoordinates(pieceActor.getPosition())
        val worldCoordinates = toWorldCoordinates(gridCoordinates)
        pieceActor.setPosition(worldCoordinates.x, worldCoordinates.y)
        pieceActor.selected = false

        /* If the piece moved, add a move object to the stack */
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

    /**
     * Used to undo a movement.
     *
     * It will pop the `moves` stack and place the last moved piece to its last position
     */
    fun undoMove() {
        val move = moves.removeLastOrNull() ?: return

        val (pieceActor, previousGridPosition) = move
        grid!!.removePiece(pieceActor.piece)

        val worldCoordinates = toWorldCoordinates(previousGridPosition)
        pieceActor.setPosition(worldCoordinates.x, worldCoordinates.y)
        pieceActor.piece.position = previousGridPosition
        grid!!.addPiece(pieceActor.piece)
    }

    /**
     * Used when the user drags the piece to move it.
     */
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
            /* If the future grid position isn't free, snap the piece to the previous valid grid position */
            val gridPosition = toGridCoordinates(worldPosition, coordinateConversionFunction)
            pieceActor.setPosition(toWorldCoordinates(gridPosition))
        }
    }

    /**
     * Called when the user dragged the main piece to the hole.
     *
     * It will start the winning animation, then call the `win` method of the launcher
     */
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
