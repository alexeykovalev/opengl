package com.snap.lighting;

import android.content.Context;
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

    private Context context;

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
    private FloatBuffer normalBuffer;

    //буфер для цветов вершин
    private final FloatBuffer colorBuffer;
    private final FloatBuffer colorBuffer1;
    private final FloatBuffer colorBuffer2;
    private final FloatBuffer colorBuffer4;

    //шейдерный объект
    private Shader mShader;
    private Shader mShader1;
    private Shader mShader2;
    private Shader mShader3;
    private Shader mShader4;


    public SceneLightRenderer(Context context) {
        // запомним контекст
        // он нам понадобится в будущем для загрузки текстур
        this.context = context;

        //координаты точечного источника света
        xLightPosition = 0.3f;
        yLightPosition = 0.2f;
        zLightPosition = 0.5f;

        setupModelViewMatrix();

        setupVertexBuffers();

        //вектор нормали перпендикулярен плоскости квадрата
        //и направлен вдоль оси Z
        float nx = 0;
        float ny = 0;
        float nz = 1;
        //нормаль одинакова для всех вершин квадрата,
        //поэтому переписываем координаты вектора нормали в массив 4 раза
        float normalArray[] = {nx, ny, nz, nx, ny, nz, nx, ny, nz, nx, ny, nz};
        float normalArray1[] = {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1};
        //создадим буфер для хранения координат векторов нормали
        ByteBuffer bnormal = ByteBuffer.allocateDirect(normalArray.length * 4);
        bnormal.order(ByteOrder.nativeOrder());
        normalBuffer = bnormal.asFloatBuffer();
        normalBuffer.position(0);

        //перепишем координаты нормалей из массива в буфер
        normalBuffer.put(normalArray);
        normalBuffer.position(0);

        //разукрасим вершины квадрата, зададим цвета для вершин
        float red1 = 0;
        float green1 = 1;
        float blue1 = 1;
        //цвет второй вершины
        float red2 = 0;
        float green2 = 0;
        float blue2 = 1;
        //цвет третьей вершины
        float red3 = 0;
        float green3 = 1;
        float blue3 = 1;
        //цвет четвертой вершины
        float red4 = 0;
        float green4 = 0;
        float blue4 = 1;
        //перепишем цвета вершин в массив
        //четвертый компонент цвета (альфу) примем равным единице
        float colorArray[] = {
                red1, green1, blue1, 1,
                red2, green2, blue2, 1,
                red3, green3, blue3, 1,
                red4, green4, blue4, 1,
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

        //создадим буфер для хранения цветов вершин
        ByteBuffer bcolor = ByteBuffer.allocateDirect(colorArray.length * 4);
        bcolor.order(ByteOrder.nativeOrder());
        colorBuffer = bcolor.asFloatBuffer();
        colorBuffer.position(0);
        //перепишем цвета вершин из массива в буфер
        colorBuffer.put(colorArray);
        colorBuffer.position(0);

        ByteBuffer bcolor1 = ByteBuffer.allocateDirect(colorArray1.length * 4);
        bcolor1.order(ByteOrder.nativeOrder());
        colorBuffer1 = bcolor1.asFloatBuffer();
        colorBuffer1.position(0);
        colorBuffer1.put(colorArray1);
        colorBuffer1.position(0);

        ByteBuffer bcolor2 = ByteBuffer.allocateDirect(colorArray1.length * 4);
        bcolor2.order(ByteOrder.nativeOrder());
        colorBuffer2 = bcolor2.asFloatBuffer();
        colorBuffer2.position(0);
        colorBuffer2.put(colorArray2);
        colorBuffer2.position(0);

        ByteBuffer bcolor4 = ByteBuffer.allocateDirect(colorArray4.length * 4);
        bcolor4.order(ByteOrder.nativeOrder());
        colorBuffer4 = bcolor4.asFloatBuffer();
        colorBuffer4.position(0);
        colorBuffer4.put(colorArray4);
        colorBuffer4.position(0);

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
        float seaVertices[] = {
                -1.0f, -0.35f, 0.0f,
                -1.0f, -1.5f, 0.0f,
                1.0f, -0.35f, 0.0f,
                1.0f, -1.5f, 0.0f
        };
        float skyVertices[] = {
                -1.0f, 1.5f, 0.0f,
                -1.0f, -0.35f, 0.0f,
                1.0f, 1.5f, 0.0f,
                1.0f, -0.35f, 0
        };
        float mainSailVertices[] = {
                -0.5f, -0.45f, 0.4f,
                0.0f, -0.45f, 0.4f,
                0.0f, 0.5f, 0.4f
        };
        float smallSailVertices[] = {
                0.05f, -0.45f, 0.4f,
                0.22f, -0.5f, 0.4f,
                0.0f, 0.25f, 0.4f
        };
        float boatVertices[] = {
                -0.5f, -0.5f, 0.4f,
                -0.5f, -0.6f, 0.4f,
                0.22f, -0.5f, 0.4f,
                0.18f, -0.6f, 0.4f
        };

        //создадим буфер для хранения координат вершин
        ByteBuffer seaVerticesBuffer = ByteBuffer.allocateDirect(seaVertices.length * 4);
        seaVerticesBuffer.order(ByteOrder.nativeOrder());
        mSeaVerticesBuffer = seaVerticesBuffer.asFloatBuffer();
        mSeaVerticesBuffer.position(0);

        ByteBuffer skyVerticesBuffer = ByteBuffer.allocateDirect(skyVertices.length * 4);
        skyVerticesBuffer.order(ByteOrder.nativeOrder());
        mSkyVerticesBuffer = skyVerticesBuffer.asFloatBuffer();
        mSkyVerticesBuffer.position(0);

        ByteBuffer mainSailVerticesBuffer = ByteBuffer.allocateDirect(mainSailVertices.length * 4);
        mainSailVerticesBuffer.order(ByteOrder.nativeOrder());
        mMainSailVerticesBuffer = mainSailVerticesBuffer.asFloatBuffer();
        mMainSailVerticesBuffer.position(0);

        ByteBuffer smallSailVerticesBuffer = ByteBuffer.allocateDirect(smallSailVertices.length * 4);
        smallSailVerticesBuffer.order(ByteOrder.nativeOrder());
        this.mSmallSailVerticesBuffer = smallSailVerticesBuffer.asFloatBuffer();
        this.mSmallSailVerticesBuffer.position(0);

        ByteBuffer boatVerticesBuffer = ByteBuffer.allocateDirect(boatVertices.length * 4);
        boatVerticesBuffer.order(ByteOrder.nativeOrder());
        mBoatVerticesBuffer = boatVerticesBuffer.asFloatBuffer();
        mBoatVerticesBuffer.position(0);

        mSeaVerticesBuffer.put(seaVertices);
        mSeaVerticesBuffer.position(0);

        mSkyVerticesBuffer.put(skyVertices);
        mSkyVerticesBuffer.position(0);

        mMainSailVerticesBuffer.put(mainSailVertices);
        mMainSailVerticesBuffer.position(0);

        mSmallSailVerticesBuffer.put(smallSailVertices);
        mSmallSailVerticesBuffer.position(0);

        mBoatVerticesBuffer.put(boatVertices);
        mBoatVerticesBuffer.position(0);
    }

    //метод, который срабатывает при изменении размеров экрана
    //в нем мы получим матрицу проекции и матрицу модели-вида-проекции
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // устанавливаем glViewport
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float k = 0.055f;
        float left = -k * ratio;
        float right = k * ratio;
        float bottom = -k;
        float top = k;
        float near = 0.1f;
        float far = 10.0f;
        // получаем матрицу проекции
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
        // матрица проекции изменилась,
        // поэтому нужно пересчитать матрицу модели-вида-проекции
        Matrix.multiplyMM(
                modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    //метод, который срабатывает при создании экрана
    //здесь мы создаем шейдерный объект
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //включаем тест глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //включаем отсечение невидимых граней
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        //включаем сглаживание текстур, это пригодится в будущем
        GLES20.glHint(
                GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
        //записываем код вершинного шейдера в виде строки
        String vertexShaderCode =
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
        //записываем код фрагментного шейдера в виде строки
        String fragmentShaderCode =
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
        mShader.linkNormalBuffer(normalBuffer);
        //свяжем буфер цветов с атрибутом a_color в вершинном шейдере
        mShader.linkColorBuffer(colorBuffer);
        //связь атрибутов с буферами сохраняется до тех пор,
        //пока не будет уничтожен шейдерный объект

        mShader1 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader1.linkVertexBuffer(mSkyVerticesBuffer);
        mShader1.linkNormalBuffer(normalBuffer);
        mShader1.linkColorBuffer(colorBuffer1);

        mShader2 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader2.linkVertexBuffer(mMainSailVerticesBuffer);
        mShader2.linkNormalBuffer(normalBuffer);
        mShader2.linkColorBuffer(colorBuffer2);

        mShader3 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader3.linkVertexBuffer(mSmallSailVerticesBuffer);
        mShader3.linkNormalBuffer(normalBuffer);
        mShader3.linkColorBuffer(colorBuffer2);

        mShader4 = new Shader(vertexShaderCode, fragmentShaderCode);
        mShader4.linkVertexBuffer(mBoatVerticesBuffer);
        mShader4.linkNormalBuffer(normalBuffer);
        mShader4.linkColorBuffer(colorBuffer4);

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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        mShader1.useProgram();
        mShader1.linkVertexBuffer(mSkyVerticesBuffer);
        mShader1.linkColorBuffer(colorBuffer1);
        mShader1.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader1.linkCamera(xСamera, yCamera, zCamera);
        mShader1.linkLightSource(xLightPosition, yLightPosition, zLightPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

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
        mShader4.linkColorBuffer(colorBuffer4);
        mShader4.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader4.linkCamera(xСamera, yCamera, zCamera);
        mShader4.linkLightSource(xLightPosition, yLightPosition, zLightPosition);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

}