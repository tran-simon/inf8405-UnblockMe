package com.inf8405.tp1.view

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.inf8405.tp1.presenter.Presenter

class GameView(private var presenter: Presenter) : ApplicationAdapter() {
    var stage: Stage? = null
    var assetManager: AssetManager? = null
    private var backgroundTexture: Texture? = null
    override fun create() {
        stage = Stage(FitViewport(400f, 400f))

        Gdx.input.inputProcessor = stage

        presenter.loadLevel(1)


        assetManager = AssetManager()
        assetManager!!.load("background.jpg", Texture::class.java)
        assetManager!!.load("boat_large.png", Texture::class.java)
        assetManager!!.load("boat_small.png", Texture::class.java)
        assetManager!!.load("boat_main.png", Texture::class.java)
    }

    private fun drawBackground(batch: Batch){
        batch.begin()
        if (backgroundTexture == null) {
            backgroundTexture = assetManager!!.get<Texture>("background.jpg")
        }

        batch.draw(backgroundTexture, 0f, 0f, stage!!.width, stage!!.height)
        batch.end()
    }
    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        if (assetManager!!.update()) {

            stage!!.act()
            drawBackground(stage!!.batch)
            stage!!.draw()
        }
    }

    override fun dispose() {
        stage!!.dispose()
        assetManager!!.dispose()
    }
}
