package com.snap.lighting;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class Shader {

    /**
     * Handle (reference) to shading program.
     */
    private int mProgramHandle;

    public Shader(String vertexShaderCode, String fragmentShaderCode) {
        mProgramHandle = createShadingProgram(vertexShaderCode, fragmentShaderCode);
    }

    private int createShadingProgram(String vertexShaderCode, String fragmentShaderCode) {
        final int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
        GLES20.glCompileShader(vertexShaderHandle);

        final int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShaderHandle);

        final int shadingProgramHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(shadingProgramHandle, vertexShaderHandle);
        GLES20.glAttachShader(shadingProgramHandle, fragmentShaderHandle);
        GLES20.glLinkProgram(shadingProgramHandle);
        return shadingProgramHandle;
    }

    //метод, который связывает
    //буфер координат вершин vertexBuffer с атрибутом a_vertex
    public void linkVertexBuffer(FloatBuffer vertexBuffer) {
        //устанавливаем активную программу
        GLES20.glUseProgram(mProgramHandle);
        //получаем ссылку на атрибут a_vertex
        int attrVertexHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_vertex");
        //включаем использование атрибута a_vertex
        GLES20.glEnableVertexAttribArray(attrVertexHandle);
        //связываем буфер координат вершин vertexBuffer с атрибутом a_vertex
        GLES20.glVertexAttribPointer(attrVertexHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    }

    //метод, который связывает
    //буфер координат векторов нормалей normalBuffer с атрибутом a_normal
    public void linkNormalBuffer(FloatBuffer normalBuffer) {
        //устанавливаем активную программу
        GLES20.glUseProgram(mProgramHandle);
        //получаем ссылку на атрибут a_normal
        int attrNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_normal");
        //включаем использование атрибута a_normal
        GLES20.glEnableVertexAttribArray(attrNormalHandle);
        //связываем буфер нормалей normalBuffer с атрибутом a_normal
        GLES20.glVertexAttribPointer(
                attrNormalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
    }

    //метод, который связывает
    //буфер цветов вершин colorBuffer с атрибутом a_color
    public void linkColorBuffer(FloatBuffer colorBuffer) {
        //устанавливаем активную программу
        GLES20.glUseProgram(mProgramHandle);
        //получаем ссылку на атрибут a_color
        int attrColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_color");
        //включаем использование атрибута a_color
        GLES20.glEnableVertexAttribArray(attrColorHandle);
        //связываем буфер нормалей colorBuffer с атрибутом a_color
        GLES20.glVertexAttribPointer(
                attrColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
    }

    //метод, который связывает матрицу модели-вида-проекции
    // modelViewProjectionMatrix с униформой u_modelViewProjectionMatrix
    public void linkModelViewProjectionMatrix(float[] modelViewProjectionMatrix) {
        //устанавливаем активную программу
        GLES20.glUseProgram(mProgramHandle);
        //получаем ссылку на униформу u_modelViewProjectionMatrix
        int uniformModelViewProjectionMatrixHandle =
                GLES20.glGetUniformLocation(mProgramHandle, "u_modelViewProjectionMatrix");
        //связываем массив modelViewProjectionMatrix
        //с униформой u_modelViewProjectionMatrix
        GLES20.glUniformMatrix4fv(
                uniformModelViewProjectionMatrixHandle, 1, false, modelViewProjectionMatrix, 0);
    }

    // метод, который связывает координаты камеры с униформой u_camera
    public void linkCamera(float xCamera, float yCamera, float zCamera) {
        //устанавливаем активную программу
        GLES20.glUseProgram(mProgramHandle);
        //получаем ссылку на униформу u_camera
        int uniformCameraPositionHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_camera");
        // связываем координаты камеры с униформой u_camera
        GLES20.glUniform3f(uniformCameraPositionHandle, xCamera, yCamera, zCamera);
    }

    // метод, который связывает координаты источника света
    // с униформой u_lightPosition
    public void linkLightSource(float xLightPosition, float yLightPosition, float zLightPosition) {
        //устанавливаем активную программу
        GLES20.glUseProgram(mProgramHandle);
        //получаем ссылку на униформу u_lightPosition
        int uniformLightPositionHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_lightPosition");
        // связываем координаты источника света с униформой u_lightPosition
        GLES20.glUniform3f(uniformLightPositionHandle, xLightPosition, yLightPosition, zLightPosition);
    }

    public void useProgram() {
        GLES20.glUseProgram(mProgramHandle);
    }
}
