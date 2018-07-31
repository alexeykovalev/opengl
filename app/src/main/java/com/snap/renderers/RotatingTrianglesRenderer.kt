package com.snap.renderers

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.snap.identityMatrix
import com.snap.model.shading.Shader
import com.snap.model.shading.ShaderType
import com.snap.model.shading.ShadingProgram
import com.snap.model.toNativeOrderBuffer
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class RotatingTrianglesRenderer : GLSurfaceView.Renderer {

    private lateinit var verticesBuffer: FloatBuffer
    private lateinit var shadingProgram: ShadingProgram

    private var projectionMatrix = identityMatrix()
    private val modelViewProjectionMatrix = identityMatrix()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glEnable(GL_DEPTH_TEST)

        val s = 0.4f
        val d = 0.9f
        val l = 3f

        verticesBuffer = floatArrayOf(
                // red
                -2 * s, -s, d,
                2 * s, -s, d,
                0f, s, d,

                // green
                -2 * s, -s, -d,
                2 * s, -s, -d,
                0f, s, -d,

                // blue
                d, -s, -2 * s,
                d, -s, 2 * s,
                d, s, 0f,

                // yellow
                -d, -s, -2 * s,
                -d, -s, 2 * s,
                -d, s, 0f,

                // X axis
                -l, 0f, 0f,
                l, 0f, 0f,

                // Y axis
                0f, -l, 0f,
                0f, l, 0f,

                // Z axis
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
        projectionMatrix = defineProjectionMatrix(width, height)

//        computeStaticMvpMatrix()
//        bindMvpMatrix()
    }

    private fun bindMvpMatrix() {
        shadingProgram.doUsingProgram {
            createUniformBinding("u_modelViewProjectionMatrix") {
                bindUniformMatrix4fv(modelViewProjectionMatrix)
            }
        }
    }

    private fun computeStaticMvpMatrix() {
        val viewMatrix = defineViewMatrix()
        val modelMatrix = defineModelMatrix()

        // computing final Model-View-Projection matrix
        val modelViewMatrix = FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
            Matrix.multiplyMM(this, 0, modelMatrix, 0, viewMatrix, 0)
        }
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
    }

    private fun computeDynamicMvpMatrix() {
        val animationPeriod = 5000
        val time = (SystemClock.uptimeMillis() % animationPeriod) / animationPeriod.toFloat()
        val angle = 2 * Math.PI * time

        // camera position
        val eyeX = (4f * Math.cos(angle)).toFloat()
        val eyeY = 1f
        val eyeZ = (4f * Math.sin(angle)).toFloat()

        // camera looks to point
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // camera-up vector pointing to
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        val viewMatrix = identityMatrix().apply {
            Matrix.setLookAtM(this, 0,
                    eyeX, eyeY, eyeZ,
                    centerX, centerY, centerZ,
                    upX, upY, upZ)
        }

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    private fun defineModelMatrix(): FloatArray {
        return identityMatrix()

//        return FloatArray(16).apply {
//            Matrix.setIdentityM(this, 0)
////            Matrix.scaleM(this, 0, 1.0f, 1.0f, 1.0f)
//            Matrix.rotateM(this, 0, 0f, 0f, 1.0f, 0f)
//        }
    }

    private fun defineViewMatrix(): FloatArray {
        // camera position
        val eyeX = 0f
        val eyeY = 0f
        val eyeZ = 4f

        // camera looks to point
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // camera-up vector pointing to
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        return identityMatrix().apply {
            Matrix.setLookAtM(this, 0,
                    eyeX, eyeY, eyeZ,
                    centerX, centerY, centerZ,
                    upX, upY, upZ)
        }
    }

    private fun defineProjectionMatrix(width: Int, height: Int): FloatArray {
        var left = -1.0f
        var right = 1.0f
        var bottom = -1f
        var top = 1f
        val near = 2f
        val far = 8f
//        if (width > height) {
//            val ratio = width.toFloat() / height
//            left *= ratio
//            right *= ratio
//        } else {
//            val ratio = height.toFloat() / width
//            bottom *= ratio
//            top *= ratio
//        }

        return FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
            Matrix.frustumM(this, 0, left, right, bottom, top, near, far)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        computeDynamicMvpMatrix()
        bindMvpMatrix()

        drawTrianglesAndAxes()
    }

    private fun drawTrianglesAndAxes() {
        // draw red triangle
        shadingProgram.setColor(red = 1.0f)
        glDrawArrays(GL_TRIANGLES, 0, 3)

        // draw green triangle
        shadingProgram.setColor(green = 1.0f)
        glDrawArrays(GL_TRIANGLES, 3, 3)

        // draw blue triangle
        shadingProgram.setColor(blue = 1.0f)
        glDrawArrays(GL_TRIANGLES, 6, 3)

        // draw yellow triangle
        shadingProgram.setColor(red = 1.0f, green = 1.0f)
        glDrawArrays(GL_TRIANGLES, 9, 3)


        // draw X axis
        shadingProgram.setColor(blue = 1.0f, green = 1.0f)
        glDrawArrays(GL_LINES, 12, 2)

        // draw Y axis
        shadingProgram.setColor(blue = 1.0f, red = 1.0f)
        glDrawArrays(GL_LINES, 14, 2)

        // draw Z axis
        shadingProgram.setColor(blue = 1.0f, red = 1.0f)
        glDrawArrays(GL_LINES, 16, 2)
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