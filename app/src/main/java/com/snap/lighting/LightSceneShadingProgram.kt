package com.snap.lighting

import com.snap.model.exception.GlLibException
import com.snap.model.shading.Shader
import com.snap.model.shading.ShaderType
import com.snap.model.shading.ShadingPair
import com.snap.model.shading.ShadingProgram

import java.nio.FloatBuffer

class LightSceneShadingProgram private constructor(private val shadingProgram: ShadingProgram) {

    @Throws(GlLibException::class)
    fun setup() {
        shadingProgram.setup()
    }

    fun linkVertexBuffer(vertexBuffer: FloatBuffer) {
        shadingProgram.executeUsingProgram {
            createAttributeBinding("a_vertex") {
                bindFloatBuffer(vertexBuffer, 3, false, 0)
            }
        }
    }

    fun linkNormalBuffer(normalBuffer: FloatBuffer) {
        shadingProgram.executeUsingProgram {
            createAttributeBinding("a_normal") {
                bindFloatBuffer(normalBuffer, 3, false, 0)
            }
        }
    }

    fun linkColorBuffer(colorBuffer: FloatBuffer) {
        shadingProgram.executeUsingProgram {
            createAttributeBinding("a_color") {
                bindFloatBuffer(colorBuffer, 4, false, 0)
            }
        }
    }

    fun linkModelViewProjectionMatrix(modelViewProjectionMatrix: FloatArray) {
        shadingProgram.executeUsingProgram {
            createUniformBinding("u_modelViewProjectionMatrix") {
                bindUniformMatrix4fv(modelViewProjectionMatrix)
            }
        }
    }

    fun linkCamera(xCamera: Float, yCamera: Float, zCamera: Float) {
        shadingProgram.executeUsingProgram {
            createUniformBinding("u_camera") {
                bindUniform3f(xCamera, yCamera, zCamera)
            }
        }
    }

    fun linkLightSource(xLightPosition: Float, yLightPosition: Float, zLightPosition: Float) {
        shadingProgram.executeUsingProgram {
            createUniformBinding("u_lightPosition") {
                bindUniform3f(xLightPosition, yLightPosition, zLightPosition)
            }
        }
    }

    companion object {

        private val VERTEX_SHADER_CODE = "uniform mat4 u_modelViewProjectionMatrix;" +

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
                "}"
        private val FRAGMENT_SHADER_CODE = "precision mediump float;" +

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
                "}"

        fun newInstance(): LightSceneShadingProgram {
            val shadingPair = ShadingPair(
                    createVertexShader(), createFragmentShader())
            return LightSceneShadingProgram(ShadingProgram(shadingPair))
        }

        private fun createVertexShader(): Shader {
            return Shader.fromSourceCode(ShaderType.VERTEX, LightSceneShadingProgram.VERTEX_SHADER_CODE)
        }

        private fun createFragmentShader(): Shader {
            return Shader.fromSourceCode(ShaderType.FRAGMENT, LightSceneShadingProgram.FRAGMENT_SHADER_CODE)
        }
    }
}
