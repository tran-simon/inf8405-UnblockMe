package com.inf8405.tp1.model.utils

import com.badlogic.gdx.math.Vector2

data class Vector(val x: Int, val y: Int) {
    fun toVector2(): Vector2 {
        return Vector2(x.toFloat(), y.toFloat())
    }
}
