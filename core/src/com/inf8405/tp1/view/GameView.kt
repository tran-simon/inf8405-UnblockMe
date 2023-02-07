package com.inf8405.tp1.view

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.inf8405.tp1.presenter.Presenter

class GameView : ApplicationAdapter() {
    private var stage: Stage? = null
    private var presenter: Presenter? = null
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG // TODO: Remove
        stage = Stage(FitViewport(400f, 400f))
        presenter = Presenter(stage!!)

        Gdx.input.inputProcessor = stage
    }

    override fun render() {
        ScreenUtils.clear(1f, 0f, 0f, 1f)

        stage!!.act()
        stage!!.draw()
    }

    override fun dispose() {
        stage!!.dispose()
    }

    fun loadLevel(level: Int) {
        presenter!!.loadLevel(level)
    }
}
