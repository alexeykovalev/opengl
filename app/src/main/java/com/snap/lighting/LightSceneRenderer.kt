package com.snap.lighting

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix


import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import com.snap.model.bytesPerFloat
import com.snap.model.createNativeFloatBuffer

class LightSceneRenderer : GLSurfaceView.Renderer {

    private var xСameraPosition: Float = 0.toFloat()
    private var yCameraPosition: Float = 0.toFloat()
    private var zCameraPosition: Float = 0.toFloat()

    private var xLightPosition: Float = 0.toFloat()
    private var yLightPosition: Float = 0.toFloat()
    private var zLightPosition: Float = 0.toFloat()

    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private lateinit var seaVerticesBuffer: FloatBuffer
    private lateinit var skyVerticesBuffer: FloatBuffer
    private lateinit var mainSailVerticesBuffer: FloatBuffer
    private lateinit var smallSailVerticesBuffer: FloatBuffer
    private lateinit var boatVerticesBuffer: FloatBuffer

    private lateinit var verticesNormalsBuffer: FloatBuffer

    private lateinit var seaVerticesColorsBuffer: FloatBuffer
    private lateinit var skyVerticesColorsBuffer: FloatBuffer
    private lateinit var anySailVerticesColorsBuffer: FloatBuffer
    private lateinit var boatVerticesColorsBuffer: FloatBuffer

    private lateinit var seaShader: LightSceneShadingProgram
    private lateinit var skyShader: LightSceneShadingProgram
    private lateinit var mainSailShader: LightSceneShadingProgram
    private lateinit var smallSailShader: LightSceneShadingProgram
    private lateinit var boatShader: LightSceneShadingProgram

    init {
        setup()
    }

    private fun setup() {
        setupLightSource()
        setupModelViewMatrix()
        setupVertexBuffers()
        setupNormalsBuffer()
        setupVerticesColorBuffers()
    }

    /**
     * Point source of light.
     */
    private fun setupLightSource() {
        xLightPosition = 0.5f
        yLightPosition = 0.2f
        zLightPosition = 0.5f
    }

    private fun setupModelViewMatrix() {
        //мы не будем двигать объекты поэтому сбрасываем модельную матрицу на единичную
        Matrix.setIdentityM(modelMatrix, 0)

        //координаты камеры
        xСameraPosition = 0.0f
        yCameraPosition = 0.0f
        zCameraPosition = 3.0f

        // пусть камера смотрит на начало координат
        // и верх у камеры будет вдоль оси Y
        // зная координаты камеры получаем матрицу вида
        Matrix.setLookAtM(
                viewMatrix, 0, xСameraPosition, yCameraPosition, zCameraPosition,
                0f, 0f, 0f, 0f, 1f, 0f)
        // умножая матрицу вида на матрицу модели
        // получаем матрицу модели-вида
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    }

    private fun setupVertexBuffers() {
        val seaVertices = floatArrayOf(-1.0f, -0.35f, 0.0f, -1.0f, -1.5f, 0.0f, 1.0f, -0.35f, 0.0f, 1.0f, -1.5f, 0.0f)
        val skyVertices = floatArrayOf(-1.0f, 1.5f, 0.0f, -1.0f, -0.35f, 0.0f, 1.0f, 1.5f, 0.0f, 1.0f, -0.35f, 0f)
        val mainSailVertices = floatArrayOf(-0.5f, -0.45f, 0.4f, 0.0f, -0.45f, 0.4f, 0.0f, 0.5f, 0.4f)
        val smallSailVertices = floatArrayOf(0.05f, -0.45f, 0.4f, 0.22f, -0.5f, 0.4f, 0.0f, 0.25f, 0.4f)
        val boatVertices = floatArrayOf(-0.5f, -0.5f, 0.4f, -0.5f, -0.6f, 0.4f, 0.22f, -0.5f, 0.4f, 0.18f, -0.6f, 0.4f)

        seaVerticesBuffer = createNativeFloatBuffer(seaVertices)
        skyVerticesBuffer = createNativeFloatBuffer(skyVertices)
        mainSailVerticesBuffer = createNativeFloatBuffer(mainSailVertices)
        smallSailVerticesBuffer = createNativeFloatBuffer(smallSailVertices)
        boatVerticesBuffer = createNativeFloatBuffer(boatVertices)
    }

