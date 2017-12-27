package com.snap;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.snap.lighting.SceneLightRenderer;

public class OpenGlTestActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private boolean isRendererSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);

        setupSurfaceView();
        setContentView(glSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRendererSet) {
            glSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRendererSet) {
            glSurfaceView.onPause();
        }
    }

    private void setupSurfaceView() {
//        if (isSupportsEgl2()) {
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(createRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        isRendererSet = true;
//        } else {
//            Toast.makeText(this, "OpenGl does not supported", Toast.LENGTH_LONG).show();
//        }
    }

    @NonNull
    private GLSurfaceView.Renderer createRenderer() {
//        return new LessonOneRenderer(getBaseContext());
        return new SceneLightRenderer();
    }

    private boolean isSupportsEgl2() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            return configurationInfo.reqGlEsVersion >= 0x20000;
        }
        return false;
    }
}
