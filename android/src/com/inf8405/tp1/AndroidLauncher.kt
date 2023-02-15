package com.inf8405.tp1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.inf8405.tp1.interfaces.Launcher
import com.inf8405.tp1.presenter.Presenter


class AndroidLauncher : AndroidApplication(), Launcher {
    private var gamePresenter = Presenter(this)
    private var prevLevelBtn: ImageButton? = null
    private var nextLevelBtn: ImageButton? = null
    private var revertBtn: ImageButton? = null
    private var resetBtn: ImageButton? = null
    private var currentPuzzleTextView: TextView? = null
    private var movesCountTextView: TextView? = null
    private var recordTextView: TextView? = null

    private var recordsSharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val config = AndroidApplicationConfiguration()

        val layout = findViewById<ConstraintLayout>(R.id.layout_game)
        layout.addView(initializeForView(gamePresenter.gameView, config))

        recordsSharedPreferences = getSharedPreferences("records", Context.MODE_PRIVATE)

        movesCountTextView = findViewById(R.id.textView_movesCount)
        currentPuzzleTextView = findViewById(R.id.textView_currentPuzzle)
        recordTextView = findViewById(R.id.textView_record)
        prevLevelBtn = findViewById(R.id.button_prevPuzzle)
        nextLevelBtn = findViewById(R.id.button_nextPuzzle)
        revertBtn = findViewById(R.id.button_revert)
        resetBtn = findViewById(R.id.button_reset)

        prevLevelBtn!!.setOnClickListener {
            gamePresenter.loadLevel(--gamePresenter.level)
            updateUI()
        }

        nextLevelBtn!!.setOnClickListener {
            gamePresenter.loadLevel(++gamePresenter.level)
            updateUI()
        }

        revertBtn!!.setOnClickListener {
            gamePresenter.undoMove()
            updateUI()
        }
    }

    fun navigateToMenu(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun reset(view: View) {
        gamePresenter.loadLevel(gamePresenter.level)
        updateUI()
    }

    private fun createWinDialog() {
        val level = gamePresenter.level

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Puzzle Solved!")
        dialogBuilder.setMessage(String.format(getString(R.string.msg_win), gamePresenter.moves.size))

        runOnUiThread {
            val dialog = dialogBuilder.create()

            // Auto-hide the dialog after 3 seconds and go to next level
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()

                if (level != 3) {
                    gamePresenter.loadLevel(level + 1)
                }
            }, 3000)

            dialog.show()
        }
    }

    private fun getSavedScore(): Int {
        val level = gamePresenter.level
        return recordsSharedPreferences!!.getInt("level$level", -1)
    }

    private fun updateUIInternal() {
        val level = gamePresenter.level
        prevLevelBtn!!.visibility = if (level > 1) View.VISIBLE else View.INVISIBLE
        nextLevelBtn!!.visibility = if (level < 3) View.VISIBLE else View.INVISIBLE

        revertBtn!!.isEnabled = gamePresenter.moves.isNotEmpty()
        revertBtn!!.imageAlpha = if (revertBtn!!.isEnabled) 255 else 150

        resetBtn!!.isEnabled = gamePresenter.moves.isNotEmpty()
        resetBtn!!.imageAlpha = if (resetBtn!!.isEnabled) 255 else 150

        currentPuzzleTextView!!.text = level.toString()
        movesCountTextView!!.text = gamePresenter.moves.size.toString()

        val score = getSavedScore()
        val textScore = if (score == -1) "--" else score.toString()
        recordTextView!!.text = String.format(getString(R.string.record), textScore, gamePresenter.bestScore)
    }

    override fun updateUI() {
        runOnUiThread {
            updateUIInternal()
        }
    }

    override fun win() {
        val level = gamePresenter.level
        val score = getSavedScore()
        val currentScore = gamePresenter.moves.size

        if (currentScore < score || score == -1) {
            val editor = recordsSharedPreferences!!.edit()
            editor.putInt("level$level", currentScore)
            editor.apply()
        }

        createWinDialog()
    }
}
