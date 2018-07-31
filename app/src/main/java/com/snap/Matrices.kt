package com.snap

import android.opengl.Matrix

fun identityMatrix() = FloatArray(16).apply {
    Matrix.setIdentityM(this, 0)
}