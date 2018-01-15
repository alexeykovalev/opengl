package com.snap.model.shading;

import android.opengl.GLES20;

public class UniformBinding {

    private final String uniformName;
    private final int uniformHandle;

    public UniformBinding(int programHandle, String uniformName) {
        this.uniformName = uniformName;
        this.uniformHandle = GLES20.glGetUniformLocation(programHandle, uniformName);
    }

    public String getUniformName() {
        return uniformName;
    }

    public void bindUniformMatrix4fv(final float[] matrix) {
        GLES20.glUniformMatrix4fv(uniformHandle, 1, false, matrix, 0);
    }

    public void bindUniform3f(final float first, final float second, final float third) {
        GLES20.glUniform3f(uniformHandle, first, second, third);
    }

    public void bindUniform4f(final float first, final float second, final float third, final float fourth) {
        GLES20.glUniform4f(uniformHandle, first, second, third, fourth);
    }
}
