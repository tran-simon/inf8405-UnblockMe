package com.inf8405.tp1.model.utils

import com.badlogic.gdx.math.Vector2

typealias ToIntFunction = (value: Float) -> Int
data class Vector(val x: Int, val y: Int) {
    companion object {
        fun fromVector2(vector2: Vector2, toIntFunction: ToIntFunction): Vector {
            return Vector(toIntFunction(vector2.x), toIntFunction(vector2.y))
        }
    }

    fun toVector2(): Vector2 {
        return Vector2(x.toFloat(), y.toFloat())
    }
}
