package com.snap.model

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

const val bytesPerFloat = 4

fun createNativeFloatBuffer(withArray: FloatArray?): FloatBuffer {
    if (withArray == null || withArray.size == 0) {
        throw IllegalArgumentException("Array has to be not empty.")
    }
    val tmpBuffer = ByteBuffer.allocateDirect(withArray.size * bytesPerFloat)
    tmpBuffer.order(ByteOrder.nativeOrder())
    val result = tmpBuffer.asFloatBuffer()
    result.put(withArray)
    result.position(0)
    return result
}
