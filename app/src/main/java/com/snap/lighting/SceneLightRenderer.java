package com.snap.lighting;

//import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

public class SceneLightRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;

    //координаты камеры
    private float xСamera, yCamera, zCamera;

    //координаты источника света
    private float xLightPosition, yLightPosition, zLightPosition;

    //матрицы
    private float[] modelMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] modelViewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] modelViewProjectionMatrix = new float[16];

    private FloatBuffer mSeaVerticesBuffer;
    private FloatBuffer mSkyVerticesBuffer;
    private FloatBuffer mMainSailVerticesBuffer;
    private FloatBuffer mSmallSailVerticesBuffer;
    private FloatBuffer mBoatVerticesBuffer;

    //буфер для нормалей вершин
    private FloatBuffer verticesNormalsBuffer;

    //буфер для цветов вершин
    private FloatBuffer colorBuffer;
    private FloatBuffer colorBuffer1;
    private FloatBuffer colorBuffer2;
    private FloatBuffer colorBuffer3;

    //шейдерный объект
    private Shader mShader;
    private Shader mShader1;
    private Shader mShader2;
    private Shader mShader3;
    private Shader mShader4;


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
        xLightPosition = 0.3f;
        yLightPosition = 0.2f;
        zLightPosition = 0.5f;
    }

    private void setupModelViewMatrix() {
        //мы не будем двигать объекты поэтому сбрасываем модельную матрицу на единичную
        Matrix.setIdentityM(modelMatrix, 0);

        //координаты камеры
        xСamera = 0.0f;
        yCamera = 0.0f;
        zCamera = 3.0f;

        // пусть камера смотрит на начало координат
        // и верх у камеры будет вдоль оси Y
        // зная координаты камеры получаем матрицу вида
        Matrix.setLookAtM(
                viewMatrix, 0, xСamera, yCamera, zCamera,
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
        verticesNormalsBuffer = createNativeFloatBuffer(normalArray);
    }

    private void setupVerticesColorBuffers() {
        // square's vertices colors
        // R-G-B-A
        float colorArray[] = {
                0f, 1f, 1f, 1,
                0f, 0f, 1f, 1,
                0f, 1f, 1f, 1,
                0f, 0f, 1f, 1,
        };
        float colorArray1[] = {
                0.2f, 0.2f, 0.8f, 1,
                0.5f, 0.5f, 1, 1,
                0.2f, 0.2f, 0.8f, 1,
                0.5f, 0.5f, 1, 1,
        };
        float colorArray2[] = {
                1, 0.1f, 0.1f, 1,
                1, 1, 1, 1,
                1, 0.1f, 0.1f, 1,
        };

        float colorArray4[] = {
                1, 1, 1, 1,
                0.2f, 0.2f, 0.2f, 1,
                1, 1, 1, 1,
                0.2f, 0.2f, 0.2f, 1,
        };
        colorBuffer = createNativeFloatBuffer(colorArray);
        colorBuffer1 = createNativeFloatBuffer(colorArray1);
        colorBuffer2 = createNativeFloatBuffer(colorArray2);
        colorBuffer3 = createNativeFloatBuffer(colorArray4);
    }

    private static FloatBuffer createNativeFloatBuffer(float[] withArray) {
        if (withArray == null || withArray.length == 0) {
            throw new IllegalArgumentException("Array has to be not empty.");
        }
        final ByteBuffer tmpBuffer = ByteBuffer.allocateDirect(withArray.length * BYTES_PER_FLOAT);
        tmpBuffer.order(ByteOrder.nativeOrder());
        final FloatBuffer result = tmpBuffer.asFloatBuffer();
        result.position(0);
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

        final String vertexShaderCode =
                "uniform mat4 u_modelViewProjectionMatrix;" +
                        "attribute vec3 a_vertex;" +
                        "attribute vec3 a_normal;" +
                        "attribute vec4 a_color;" +
                        "varying vec3 v_vertex;" +
                        "varying vec3 v_normal;" +
                        "varying vec4 v_color;" +
                        "void main() {" +
                        "v_vertex=a_vertex;" +
                        "vec3 n_normal=normalize(a_normal);" +
                        "v_normal=n_normal;" +
                        "v_color=a_color;" +
                        "gl_Position = u_modelViewProjectionMatrix * vec4(a_vertex,1.0);" +
                        "}";

        final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec3 u_camera;" +
                        "uniform vec3 u_lightPosition;" +
                        "varying vec3 v_vertex;" +
                        "varying vec3 v_normal;" +
                        "varying vec4 v_color;" +
                        "void main() {" +
                        "vec3 n_normal=normalize(v_normal);" +
                        "vec3 lightvector = normalize(u_lightPosition - v_vertex);" +
                        "vec3 lookvector = normalize(u_camera - v_vertex);" +
                        "float ambient=0.2;" +
                        "float k_diffuse=0.3;" +
                        "float k_specular=0.5;" +
                        "float diffuse = k_diffuse * max(dot(n_normal, lightvector), 0.0);" +
                        "vec3 reflectvector = reflect(-lightvector, n_normal);" +
                        "float specular = k_specular * pow( max(dot(lookvector,reflectvector),0.0), 40.0 );" +
                        "vec4 one=vec4(1.0,1.0,1.0,1.0);" +
                        "vec4 lightColor = (ambient+diffuse+specular)*one;" +
                        "gl_FragColor = mix(lightColor,v_color,0.6);" +
                        "}";
        //создадим шейдерный объект
        mShader = new Shader(vertexShaderCode, fragmentShaderCode);
        //свяжем буфер вершин с атрибутом a_vertex в вершинном шейдере
        mShader.linkVertexBuffer(mSeaVerticesBuffer);
        //свяжем буфер нормалей с атрибутом a_normal в вершинном шейдере
        mShader.linkNormalBuffer(verticesNormalsBuffer);
        //свяжем буфер цветов с атрибутом a_color в вершинном шейдере
        mShader.linkColorBuffer(colorBuffer);
        //связь атрибутов с буферами сохраняется до тех пор,
        //пока не будет уничтожен шейдерный объект

        mShader1 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader1.linkVertexBuffer(mSkyVerticesBuffer);
        mShader1.linkNormalBuffer(verticesNormalsBuffer);
        mShader1.linkColorBuffer(colorBuffer1);

        mShader2 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader2.linkVertexBuffer(mMainSailVerticesBuffer);
        mShader2.linkNormalBuffer(verticesNormalsBuffer);
        mShader2.linkColorBuffer(colorBuffer2);

        mShader3 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader3.linkVertexBuffer(mSmallSailVerticesBuffer);
        mShader3.linkNormalBuffer(verticesNormalsBuffer);
        mShader3.linkColorBuffer(colorBuffer2);

        mShader4 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader4.linkVertexBuffer(mBoatVerticesBuffer);
        mShader4.linkNormalBuffer(verticesNormalsBuffer);
        mShader4.linkColorBuffer(colorBuffer3);

    }

    //метод, в котором выполняется рисование кадра
    public void onDrawFrame(GL10 unused) {
        //очищаем кадр
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //передаем в шейдерный объект матрицу модели-вида-проекции
        mShader.useProgram();
        mShader.linkVertexBuffer(mSeaVerticesBuffer);
        mShader.linkColorBuffer(colorBuffer);
        mShader.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader.linkCamera(xСamera, yCamera, zCamera);
        mShader.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, BYTES_PER_FLOAT);

        mShader1.useProgram();
        mShader1.linkVertexBuffer(mSkyVerticesBuffer);
        mShader1.linkColorBuffer(colorBuffer1);
        mShader1.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader1.linkCamera(xСamera, yCamera, zCamera);
        mShader1.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, BYTES_PER_FLOAT);

        mShader2.useProgram();
        mShader2.linkVertexBuffer(mMainSailVerticesBuffer);
        mShader2.linkColorBuffer(colorBuffer2);
        mShader2.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader2.linkCamera(xСamera, yCamera, zCamera);
        mShader2.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        mShader3.useProgram();
        mShader3.linkVertexBuffer(mSmallSailVerticesBuffer);
        mShader3.linkColorBuffer(colorBuffer2);
        mShader3.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader3.linkCamera(xСamera, yCamera, zCamera);
        mShader3.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        mShader4.useProgram();
        mShader4.linkVertexBuffer(mBoatVerticesBuffer);
        mShader4.linkColorBuffer(colorBuffer3);
        mShader4.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader4.linkCamera(xСamera, yCamera, zCamera);
        mShader4.linkLightSource(xLightPosition, yLightPosition, zLightPosition);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, BYTES_PER_FLOAT);
    }

}