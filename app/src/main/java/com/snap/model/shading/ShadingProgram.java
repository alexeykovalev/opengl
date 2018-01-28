package com.snap.model.shading;

import android.opengl.GLES20;

import com.snap.model.exception.GlException;
import com.snap.model.exception.GlLibException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents Pair of shaders - Vertex and Fragment.
 */
public class ShadingProgram {

    private static final int NOT_DEFINED_PROGRAM_HANDLE = 0;

    /**
     * Handle (reference) to shading program.
     */
    private int mProgramHandle;

    private final ShadingPair mShadingPair;

    public static int createShadingProgram(ShadingPair shadingPair) throws GlLibException {
        final int shadingProgramHandle = GLES20.glCreateProgram();
        if (shadingProgramHandle == NOT_DEFINED_PROGRAM_HANDLE) {
            throw new GlException("Could not create shading program.");
        }
        shadingPair.setupAndAttachToProgram(shadingProgramHandle);
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

    public ShadingProgram(ShadingPair shadingPair) {
        mShadingPair = shadingPair;
    }

    public boolean isSetup() {
        return mProgramHandle != NOT_DEFINED_PROGRAM_HANDLE;
    }

    public void setup() throws GlLibException {
        if (isSetup()) {
            throw new GlLibException("Shading program already setup.");
        }
        mProgramHandle = createShadingProgram(mShadingPair);
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

    public void executeAction(final ShadingProgramAction shadingProgramActionToExecute) {
        useProgram();
        shadingProgramActionToExecute.execute(this);
    }

    public AttributeBinding createAttributeBinding(final String attributeName) {
        return new AttributeBinding(mProgramHandle, attributeName);
    }

    public UniformBinding createUniformBinding(final String uniformName) {
        return new UniformBinding(mProgramHandle, uniformName);
    }

    public static class ShadingPair {

        private final Shader vertexShader;
        private final Shader fragmentShader;

        public ShadingPair(Shader vertexShader, Shader fragmentShader) {
            if (vertexShader.getType() != Shader.ShaderType.VERTEX) {
                throw new IllegalArgumentException("First param has to be a Vertex shader.");
            }
            if (fragmentShader.getType() != Shader.ShaderType.FRAGMENT) {
                throw new IllegalArgumentException("Second param has to be a Fragment shader.");
            }
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }

        public Shader getVertexShader() {
            return vertexShader;
        }

        public Shader getFragmentShader() {
            return fragmentShader;
        }

        void setupAndAttachToProgram(final int shadingProgramHandle) throws GlLibException {
            vertexShader.setup();
            GLES20.glAttachShader(shadingProgramHandle, vertexShader.getShaderHandle());
            fragmentShader.setup();
            GLES20.glAttachShader(shadingProgramHandle, fragmentShader.getShaderHandle());
        }
    }

    public interface ShadingProgramAction {

        void execute(ShadingProgram onProgram);
    }
}
