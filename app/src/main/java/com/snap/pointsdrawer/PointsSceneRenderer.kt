package com.snap.pointsdrawer

import android.opengl.GLSurfaceView
import android.opengl.Matrix

import com.snap.model.exception.GlLibException
import com.snap.model.shading.Shader
import com.snap.model.shading.ShadingProgram

import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glViewport
import com.snap.model.createNativeFloatBuffer
import com.snap.model.shading.ShaderType

class PointsSceneRenderer : GLSurfaceView.Renderer {

    private lateinit var shadingProgram: ShadingProgram

    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var colorsBuffer: FloatBuffer

    private val mProjectionMatrix = FloatArray(16)


    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        glClearColor(0f, 0f, 0f, 1f)
        glEnable(GL_DEPTH_TEST)
        setupShadingProgram()
        setupVerticesBuffer()
        setupColorsBuffer()
    }

    private fun setupVerticesBuffer() {
        val z1 = -1.0f
        val z2 = -1.0f

        val vertices = floatArrayOf(-0.7f, -0.5f, z1, 0.3f, -0.5f, z1, -0.2f, 0.3f, z1,

                -0.3f, -0.4f, z2, 0.7f, -0.4f, z2, 0.2f, 0.4f, z2)
        verticesBuffer = createNativeFloatBuffer(vertices)
    }

    private fun setupColorsBuffer() {
        val colors = floatArrayOf(
                // R G B A
                0.0f, 1.0f, 0.0f, 1f, 0.0f, 1.0f, 0.0f, 1f, 0.0f, 1.0f, 0.0f, 1f,

                0.0f, 0.0f, 1.0f, 1f, 0.0f, 0.0f, 1.0f, 1f, 0.0f, 0.0f, 1.0f, 1f)
        colorsBuffer = createNativeFloatBuffer(colors)
    }

    private fun setupShadingProgram() {
        val vertexShader = Shader.fromSourceCode(ShaderType.VERTEX, VERTEX_SHADER_CODE)
        val fragmentShader = Shader.fromSourceCode(ShaderType.FRAGMENT, FRAGMENT_SHADER_CODE)
        shadingProgram = ShadingProgram(vertexShader, fragmentShader)
        try {
            shadingProgram.setup()
        } catch (e: GlLibException) {
            throw RuntimeException("Unable to createAndCompile shading program", e)
        }

    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        bindProjectionMatrix(width.toFloat(), height.toFloat())
    }

    override fun onDrawFrame(gl: GL10) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        bindVertices(verticesBuffer)
        bindColors(colorsBuffer)

        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    private fun bindVertices(verticesData: FloatBuffer?) {
        shadingProgram.doUsingProgram {
            createAttributeBinding("a_Position") {
                bindFloatBuffer(verticesData!!, 3, false, 0)
            }
        }
    }

    private fun bindColors(colorsBuffer: FloatBuffer?) {
        shadingProgram.doUsingProgram {
            createAttributeBinding("a_Color") {
                bindFloatBuffer(colorsBuffer!!, 4, false, 0)
            }
        }
    }


    private fun bindProjectionMatrix(width: Float, height: Float) {
        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 1.0f
        val far = 8.0f

        if (width > height) {
            val screenRatio = width / height
            left *= screenRatio
            right *= screenRatio
        } else {
            val screenRatio = height / width
            bottom *= screenRatio
            top *= screenRatio
        }

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)

        shadingProgram.doUsingProgram {
            createUniformBinding("u_Matrix") {
                bindUniformMatrix4fv(mProjectionMatrix)
            }
        }
    }

    companion object {

        // attribute - receives data per each vertex
        // uniform - like static - all vertices shares this data
        // varying - for passing data between Vertex/Fragment shaders
        private val VERTEX_SHADER_CODE = "attribute vec4 a_Position;" +
                "attribute vec4 a_Color;" +
                "uniform mat4 u_Matrix;" +
                "varying vec4 v_Color;" +

                "void main() {" +
                "   gl_Position = u_Matrix * a_Position;;" +
                "   v_Color = a_Color;" +
                "}"

        private val FRAGMENT_SHADER_CODE = "precision mediump float;" +
                "varying vec4 v_Color;" +
                "void main() {" +
                "   gl_FragColor = v_Color;" +
                "}"
    }

}
