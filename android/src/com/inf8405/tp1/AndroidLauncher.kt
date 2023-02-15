package com.inf8405.tp1

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.inf8405.tp1.presenter.Presenter


class AndroidLauncher : AndroidApplication() {
    private var gamePresenter = Presenter()
    private var prevLevelBtn: ImageButton? = null
    private var nextLevelBtn: ImageButton? = null
    private var revertBtn: ImageButton? = null
    private var currentPuzzleTextView: TextView? = null
    private var movesCountTextView: TextView? = null

    private var level = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val config = AndroidApplicationConfiguration()

        val layout = findViewById<ConstraintLayout>(R.id.layout_game)
        layout.addView(initializeForView(gamePresenter.gameView, config))

        movesCountTextView = findViewById(R.id.textView_movesCount)
        currentPuzzleTextView = findViewById(R.id.textView_currentPuzzle)
        prevLevelBtn = findViewById(R.id.button_prevPuzzle)
        nextLevelBtn = findViewById(R.id.button_nextPuzzle)
        revertBtn = findViewById(R.id.button_revert)

        prevLevelBtn!!.setOnClickListener {
            gamePresenter.loadLevel(--level)
            updateUI()
        }

        nextLevelBtn!!.setOnClickListener {
            gamePresenter.loadLevel(++level)
            updateUI()
        }

        revertBtn!!.setOnClickListener {
            gamePresenter.undoMove()
            updateUI()
        }

        gamePresenter.updateUI = {
            runOnUiThread() {
                updateUI()
            }
        }
    }

    private fun updateUI() {
        prevLevelBtn!!.isEnabled = level > 1
        nextLevelBtn!!.isEnabled = level < 3
        revertBtn!!.isEnabled = gamePresenter.moves.isNotEmpty()
        currentPuzzleTextView!!.text = level.toString()
        movesCountTextView!!.text = gamePresenter.moves.size.toString()
    }

    fun navigateToMenu(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun reset(view: View) {
        gamePresenter.loadLevel(level)
        updateUI()
    }
}
