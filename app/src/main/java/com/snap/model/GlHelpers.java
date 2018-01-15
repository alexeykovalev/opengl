package com.snap.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GlHelpers {

    public static final int BYTES_PER_FLOAT = 4;

    private GlHelpers() {
        throw new AssertionError("No instances");
    }


    public static FloatBuffer createNativeFloatBuffer(float[] withArray) {
        if (withArray == null || withArray.length == 0) {
            throw new IllegalArgumentException("Array has to be not empty.");
        }
        final ByteBuffer tmpBuffer = ByteBuffer.allocateDirect(withArray.length * BYTES_PER_FLOAT);
        tmpBuffer.order(ByteOrder.nativeOrder());
        final FloatBuffer result = tmpBuffer.asFloatBuffer();
        result.put(withArray);
        result.position(0);
        return result;
    }
}
