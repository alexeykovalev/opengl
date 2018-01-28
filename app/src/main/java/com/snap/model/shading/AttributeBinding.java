package com.snap.model.shading;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class AttributeBinding {

    private final String attributeName;
    private final int attributeHandle;

    public AttributeBinding(int programHandle, String attributeName) {
        this.attributeName = attributeName;
        this.attributeHandle = GLES20.glGetAttribLocation(programHandle, attributeName);
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void bindVertices(
            final FloatBuffer verticesData,
            final int sizePerItem,
            final boolean isNormalized,
            final int stride) {
        GLES20.glEnableVertexAttribArray(attributeHandle);
        GLES20.glVertexAttribPointer(attributeHandle, sizePerItem, GLES20.GL_FLOAT, isNormalized,
                stride, verticesData);
    }
}
