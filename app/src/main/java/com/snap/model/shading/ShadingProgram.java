package com.snap.model.shading;

import android.opengl.GLES20;

import com.snap.model.ParamBinding;
import com.snap.model.exception.GlException;
import com.snap.model.exception.GlLibException;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShadingProgram {

    private static final int NOT_DEFINED_PROGRAM_HANDLE = 0;

    /**
     * Handle (reference) to shading program.
     */
    private int mProgramHandle;

    private final List<Shader> mShaders;

    public static int createShadingProgram(List<Shader> shaders) throws GlLibException {
        final int shadingProgramHandle = GLES20.glCreateProgram();
        if (shadingProgramHandle == NOT_DEFINED_PROGRAM_HANDLE) {
            throw new GlException("Could not create shading program.");
        }
        for (Shader shader : shaders) {
            shader.setup();
            GLES20.glAttachShader(shadingProgramHandle, shader.getShaderHandle());
        }
        GLES20.glLinkProgram(shadingProgramHandle);
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(shadingProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            String errorMessage = GLES20.glGetProgramInfoLog(shadingProgramHandle);
            GLES20.glDeleteProgram(shadingProgramHandle);
            throw new GlException("Could not link shading program. " + errorMessage);
        }
        return shadingProgramHandle;
    }

    /**
     * Order of shaders is important Vertex -> Fragment
     */
    public ShadingProgram(List<Shader> shaders) {
        if (shaders.isEmpty()) {
            throw new IllegalArgumentException("Shading program should contains shaders - you haven't passed any.");
        }
        mShaders = Collections.unmodifiableList(new ArrayList<>(shaders));
    }

    public boolean isSetup() {
        return mProgramHandle != NOT_DEFINED_PROGRAM_HANDLE;
    }

    public void setup() throws GlLibException {
        if (isSetup()) {
            throw new GlLibException("Shading program already setup.");
        }
        mProgramHandle = createShadingProgram(mShaders);
    }

    public void release() {
        if (isSetup()) {
            GLES20.glDeleteProgram(mProgramHandle);
            mProgramHandle = NOT_DEFINED_PROGRAM_HANDLE;
        }
    }

    public void useProgram() {
        if (!isSetup()) {
            throw new IllegalStateException("You have to setup program before using it.");
        }
        GLES20.glUseProgram(mProgramHandle);
    }


    // TODO: 1/3/18 oleksiikovalov has to be removed to wrapper - LightSceneShadingProgram

    public void linkVertexBuffer(FloatBuffer vertexBuffer) {
        useProgram();
        ParamBinding.ofAttribute(mProgramHandle, "a_vertex")
                .bindFloatBuffer(vertexBuffer, 3, false, 0);
    }

    public void linkNormalBuffer(FloatBuffer normalBuffer) {
        useProgram();
        ParamBinding.ofAttribute(mProgramHandle, "a_normal")
                .bindFloatBuffer(normalBuffer, 3, false, 0);
    }

    public void linkColorBuffer(FloatBuffer colorBuffer) {
        useProgram();
        ParamBinding.ofAttribute(mProgramHandle, "a_color")
                .bindFloatBuffer(colorBuffer, 4, false, 0);
    }

    public void linkModelViewProjectionMatrix(float[] modelViewProjectionMatrix) {
        useProgram();
        ParamBinding.ofUniform(mProgramHandle, "u_modelViewProjectionMatrix")
                .bindUniformMatrix4fv(modelViewProjectionMatrix);
    }

    public void linkCamera(float xCamera, float yCamera, float zCamera) {
        useProgram();
        ParamBinding.ofUniform(mProgramHandle, "u_camera")
                .bindUniform3f(xCamera, yCamera, zCamera);
    }

    public void linkLightSource(float xLightPosition, float yLightPosition, float zLightPosition) {
        useProgram();
        ParamBinding.ofUniform(mProgramHandle, "u_lightPosition")
                .bindUniform3f(xLightPosition, yLightPosition, zLightPosition);
    }

}
