package com.inf8405.tp1

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.inf8405.tp1.view.GameView

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val config = AndroidApplicationConfiguration()

        val layout = findViewById<ConstraintLayout>(R.id.layout_game)
        layout.addView(initializeForView(GameView(), config))
    }
}
