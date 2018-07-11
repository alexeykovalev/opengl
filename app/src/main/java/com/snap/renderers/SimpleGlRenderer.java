package com.snap.renderers;

import static android.opengl.GLES10.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.snap.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;;

/**
 * @author Alexey
 * @since 11/12/17
 */
class SimpleGlRenderer implements GLSurfaceView.Renderer {

    private static String TAG = SimpleGlRenderer.class.getCanonicalName();

    private static final int VERTEX_SIZE_IN_BYTES = (2 + 2) * 4;

    private final Context context;

    private FloatBuffer triangleVertices;

    SimpleGlRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated: ");
        triangleVertices = createTriangleVerticesWithTextureCoords();

        setupGlState(gl);
    }

    private void setupGlState(GL10 gl) {
        final int glTextureName = createGlTexture(gl);
        loadImageInsideTexture(gl, glTextureName, createTextureBitmap());

        gl.glClearColor(0.1f, 0.0f, 0.0f, 1f);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        // set identity matrix for projection - we are starting composing projection from the scratch
        gl.glLoadIdentity();
        gl.glOrthof(0, 320, 0, 480, 1, -1);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, glTextureName);

        triangleVertices.position(0);
        gl.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_SIZE_IN_BYTES, triangleVertices);
        triangleVertices.position(2);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE_IN_BYTES, triangleVertices);
    }

    private void loadImageInsideTexture(GL10 gl, int textureName, Bitmap textureBitmap) {
        // sets active texture that we are going to work with
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, textureBitmap, 0);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
    }

    private FloatBuffer createTriangleVerticesWithTextureCoords() {
        // x, y, RGBA - 1 float for each letter * 4 - bytes representing float
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(3 * VERTEX_SIZE_IN_BYTES);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer result = byteBuffer.asFloatBuffer();
        result.put(new float[]{
                0.0f, 0.0f, 0.0f, 1.0f,
                319.0f, 0.0f, 1.0f, 1.0f,
                160.0f, 479.0f, 0.5f, 0.0f
        });
        result.flip();
        return result;
    }

    /**
     * Returns ID of the generated Texture object.
     */
    private int createGlTexture(GL10 gl) {
        final int[] ids = new int[1];
        gl.glGenTextures(1, ids, 0);
        return ids[0];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged: ");
        gl.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL_COLOR_BUFFER_BIT);
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
    }

    private Bitmap createTextureBitmap() {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.lnx);
    }
}
