package com.snap.model.shading

import android.opengl.GLES20

import java.nio.FloatBuffer

/**
 * Each point in 3D scene has associated attributes with it - like:
 * vertex position;
 * vertex color;
 * texture coordinates;
 * normal direction;
 */
class AttributeBinding(programHandle: Int, attributeName: String) {

    private val attributeHandle: Int = GLES20.glGetAttribLocation(programHandle, attributeName)

    fun bindFloatBuffer(
            floatBuffer: FloatBuffer,
            sizePerItem: Int,
            isNormalized: Boolean = false,
            stride: Int = 0) {
        GLES20.glEnableVertexAttribArray(attributeHandle)
        GLES20.glVertexAttribPointer(attributeHandle, sizePerItem, GLES20.GL_FLOAT, isNormalized,
                stride, floatBuffer)
    }

    fun bind4f(first: Float, second: Float, third: Float, fourth: Float) {
        GLES20.glVertexAttrib4f(attributeHandle, first, second, third, fourth)
    }

}
