package com.snap.model.shading

import android.opengl.GLES20

import com.snap.model.exception.GlException
import com.snap.model.exception.GlLibException

private const val notDefinedProgramHandle = 0

/**
 * Represents Pair of shaders - Vertex and Fragment.
 */
internal class ShadingProgram(private val vertexShader: Shader,
                              private val fragmentShader: Shader) {

    init {
        if (vertexShader.type !== ShaderType.VERTEX) {
            throw IllegalArgumentException("First param has to be a Vertex shader.")
        }
        if (fragmentShader.type !== ShaderType.FRAGMENT) {
            throw IllegalArgumentException("Second param has to be a Fragment shader.")
        }
    }

    /**
     * Handle (reference) to shading program.
     */
    private var programHandle: Int = 0

    val isSetup: Boolean
        get() = programHandle != notDefinedProgramHandle

    fun setup() {
        if (isSetup) {
            throw GlLibException("Shading program already createAndCompile.")
        }
        programHandle = createShadingProgram(vertexShader, fragmentShader)
    }

    private fun createShadingProgram(vertexShader: Shader, fragmentShader: Shader): Int {
        val shadingProgramHandle = GLES20.glCreateProgram()
        if (shadingProgramHandle == notDefinedProgramHandle) {
            throw GlException("Could not create shading program.")
        }

        vertexShader.createAndCompile()
        GLES20.glAttachShader(shadingProgramHandle, vertexShader.shaderHandle)

        fragmentShader.createAndCompile()
        GLES20.glAttachShader(shadingProgramHandle, fragmentShader.shaderHandle)

        GLES20.glLinkProgram(shadingProgramHandle)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(shadingProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val errorMessage = GLES20.glGetProgramInfoLog(shadingProgramHandle)
            GLES20.glDeleteProgram(shadingProgramHandle)
            throw GlException("Could not link shading program. $errorMessage")
        }

        return shadingProgramHandle
    }

    fun release() {
        if (isSetup) {
            GLES20.glDeleteProgram(programHandle)
            programHandle = notDefinedProgramHandle
        }
    }

    fun useProgram() {
        if (!isSetup) {
            throw IllegalStateException("You have to createAndCompile program before using it.")
        }
        GLES20.glUseProgram(programHandle)
    }

    fun doUsingProgram(block: ShadingProgram.() -> Unit) = apply {
        useProgram()
        block()
    }

    fun createAttributeBinding(attributeName: String, block: AttributeBinding.() -> Unit = {}) =
            AttributeBinding(programHandle, attributeName).apply(block)

    fun createUniformBinding(uniformName: String, block: UniformBinding.() -> Unit = {}) =
            UniformBinding(programHandle, uniformName).apply(block)
}
