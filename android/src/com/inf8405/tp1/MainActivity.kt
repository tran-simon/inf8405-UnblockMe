package com.inf8405.tp1

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View

/**
 * This is the activity for the main menu.
 *
 * This is the screen we see when we open the app
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * Called when the "Play" button is pressed
     */
    fun startGame(view: View) {
        startActivity(Intent(this, AndroidLauncher::class.java))
    }

    /**
     * Called when the "About" button is pressed.
     *
     * Shows the about dialog
     */
    fun showAbout(view: View) {
        val aboutFragment = AboutDialogFragment()
        aboutFragment.show(supportFragmentManager, "about")
    }

    /**
     * Called when the "Exit" button is pressed
     */
    fun exitGame(view: View) {
        this.finishAffinity()
    }
}

/**
 * The About dialog.
 *
 * It displays the names of the authors of this game
 */
class AboutDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_about, null))
                // Add action buttons
                .setPositiveButton(R.string.ok
                ) { _, _ ->

                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}
