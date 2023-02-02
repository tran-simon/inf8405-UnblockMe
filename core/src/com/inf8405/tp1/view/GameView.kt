package com.inf8405.tp1.view

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.inf8405.tp1.model.GameGrid
import com.inf8405.tp1.model.GamePiece
import com.inf8405.tp1.model.Orientation
import com.inf8405.tp1.model.utils.Vector2D

class GameView : ApplicationAdapter() {
    var batch: SpriteBatch? = null
    var img: Texture? = null

    private val camera = OrthographicCamera()
    private val grid = GameGrid()
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG // TODO: Remove

        camera.setToOrtho(false, 400f, 400f)
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")

        // TODO: Remove these test pieces
        grid.addPiece(GamePiece(Vector2D(1, 2), 3))
        grid.addPiece(GamePiece(Vector2D(4, 2), 3, Orientation.VERTICAL))

        grid.print()
    }

    override fun render() {
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        camera.update()
        val scale = Vector2D(camera.viewportWidth / grid.width, camera.viewportHeight / grid.height)
        batch!!.projectionMatrix = camera.combined


        batch!!.begin()

        for ((position, size, orientation) in grid.pieces) {
            val (x, y) = scale.scale(position)
            val unscaledSize = if (orientation == Orientation.HORIZONTAL) Vector2D(size, 1) else Vector2D(1, size)
            val (width, height) = scale.scale(unscaledSize)

            batch!!.draw(img, x, y, width, height)
        }

        batch!!.end()
    }

    override fun dispose() {
        batch!!.dispose()
        img!!.dispose()
    }
}
