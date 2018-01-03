package com.snap.lighting;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.snap.model.exception.GlLibException;
import com.snap.model.shading.Shader;
import com.snap.model.shading.ShadingProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

public class SceneLightRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;

    // TODO: 1/3/18 oleksiikovalov has to be removed to wrapper - LightSceneShadingProgram
    private static final String VERTEX_SHADER_CODE =
            "uniform mat4 u_modelViewProjectionMatrix;" +

            "attribute vec3 a_vertex;" +
            "attribute vec3 a_normal;" +
            "attribute vec4 a_color;" +

            "varying vec3 v_vertex;" +
            "varying vec3 v_normal;" +
            "varying vec4 v_color;" +

            "void main() {" +
                "v_vertex = a_vertex;" +
                "vec3 n_normal = normalize(a_normal);" +
                "v_normal = n_normal;" +
                "v_color = a_color;" +
                "gl_Position = u_modelViewProjectionMatrix * vec4(a_vertex, 1.0);" +
            "}";

    // TODO: 1/3/18 oleksiikovalov has to be removed to wrapper - LightSceneShadingProgram
    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +

            "uniform vec3 u_camera;" +
            "uniform vec3 u_lightPosition;" +

            "varying vec3 v_vertex;" +
            "varying vec3 v_normal;" +
            "varying vec4 v_color;" +

            "void main() {" +
                "vec3 n_normal = normalize(v_normal);" +
                "vec3 lightvector = normalize(u_lightPosition - v_vertex);" +
                "vec3 lookvector = normalize(u_camera - v_vertex);" +
                "float ambient = 0.0; /*0.2*/" +
                "float k_diffuse = 1.0; /*0.3*/" +
                "float k_specular = 0.0; /*0.5*/" +
                "float diffuse = k_diffuse * max(dot(n_normal, lightvector), 0.0);" +
                "vec3 reflectvector = reflect(-lightvector, n_normal);" +
                "float specular = k_specular * pow(max(dot(lookvector,reflectvector),0.0), 40.0 );" +
                "vec4 one = vec4(1.0, 1.0, 1.0, 1.0);" +
                "vec4 lightColor = (ambient + diffuse + specular)*one;" +
                "gl_FragColor = mix(lightColor, v_color, 0.0);" +
            "}";

    private float xСameraPosition, yCameraPosition, zCameraPosition;

    private float xLightPosition, yLightPosition, zLightPosition;

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    private FloatBuffer mSeaVerticesBuffer;
    private FloatBuffer mSkyVerticesBuffer;
    private FloatBuffer mMainSailVerticesBuffer;
    private FloatBuffer mSmallSailVerticesBuffer;
    private FloatBuffer mBoatVerticesBuffer;

    private FloatBuffer mVerticesNormalsBuffer;

    private FloatBuffer mSeaVerticesColorsBuffer;
    private FloatBuffer mSkyVerticesColorsBuffer;
    private FloatBuffer mAnySailVerticesColorsBuffer;
    private FloatBuffer mBoatVerticesColorsBuffer;

    private ShadingProgram mSeaShader;
    private ShadingProgram mSkyShader;
    private ShadingProgram mMainSailShader;
    private ShadingProgram mSmallSailShader;
    private ShadingProgram mBoatShader;


    public SceneLightRenderer() {
        setup();
    }

    private void setup() {
        setupLightSource();
        setupModelViewMatrix();
        setupVertexBuffers();
        setupNormalsBuffer();
        setupVerticesColorBuffers();
    }

    /**
     * Point source of light.
     */
    private void setupLightSource() {
        xLightPosition = 0.5f;
        yLightPosition = 0.2f;
        zLightPosition = 0.5f;
    }

    private void setupModelViewMatrix() {
        //мы не будем двигать объекты поэтому сбрасываем модельную матрицу на единичную
        Matrix.setIdentityM(modelMatrix, 0);

        //координаты камеры
        xСameraPosition = 0.0f;
        yCameraPosition = 0.0f;
        zCameraPosition = 3.0f;

        // пусть камера смотрит на начало координат
        // и верх у камеры будет вдоль оси Y
        // зная координаты камеры получаем матрицу вида
        Matrix.setLookAtM(
                viewMatrix, 0, xСameraPosition, yCameraPosition, zCameraPosition,
                0, 0, 0, 0, 1, 0);
        // умножая матрицу вида на матрицу модели
        // получаем матрицу модели-вида
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
    }

    private void setupVertexBuffers() {
        float[] seaVertices = {
                -1.0f, -0.35f, 0.0f,
                -1.0f, -1.5f, 0.0f,
                1.0f, -0.35f, 0.0f,
                1.0f, -1.5f, 0.0f
        };
        float[] skyVertices = {
                -1.0f, 1.5f, 0.0f,
                -1.0f, -0.35f, 0.0f,
                1.0f, 1.5f, 0.0f,
                1.0f, -0.35f, 0
        };
        float[] mainSailVertices = {
                -0.5f, -0.45f, 0.4f,
                0.0f, -0.45f, 0.4f,
                0.0f, 0.5f, 0.4f
        };
        float[] smallSailVertices = {
                0.05f, -0.45f, 0.4f,
                0.22f, -0.5f, 0.4f,
                0.0f, 0.25f, 0.4f
        };
        float[] boatVertices = {
                -0.5f, -0.5f, 0.4f,
                -0.5f, -0.6f, 0.4f,
                0.22f, -0.5f, 0.4f,
                0.18f, -0.6f, 0.4f
        };

        mSeaVerticesBuffer = createNativeFloatBuffer(seaVertices);
        mSkyVerticesBuffer = createNativeFloatBuffer(skyVertices);
        mMainSailVerticesBuffer = createNativeFloatBuffer(mainSailVertices);
        mSmallSailVerticesBuffer = createNativeFloatBuffer(smallSailVertices);
        mBoatVerticesBuffer = createNativeFloatBuffer(boatVertices);
    }

    private void setupNormalsBuffer() {
        //вектор нормали перпендикулярен плоскости квадрата
        //и направлен вдоль оси Z
        float nx = 0;
        float ny = 0;
        float nz = 1;
        //нормаль одинакова для всех вершин квадрата,
        //поэтому переписываем координаты вектора нормали в массив 4 раза
        float normalArray[] = {
                nx, ny, nz,
                nx, ny, nz,
                nx, ny, nz,
                nx, ny, nz
        };
        mVerticesNormalsBuffer = createNativeFloatBuffer(normalArray);
    }

    private void setupVerticesColorBuffers() {
        // R-G-B-A
        float[] seaVerticesColors = {
                0f, 1f, 1f, 1,
                0f, 0f, 1f, 1,
                0f, 1f, 1f, 1,
                0f, 0f, 1f, 1,
        };
        float[] skyVerticesColors = {
                0.2f, 0.2f, 0.8f, 1,
                0.5f, 0.5f, 1, 1,
                0.2f, 0.2f, 0.8f, 1,
                0.5f, 0.5f, 1, 1,
        };
        float[] sailVerticesColors = {
                1, 0.1f, 0.1f, 1,
                1, 1, 1, 1,
                1, 0.1f, 0.1f, 1,
        };
        float[] boatVerticesColors = {
                1, 1, 1, 1,
                0.2f, 0.2f, 0.2f, 1,
                1, 1, 1, 1,
                0.2f, 0.2f, 0.2f, 1,
        };
        mSeaVerticesColorsBuffer = createNativeFloatBuffer(seaVerticesColors);
        mSkyVerticesColorsBuffer = createNativeFloatBuffer(skyVerticesColors);
        mAnySailVerticesColorsBuffer = createNativeFloatBuffer(sailVerticesColors);
        mBoatVerticesColorsBuffer = createNativeFloatBuffer(boatVerticesColors);
    }

    private static FloatBuffer createNativeFloatBuffer(float[] withArray) {
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

    /**
     * Here we are going to setup Projection and Model-View-Projection matrices.
     */
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // устанавливаем glViewport
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        float k = 0.047f;
        float left = -k * ratio;
        float right = k * ratio;
        float bottom = -k;
        float top = k;
        float near = 0.1f;
        float far = 10.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
        Matrix.multiplyMM(modelViewProjectionMatrix,
                0, projectionMatrix,
                0, modelViewMatrix,
                0);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //включаем тест глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //включаем отсечение невидимых граней
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        //включаем сглаживание текстур, это пригодится в будущем
        GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);

        mSeaShader = createShadingProgram();
        mSkyShader = createShadingProgram();
        mMainSailShader = createShadingProgram();
        mSmallSailShader = createShadingProgram();
        mBoatShader = createShadingProgram();

        try {
            mSeaShader.setup();
            mSkyShader.setup();
            mMainSailShader.setup();
            mSmallSailShader.setup();
            mBoatShader.setup();
        } catch (GlLibException e) {
            throw new RuntimeException(e);
        }


    }

    private static ShadingProgram createShadingProgram() {
        return new ShadingProgram(Arrays.asList(createVertexShader(), createFragmentShader()));
    }

    private static Shader createVertexShader() {
        return Shader.fromSourceCode(Shader.ShaderType.VERTEX, VERTEX_SHADER_CODE);
    }

    private static Shader createFragmentShader() {
        return Shader.fromSourceCode(Shader.ShaderType.FRAGMENT, FRAGMENT_SHADER_CODE);
    }

    /**
     * Frame rendering
     */
    public void onDrawFrame(GL10 unused) {
        // clean frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Render Sea
        linkAttributesAndUniforms(mSeaShader,
                mSeaVerticesBuffer, mVerticesNormalsBuffer, mSeaVerticesColorsBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, BYTES_PER_FLOAT);

        // Render Sky
        linkAttributesAndUniforms(mSkyShader,
                mSkyVerticesBuffer, mVerticesNormalsBuffer, mSkyVerticesColorsBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, BYTES_PER_FLOAT);

        // Render MainSail
        linkAttributesAndUniforms(mMainSailShader,
                mMainSailVerticesBuffer, mVerticesNormalsBuffer, mAnySailVerticesColorsBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        // Render SmallSail
        linkAttributesAndUniforms(mSmallSailShader,
                mSmallSailVerticesBuffer, mVerticesNormalsBuffer, mAnySailVerticesColorsBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        // Render Boat
        linkAttributesAndUniforms(mBoatShader,
                mBoatVerticesBuffer, mVerticesNormalsBuffer, mBoatVerticesColorsBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, BYTES_PER_FLOAT);
    }

    private void linkAttributesAndUniforms(ShadingProgram shadingProgram,
                                           FloatBuffer verticesBuffer,
                                           FloatBuffer verticesNormalsBuffer,
                                           FloatBuffer verticesColorsBuffer) {
        shadingProgram.useProgram();

        shadingProgram.linkVertexBuffer(verticesBuffer);
        shadingProgram.linkNormalBuffer(verticesNormalsBuffer);
        shadingProgram.linkColorBuffer(verticesColorsBuffer);

        shadingProgram.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        shadingProgram.linkCamera(xСameraPosition, yCameraPosition, zCameraPosition);
        shadingProgram.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
    }

}