package com.snap.model.shading

import android.opengl.GLES20

class UniformBinding(programHandle: Int, uniformName: String) {

    private val uniformHandle: Int = GLES20.glGetUniformLocation(programHandle, uniformName)

    fun bindUniformMatrix4fv(matrix: FloatArray) {
        GLES20.glUniformMatrix4fv(uniformHandle, 1, false, matrix, 0)
    }

    fun bindUniform3f(first: Float, second: Float, third: Float) {
        GLES20.glUniform3f(uniformHandle, first, second, third)
    }

    fun bindUniform4f(first: Float, second: Float, third: Float, fourth: Float) {
        GLES20.glUniform4f(uniformHandle, first, second, third, fourth)
    }
}
