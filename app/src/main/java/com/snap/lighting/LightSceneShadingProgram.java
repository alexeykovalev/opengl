package com.snap.lighting;

import com.snap.model.exception.GlLibException;
import com.snap.model.shading.Shader;
import com.snap.model.shading.ShadingProgram;

import java.nio.FloatBuffer;

public class LightSceneShadingProgram {

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

    private final ShadingProgram shadingProgram;

    public static LightSceneShadingProgram newInstance() {
        final ShadingProgram.ShadingPair shadingPair = new ShadingProgram.ShadingPair(
                createVertexShader(), createFragmentShader());
        return new LightSceneShadingProgram(new ShadingProgram(shadingPair));
    }

    private static Shader createVertexShader() {
        return Shader.fromSourceCode(Shader.ShaderType.VERTEX, LightSceneShadingProgram.VERTEX_SHADER_CODE);
    }

    private static Shader createFragmentShader() {
        return Shader.fromSourceCode(Shader.ShaderType.FRAGMENT, LightSceneShadingProgram.FRAGMENT_SHADER_CODE);
    }

    private LightSceneShadingProgram(ShadingProgram shadingProgram) {
        this.shadingProgram = shadingProgram;
    }

    public void setup() throws GlLibException {
        shadingProgram.setup();
    }

    private void executeUsingShadingProgram(ShadingProgram.ShadingProgramAction action) {
        shadingProgram.executeAction(action);
    }

    public void linkVertexBuffer(final FloatBuffer vertexBuffer) {
        executeUsingShadingProgram(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                onProgram.createAttributeBinding("a_vertex")
                        .bindVertices(vertexBuffer, 3, false, 0);
            }
        });
    }

    public void linkNormalBuffer(final FloatBuffer normalBuffer) {
        executeUsingShadingProgram(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                shadingProgram.createAttributeBinding("a_normal")
                        .bindVertices(normalBuffer, 3, false, 0);
            }
        });
    }

    public void linkColorBuffer(final FloatBuffer colorBuffer) {
        executeUsingShadingProgram(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                shadingProgram.createAttributeBinding("a_color")
                        .bindVertices(colorBuffer, 4, false, 0);
            }
        });
    }

    public void linkModelViewProjectionMatrix(final float[] modelViewProjectionMatrix) {
        executeUsingShadingProgram(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                onProgram.createUniformBinding("u_modelViewProjectionMatrix")
                        .bindUniformMatrix4fv(modelViewProjectionMatrix);
            }
        });
    }

    public void linkCamera(final float xCamera, final float yCamera, final float zCamera) {
        executeUsingShadingProgram(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                onProgram.createUniformBinding("u_camera")
                        .bindUniform3f(xCamera, yCamera, zCamera);
            }
        });
    }

    public void linkLightSource(final float xLightPosition, final float yLightPosition, final float zLightPosition) {
        executeUsingShadingProgram(new ShadingProgram.ShadingProgramAction() {
            @Override
            public void execute(ShadingProgram onProgram) {
                onProgram.createUniformBinding("u_lightPosition")
                        .bindUniform3f(xLightPosition, yLightPosition, zLightPosition);
            }
        });
    }
}
