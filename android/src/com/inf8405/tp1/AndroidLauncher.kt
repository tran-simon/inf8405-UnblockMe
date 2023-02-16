package com.inf8405.tp1

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
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

/**
 * The game LibGDX launcher.
 */
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
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Initialize the AndroidLauncher
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_win)

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


        /* Button click listeners*/

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

    /**
     * Called when the menu button is pressed.
     *
     * This redirects to the main menu
     */
    fun navigateToMenu(view: View) {
        finish()
    }

    /**
     * Called when the reset button is pressed.
     *
     * This reloads the current level
     */
    fun reset(view: View) {
        gamePresenter.loadLevel(gamePresenter.level)
        updateUI()
    }

    /**
     * Creates and opens the dialog when the user has completed a puzzle.
     *
     * The dialog will automatically hide after 3 seconds and launch the next puzzle (if it exists).
     */
    private fun createWinDialog() {
        val level = gamePresenter.level

        /* Create the Dialog */
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Puzzle Solved!")
        dialogBuilder.setMessage(String.format(getString(R.string.msg_win), gamePresenter.moves.size))

        runOnUiThread {
            val dialog = dialogBuilder.create()

            /* Auto-hide the dialog after 3 seconds and go to next level */
            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()

                if (level != 3) {
                    gamePresenter.loadLevel(level + 1)
                }
            }, 3000)

            dialog.show()
        }
    }

    /**
     * Get the saved high score. -1 if the user never completed the puzzle.
     */
    private fun getSavedScore(): Int {
        val level = gamePresenter.level
        return recordsSharedPreferences!!.getInt("level$level", -1)
    }

    /**
     * Update the UI with the current values from the GamePresenter
     */
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

    /**
     * Called by the GamePresenter if the UI needs to be updated
     */
    override fun updateUI() {
        runOnUiThread {
            updateUIInternal()
        }
    }

    /**
     * Called when the user has completed a puzzle.
     *
     * This updates the high score if necessary, plays the win sound and opens the win dialog
     */
    override fun win() {
        val level = gamePresenter.level
        val score = getSavedScore()
        val currentScore = gamePresenter.moves.size

        if (currentScore < score || score == -1) {
            val editor = recordsSharedPreferences!!.edit()
            editor.putInt("level$level", currentScore)
            editor.apply()
        }

        mediaPlayer!!.start()
        createWinDialog()
    }

    /**
     * Called when the application exits.
     *
     * This releases some assets to save resources
     */
    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