    private fun setupNormalsBuffer() {
        //вектор нормали перпендикулярен плоскости квадрата
        //и направлен вдоль оси Z
        val nx = 0f
        val ny = 0f
        val nz = 1f
        //нормаль одинакова для всех вершин квадрата,
        //поэтому переписываем координаты вектора нормали в массив 4 раза
        val normalArray = floatArrayOf(nx, ny, nz, nx, ny, nz, nx, ny, nz, nx, ny, nz)
        verticesNormalsBuffer = createNativeFloatBuffer(normalArray)
    }

    private fun setupVerticesColorBuffers() {
        // R-G-B-A
        val seaVerticesColors = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f)
        val skyVerticesColors = floatArrayOf(0.2f, 0.2f, 0.8f, 1f, 0.5f, 0.5f, 1f, 1f, 0.2f, 0.2f, 0.8f, 1f, 0.5f, 0.5f, 1f, 1f)
        val sailVerticesColors = floatArrayOf(1f, 0.1f, 0.1f, 1f, 1f, 1f, 1f, 1f, 1f, 0.1f, 0.1f, 1f)
        val boatVerticesColors = floatArrayOf(1f, 1f, 1f, 1f, 0.2f, 0.2f, 0.2f, 1f, 1f, 1f, 1f, 1f, 0.2f, 0.2f, 0.2f, 1f)
        seaVerticesColorsBuffer = createNativeFloatBuffer(seaVerticesColors)
        skyVerticesColorsBuffer = createNativeFloatBuffer(skyVerticesColors)
        anySailVerticesColorsBuffer = createNativeFloatBuffer(sailVerticesColors)
        boatVerticesColorsBuffer = createNativeFloatBuffer(boatVerticesColors)
    }

    /**
     * Here we are going to createAndCompile Projection and Model-View-Projection matrices.
     */
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        // устанавливаем glViewport
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height
        val k = 0.047f
        val left = -k * ratio
        val right = k * ratio
        val bottom = -k
        val near = 0.1f
        val far = 10.0f

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, k, near, far)
        Matrix.multiplyMM(modelViewProjectionMatrix,
                0, projectionMatrix,
                0, modelViewMatrix,
                0)
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        //включаем тест глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        //включаем отсечение невидимых граней
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        //включаем сглаживание текстур, это пригодится в будущем
        GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST)

        seaShader = LightSceneShadingProgram.newInstance()
        skyShader = LightSceneShadingProgram.newInstance()
        mainSailShader = LightSceneShadingProgram.newInstance()
        smallSailShader = LightSceneShadingProgram.newInstance()
        boatShader = LightSceneShadingProgram.newInstance()

        listOf(seaShader, skyShader, mainSailShader, smallSailShader, boatShader).forEach { it.setup() }
    }

    /**
     * Frame rendering
     */
    override fun onDrawFrame(unused: GL10) {
        // clean frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Render Sea
        linkAttributesAndUniforms(seaShader,
                seaVerticesBuffer, verticesNormalsBuffer, seaVerticesColorsBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, bytesPerFloat)

        // Render Sky
        linkAttributesAndUniforms(skyShader,
                skyVerticesBuffer, verticesNormalsBuffer, skyVerticesColorsBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, bytesPerFloat)

        // Render MainSail
        linkAttributesAndUniforms(mainSailShader,
                mainSailVerticesBuffer, verticesNormalsBuffer, anySailVerticesColorsBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

        // Render SmallSail
        linkAttributesAndUniforms(smallSailShader,
                smallSailVerticesBuffer, verticesNormalsBuffer, anySailVerticesColorsBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

        // Render Boat
        linkAttributesAndUniforms(boatShader,
                boatVerticesBuffer, verticesNormalsBuffer, boatVerticesColorsBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, bytesPerFloat)
    }

    private fun linkAttributesAndUniforms(shadingProgram: LightSceneShadingProgram,
                                          verticesBuffer: FloatBuffer,
                                          verticesNormalsBuffer: FloatBuffer,
                                          verticesColorsBuffer: FloatBuffer) {

        shadingProgram.linkVertexBuffer(verticesBuffer)
        shadingProgram.linkNormalBuffer(verticesNormalsBuffer)
        shadingProgram.linkColorBuffer(verticesColorsBuffer)

        shadingProgram.linkModelViewProjectionMatrix(modelViewProjectionMatrix)
        shadingProgram.linkCamera(xСameraPosition, yCameraPosition, zCameraPosition)
        shadingProgram.linkLightSource(xLightPosition, yLightPosition, zLightPosition)
    }

}