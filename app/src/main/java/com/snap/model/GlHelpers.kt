package com.snap.model

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

const val bytesPerFloat = 4

private fun createNativeOrderFloatBuffer(ofArray: FloatArray): FloatBuffer {
    if (ofArray.isEmpty()) {
        throw IllegalArgumentException("Array has to be not empty.")
    }
    val tmpBuffer = ByteBuffer.allocateDirect(ofArray.size * bytesPerFloat)
    tmpBuffer.order(ByteOrder.nativeOrder())
    val result = tmpBuffer.asFloatBuffer()
    result.put(ofArray)
    result.position(0)
    return result
}

fun FloatArray.toNativeOrderBuffer(): FloatBuffer = createNativeOrderFloatBuffer(this)
