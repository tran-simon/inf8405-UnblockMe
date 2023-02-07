package com.inf8405.tp1.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import com.inf8405.tp1.model.GamePiece
import com.inf8405.tp1.model.Orientation
import com.inf8405.tp1.model.utils.Vector
import com.inf8405.tp1.presenter.Presenter


class PieceActor(val presenter: Presenter, val piece: GamePiece) : Actor() {
    private var img: Texture? = null

    init {
        img = Texture("badlogic.jpg")

        val position = presenter.toWorldCoordinates(piece.position)
        val vectorSize = if (piece.orientation == Orientation.HORIZONTAL) Vector(piece.size, 1) else Vector(1, piece.size)
        val size = presenter.toWorldCoordinates(vectorSize)

        setPosition(position.x, position.y)
        setSize(size.x, size.y)

        addListener(object : DragListener() {
            override fun dragStart(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.dragStart(event, x, y, pointer)
                presenter.selectPieceActor(this@PieceActor, Vector2(x, y))
            }

            override fun dragStop(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.dragStop(event, x, y, pointer)
                presenter.unselectPieceActor(this@PieceActor)
            }

            override fun drag(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.drag(event, x, y, pointer)
                presenter.movePieceActor(this@PieceActor, Vector2(x, y))
            }
        })
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch!!.draw(img, x, y, width, height)
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (piece.isMain && presenter.active) {
            val gridPosition = presenter.toGridCoordinates(getPosition(), Presenter.CoordinateConversionFunction.FLOOR)
            if (gridPosition.x + piece.size == presenter.grid!!.width) {
                presenter.win()
            }
        }
    }

    fun getPosition(): Vector2 {
        return Vector2(x, y)
    }

    fun setPosition(position: Vector2) {
        x = position.x
        y = position.y
    }
}
