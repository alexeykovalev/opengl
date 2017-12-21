package com.snap;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class LessonOneRenderer implements GLSurfaceView.Renderer {

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private final float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private final float[] mViewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private final float[] mProjectionMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private final float[] mMVPMatrix = new float[16];

    /**
     * Store our model data in a float buffer.
     */
    private final FloatBuffer mainSailTriangleVerticesBuffer;
    private final FloatBuffer jibSailTriangleVerticesBuffer;
    private final FloatBuffer seaTriangleOneVerticesBuffer;
    private final FloatBuffer seaTriangleTwoVerticesBuffer;
    private final FloatBuffer boardTriangleOneVerticesBuffer;
    private final FloatBuffer boardTriangleTwoVerticesBuffer;


    /**
     * This will be used to pass in the transformation matrix.
     */
    private int mMVPMatrixHandle;

    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;

    /**
     * This will be used to pass in model color information.
     */
    private int mColorHandle;

    /**
     * How many bytes per float.
     */
    private static final int BYTES_PER_FLOAT = 4;

    /**
     * How many elements per vertex.
     */
    private static final int ELEMENTS_PER_VERTEX = 7 * BYTES_PER_FLOAT;

    private float boatPositionX;

    private final Context context;


    /**
     * Initialize the model data.
     */
    public LessonOneRenderer(Context context) {
        this.context = context;

        // This triangle is white_blue.First sail is mainsail
        // triangle1VerticesData
        final float[] mainSailTriangle = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                0.0f, -0.25f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f,

                0.0f, 0.56f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f};

        // This triangle is white_blue..The second is called the jib sail
        // triangle2VerticesData
        final float[] jibSailTriangle = {
                // X, Y, Z,
                // R, G, B, A
                -0.25f, -0.25f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f,

                0.03f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                -0.25f, 0.4f, 0.0f,
                0.8f, 0.8f, 1.0f, 1.0f};

        // This triangle3 is blue.
        // triangle3VerticesData
        final float[] seaTriangleOne = {
                // X, Y, Z,
                // R, G, B, A
                -1.0f, -1.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, -0.35f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                -1.0f, -0.35f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f};


        // This triangle4 is blue.
        // triangle4VerticesData
        final float[] seaTriangleTwo = {
                // X, Y, Z,
                // R, G, B, A
                -1.0f, -1.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, -1.5f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, -0.35f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f};

        // This triangle5VerticesData is brown.
        final float[] boardTriangleOne = {
                // X, Y, Z,
                // R, G, B, A
                -0.4f, -0.3f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                -0.4f, -0.4f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                0.3f, -0.3f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f};

        // This triangle6VerticesData is brown.
        final float[] boardTriangleTwo = {
                // X, Y, Z,
                // R, G, B, A
                -0.4f, -0.4f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                0.22f, -0.4f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f,

                0.3f, -0.3f, 0.0f,
                0.7f, 0.3f, 0.4f, 1.0f};


        // Initialize the buffers.
        mainSailTriangleVerticesBuffer = ByteBuffer.allocateDirect(mainSailTriangle.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        jibSailTriangleVerticesBuffer = ByteBuffer.allocateDirect(jibSailTriangle.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        seaTriangleOneVerticesBuffer = ByteBuffer.allocateDirect(seaTriangleOne.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        seaTriangleTwoVerticesBuffer = ByteBuffer.allocateDirect(seaTriangleTwo.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        boardTriangleOneVerticesBuffer = ByteBuffer.allocateDirect(boardTriangleOne.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        boardTriangleTwoVerticesBuffer = ByteBuffer.allocateDirect(boardTriangleTwo.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();


        mainSailTriangleVerticesBuffer.put(mainSailTriangle).position(0);
        jibSailTriangleVerticesBuffer.put(jibSailTriangle).position(0);
        seaTriangleOneVerticesBuffer.put(seaTriangleOne).position(0);

        seaTriangleTwoVerticesBuffer.put(seaTriangleTwo).position(0);
        boardTriangleOneVerticesBuffer.put(boardTriangleOne).position(0);
        boardTriangleTwoVerticesBuffer.put(boardTriangleTwo).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.7f, 1.0f);
        initCameraViewMatrix();

        // Load in the vertex shader.
        final int vertexShaderHandle = compileVertexShader();
        // Load in the fragment shader
        final int fragmentShaderHandle = compileFragmentShader();
        // Create shading program based on compiled shaders -> output of vertex shader will be an
        // input of fragment shader
        final int programHandle = compileShadingProgram(vertexShaderHandle, fragmentShaderHandle);

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }

    private void initCameraViewMatrix() {
        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward this vector
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our Up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

    private int compileVertexShader() {
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"       // A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.

                        + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.

                        + "void main()                    \n"     // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"     // Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n"  // gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);
            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);
            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }
        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }
        return vertexShaderHandle;
    }

    private int compileFragmentShader() {
        final String fragmentShader =
                "precision mediump float;       \n"       // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"     // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"     // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
                        + "}                              \n";

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }
        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }
        return fragmentShaderHandle;
    }

    private int compileShadingProgram(int vertexShaderHandle, int fragmentShaderHandle) {
        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();
        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }
        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }
        return programHandle;
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);
        initProjectionMatrix(width, height);
    }

    private void initProjectionMatrix(float width, int height) {
        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, boatPositionX, 0.0f, 0.0f);
        drawTriangle(mainSailTriangleVerticesBuffer);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, boatPositionX + 0.3f, 0.0f, 0.0f);
        drawTriangle(jibSailTriangleVerticesBuffer);
        if (boatPositionX <= 1) {
            boatPositionX = (float) (boatPositionX + 0.001);
        } else {
            boatPositionX = 0;
        }

        Matrix.setIdentityM(mModelMatrix, 0);
        drawTriangle(seaTriangleOneVerticesBuffer);

        Matrix.setIdentityM(mModelMatrix, 0);
        drawTriangle(seaTriangleTwoVerticesBuffer);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, boatPositionX, 0.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, 0, 0.0f, 0.0f, 1.0f);
        drawTriangle(boardTriangleOneVerticesBuffer);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, boatPositionX, 0.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, 0, 0.0f, 0.0f, 1.0f);
        drawTriangle(boardTriangleTwoVerticesBuffer);

    }

    /**
     * Draws a triangle from the given vertex data.
     */
    private void drawTriangle(final FloatBuffer triangleVerticesBuffer) {
        // Pass in the position information
        triangleVerticesBuffer.position(0);
        // Size of the position data in elements.
        final int positionDataSize = 3;
        GLES20.glVertexAttribPointer(mPositionHandle, positionDataSize, GLES20.GL_FLOAT, false,
                ELEMENTS_PER_VERTEX, triangleVerticesBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        triangleVerticesBuffer.position(3);
        // Size of the color data in elements.
        final int colorDataSize = 4;
        GLES20.glVertexAttribPointer(mColorHandle, colorDataSize, GLES20.GL_FLOAT, false,
                ELEMENTS_PER_VERTEX, triangleVerticesBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}
