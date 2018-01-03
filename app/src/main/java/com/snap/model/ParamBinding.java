package com.snap.model;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

// TODO: 1/3/18 oleksiikovalov separate AttributeBinding and UniformBinding
public class ParamBinding {

    public static ParamBinding ofAttribute(final int programHandle, final String attributeName) {
        return new ParamBinding(programHandle, attributeName, BindingType.Attribute);
    }

    public static ParamBinding ofUniform(final int programHandle, final String uniformName) {
        return new ParamBinding(programHandle, uniformName, BindingType.Uniform);
    }

    private final BindingType bindingType;
    private final int programHandle;

    private final String paramName;
    private final int paramHandle;

    private ParamBinding(int programHandle, String paramName, BindingType bindingType) {
        this.programHandle = programHandle;
        this.paramName = paramName;
        this.bindingType = bindingType;
        this.paramHandle = bindingType == BindingType.Attribute ?
                GLES20.glGetAttribLocation(programHandle, paramName) :
                GLES20.glGetUniformLocation(programHandle, paramName);
    }

    public void bindFloatBuffer(
            final FloatBuffer binaryData,
            final int sizePerItem,
            final boolean isNormalized,
            final int stride) {
        GLES20.glEnableVertexAttribArray(paramHandle);
        GLES20.glVertexAttribPointer(paramHandle, sizePerItem, GLES20.GL_FLOAT, isNormalized,
                stride, binaryData);
    }




    public void bindUniformMatrix4fv(final float[] matrix) {
        GLES20.glUniformMatrix4fv(paramHandle, 1, false, matrix, 0);
    }

    public void bindUniform3f(final float first, final float second, final float third) {
        GLES20.glUniform3f(paramHandle, first, second, third);
    }

    public enum BindingType {
        Attribute, Uniform
    }
}
