package com.snap.pointsdrawer;

import android.opengl.GLSurfaceView;

import com.snap.model.GlHelpers;
import com.snap.model.exception.GlLibException;
import com.snap.model.shading.Shader;
import com.snap.model.shading.ShadingProgram;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glViewport;

public class PointsSceneRenderer implements GLSurfaceView.Renderer {

    private final static String VERTEX_SHADER_CODE =
            "attribute vec4 a_Position;" +
                    "void main() {" +
                    "gl_Position = a_Position;" +
                    "gl_PointSize = 15.0;" +
                    "}";

    private final static String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
                    "uniform vec4 u_Color;" +
                    "void main() {" +
                    "gl_FragColor = u_Color;" +
                    "}";

    private ShadingProgram shadingProgram;
    private FloatBuffer pointsVertices;


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0f, 0f, 0f, 1f);
        setupPointsVertices();
        setupShadingProgram();
    }

    private void setupPointsVertices() {
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
        pointsVertices = GlHelpers.createNativeFloatBuffer(firstTriangle);
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        bindVertices(pointsVertices);


        bindBlueColor();
        glLineWidth(5.0f);
        glDrawArrays(GL_LINES, 0, 2);

        bindRedColor();
        glDrawArrays(GL_POINTS, 2, 1);
    }

    private void bindVertices(final FloatBuffer triangleVertices) {
        shadingProgram.executeAction(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                onProgram.createAttributeBinding("a_Position")
                        .bindVertices(triangleVertices, 3, false, 0);
            }
        });
    }

    private void bindBlueColor() {
        shadingProgram.executeAction(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                onProgram.createUniformBinding("u_Color")
                        .bindUniform4f(0f, 0f, 1f, 1f);
            }
        });
    }

    private void bindRedColor() {
        shadingProgram.executeAction(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                onProgram.createUniformBinding("u_Color")
                        .bindUniform4f(1f, 0f, 0f, 1f);
            }
        });
    }
}
