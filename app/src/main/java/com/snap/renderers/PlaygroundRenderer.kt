package com.snap.renderers

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.snap.model.shading.Shader
import com.snap.model.shading.ShaderType
import com.snap.model.shading.ShadingProgram
import com.snap.model.toNativeOrderBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class PlaygroundRenderer : GLSurfaceView.Renderer {

    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var shadingProgram: ShadingProgram

    private val modelViewProjectionMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glEnable(GL_DEPTH_TEST)

        val s = 0.4f
        val d = 0.9f
        val l = 3f

        verticesBuffer = floatArrayOf(
                // первый треугольник
                -2 * s, -s, d,
                2 * s, -s, d,
                0f, s, d,

                // второй треугольник
                -2 * s, -s, -d,
                2 * s, -s, -d,
                0f, s, -d,

                // третий треугольник
                d, -s, -2 * s,
                d, -s, 2 * s,
                d, s, 0f,

                // четвертый треугольник
                -d, -s, -2 * s,
                -d, -s, 2 * s,
                -d, s, 0f,

                // ось X
                -l, 0f, 0f,
                l, 0f, 0f,

                // ось Y
                0f, -l, 0f,
                0f, l, 0f,

                // ось Z
                0f, 0f, -l,
                0f, 0f, l
        ).toNativeOrderBuffer()

        shadingProgram = ShadingProgram(
                Shader.fromSourceCode(ShaderType.VERTEX, vertexShaderCode),
                Shader.fromSourceCode(ShaderType.FRAGMENT, fragmentShaderCode)
        ).compile()

        shadingProgram.doUsingProgram {
            createAttributeBinding("a_vertex") {
                bindFloatBuffer(verticesBuffer, 3)
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 2f
        val far = 8f
        if (width > height) {
            val ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            val ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }

        val projectionMatrix = FloatArray(16)
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)


        val viewMatrix = FloatArray(16)
        // camera position
        val eyeX = 0f
        val eyeY = 0f
        val eyeZ = 2f

        // camera looks to point
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // camera-up vector pointing to
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)


        // computing final Model-View-Projection matrix
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        shadingProgram.doUsingProgram {
            createUniformBinding("u_modelViewProjectionMatrix") {
                bindUniformMatrix4fv(modelViewProjectionMatrix)
            }
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        shadingProgram.setColor(red = 1.0f)
        glDrawArrays(GL_TRIANGLES, 0, 3)

        shadingProgram.setColor(green = 1.0f)
        glDrawArrays(GL_TRIANGLES, 3, 3)

        shadingProgram.setColor(blue = 1.0f)
        glDrawArrays(GL_TRIANGLES, 6, 3)

        shadingProgram.setColor(red = 1.0f, green = 1.0f)
        glDrawArrays(GL_TRIANGLES, 9, 3)

        shadingProgram.setColor(blue = 1.0f, green = 1.0f)
        glDrawArrays(GL_LINES, 12, 3)

        shadingProgram.setColor(blue = 1.0f, red = 1.0f)
        glDrawArrays(GL_LINES, 15, 3)
    }
}

private fun ShadingProgram.setColor(red: Float = 0.0f,
                                    green: Float = 0.0f,
                                    blue: Float = 0.0f,
                                    alpha: Float = 1.0f) = apply {
    doUsingProgram {
        createAttributeBinding("a_color") {
            bind4f(red, green, blue, alpha)
        }
    }
}

private const val vertexShaderCode = "uniform mat4 u_modelViewProjectionMatrix;" +

        "attribute vec4 a_vertex;" +
        "attribute vec4 a_color;" +

        "varying vec4 v_color;" +

        "void main() {" +
        "v_color = a_color;" +
        "gl_PointSize = 10.0;" +
        "gl_Position = u_modelViewProjectionMatrix * a_vertex;" +
        "}"


private const val fragmentShaderCode = "precision mediump float;" +

        "varying vec4 v_color;" +

        "void main() {" +
        "gl_FragColor = v_color;" +
        "}"