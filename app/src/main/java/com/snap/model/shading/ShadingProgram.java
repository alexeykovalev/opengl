package com.snap.model.shading;

import android.opengl.GLES20;

import com.snap.model.ParamBinding;
import com.snap.model.exception.GlException;
import com.snap.model.exception.GlLibException;

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

    public void executeUsingProgram(final ShadingProgramAction shadingProgramActionToExecute) {
        useProgram();
        shadingProgramActionToExecute.execute(this);
    }

    public ParamBinding createAttributeBinding(final String attributeName) {
        return ParamBinding.ofAttribute(mProgramHandle, attributeName);
    }

    public ParamBinding createUniformBinding(final String uniformName) {
        return ParamBinding.ofUniform(mProgramHandle, uniformName);
    }

    public interface ShadingProgramAction {

        void execute(ShadingProgram withProgram);
    }
}
