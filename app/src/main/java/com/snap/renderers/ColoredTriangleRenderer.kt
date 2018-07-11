package com.snap.renderers

import android.opengl.GLSurfaceView

import com.snap.model.exception.GlLibException
import com.snap.model.shading.Shader
import com.snap.model.shading.ShadingProgram

import java.nio.FloatBuffer
import java.util.ArrayList

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glViewport
import com.snap.model.shading.ShaderType
import com.snap.model.toNativeOrderBuffer

class ColoredTriangleRenderer : GLSurfaceView.Renderer {

    private val triangles = ArrayList<FloatBuffer>()
    private lateinit var shadingProgram: ShadingProgram

    override fun onSurfaceCreated(ignore: GL10, config: EGLConfig) {
        glClearColor(0f, 0f, 0f, 1f)
        setupVertices()
        setupShadingProgram()
    }

    private fun setupVertices() {
        val firstTriangle = floatArrayOf(-0.5f, -0.2f, 0f, 0f, 0.2f, 0f, 0.5f, -0.2f, 0f)
        val secondTriangle = floatArrayOf(-0.5f, -0.2f, 0f, 0.5f, -0.2f, 0f, 0f, -0.6f, 0f)
        triangles.add(firstTriangle.toNativeOrderBuffer())
        //        triangles.add(GlHelpers.createNativeOrderFloatBuffer(secondTriangle));
    }

    private fun setupShadingProgram() {
        val vertexShader = Shader.fromSourceCode(ShaderType.VERTEX, vertexShaderCode)
        val fragmentShader = Shader.fromSourceCode(ShaderType.FRAGMENT, fragmentShaderCode)
        shadingProgram = ShadingProgram(vertexShader, fragmentShader)
        try {
            shadingProgram.setup()
        } catch (e: GlLibException) {
            throw RuntimeException("Unable to createAndCompile shading program", e)
        }

    }

    private fun bindData(triangleVertices: FloatBuffer) {
        shadingProgram.doUsingProgram {
            createAttributeBinding("a_Position") {
                bindFloatBuffer(triangleVertices, 3)
            }
            createUniformBinding("u_Color") {
                bindUniform4f(0f, 0f, 1f, 1f)
            }
        }
    }

    override fun onSurfaceChanged(ignore: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(ignore: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)
        triangles.forEach { drawTriangle(it) }

    }

    private fun drawTriangle(triangleVertices: FloatBuffer) {
        bindData(triangleVertices)
        glDrawArrays(GL_TRIANGLES, 0, 3)
    }
}

private const val vertexShaderCode = "attribute vec4 a_Position;" +
        "void main() {" +
        "gl_Position = a_Position;" +
        "}"

private const val fragmentShaderCode = "precision mediump float;" +
        "uniform vec4 u_Color;" +
        "void main() {" +
        "gl_FragColor = u_Color;" +
        "}"
