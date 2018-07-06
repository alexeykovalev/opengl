package com.snap.model.shading

import android.opengl.GLES20

import java.nio.FloatBuffer

class AttributeBinding(programHandle: Int, val attributeName: String) {

    private val attributeHandle: Int = GLES20.glGetAttribLocation(programHandle, attributeName)

    fun bindFloatBuffer(
            floatBuffer: FloatBuffer,
            sizePerItem: Int,
            isNormalized: Boolean,
            stride: Int) {
        GLES20.glEnableVertexAttribArray(attributeHandle)
        GLES20.glVertexAttribPointer(attributeHandle, sizePerItem, GLES20.GL_FLOAT, isNormalized,
                stride, floatBuffer)
    }
}
