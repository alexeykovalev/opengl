package com.snap.coloredtriangle;

import android.opengl.GLSurfaceView;

import com.snap.model.GlHelpers;
import com.snap.model.exception.GlLibException;
import com.snap.model.shading.Shader;
import com.snap.model.shading.ShadingProgram;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glViewport;

public class ColoredTriangleRenderer implements GLSurfaceView.Renderer {

    private final static String VERTEX_SHADER_CODE =
            "attribute vec4 a_Position;" +
            "void main() {" +
                "gl_Position = a_Position;" +
            "}";

    private final static String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "uniform vec4 u_Color;" +
            "void main() {" +
                "gl_FragColor = u_Color;" +
            "}";

    private List<FloatBuffer> triangles = new ArrayList<>();
    private ShadingProgram shadingProgram;

    @Override
    public void onSurfaceCreated(GL10 ignore, EGLConfig config) {
        glClearColor(0f, 0f, 0f, 1f);
        setupVertices();
        setupShadingProgram();
    }

    private void setupVertices() {
        final float[] firstTriangle = {
                -0.5f, -0.2f, 0f,
                0, 0.2f, 0f,
                0.5f, -0.2f, 0f,
        };
        final float[] secondTriangle = {
                -0.5f, -0.2f, 0f,
                0.5f, -0.2f, 0f,
                0f, -0.6f, 0f
        };
        triangles.add(GlHelpers.createNativeFloatBuffer(firstTriangle));
        triangles.add(GlHelpers.createNativeFloatBuffer(secondTriangle));
    }

    private void setupShadingProgram() {
        Shader vertexShader = Shader.fromSourceCode(Shader.ShaderType.VERTEX, VERTEX_SHADER_CODE);
        Shader fragmentShader = Shader.fromSourceCode(Shader.ShaderType.FRAGMENT, FRAGMENT_SHADER_CODE);
        shadingProgram = new ShadingProgram(new ShadingProgram.ShadingPair(vertexShader, fragmentShader));
        try {
            shadingProgram.setup();
        } catch (GlLibException e) {
            throw new RuntimeException("Unable to setup shading program", e);
        }
    }

    private void bindData(final FloatBuffer triangleVertices) {
        shadingProgram.executeUsingProgram(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram withProgram) {
                withProgram.createAttributeBinding("a_Position")
                        .bindVertices(triangleVertices, 3, false, 0);
                withProgram.createUniformBinding("u_Color")
                        .bindUniform4f(0f, 0f, 1f, 1f);
            }
        });
    }

    @Override
    public void onSurfaceChanged(GL10 ignore, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 ignore) {
        glClear(GL_COLOR_BUFFER_BIT);
        for (FloatBuffer triangleVertices : triangles) {
            drawTriangle(triangleVertices);
        }
    }

    private void drawTriangle(final FloatBuffer triangleVertices) {
        bindData(triangleVertices);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
