package com.snap

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.snap.renderers.PlaygroundRenderer

class OpenGlTestActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private var isRendererSet: Boolean = false

    private val renderer: GLSurfaceView.Renderer by lazy {
        PlaygroundRenderer()
    }

    private val isSupportsEgl2: Boolean
        get() {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo
            return configurationInfo.reqGlEsVersion >= 0x20000
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)

        setupSurfaceView()
        setContentView(glSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        if (isRendererSet) {
            glSurfaceView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRendererSet) {
            glSurfaceView.onPause()
        }
    }

    private fun setupSurfaceView() {
        if (isSupportsEgl2) {
            glSurfaceView.setEGLContextClientVersion(2)
            glSurfaceView.setRenderer(renderer)
            glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            isRendererSet = true
        } else {
            Toast.makeText(this, "OpenGl does not supported", Toast.LENGTH_LONG).show()
        }
    }
}
