package com.inf8405.tp1.model.utils

data class Vector2D(val x: Float, val y: Float) {
    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())

    fun scale(scale: Vector2D): Vector2D {
        return Vector2D(x * scale.x, y * scale.y)
    }

    fun toIntVector(): Pair<Int, Int> {
        return Pair(x.toInt(), y.toInt())
    }
}
