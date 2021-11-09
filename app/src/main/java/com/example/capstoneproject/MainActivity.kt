package com.example.capstoneproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

typealias CameraBitmapOutputListner = (bitmap: Bitmap) -> Unit

class MainActivity : AppCompatActivity() {

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupCameraControllers()

        if (allPermissionsGranted) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setupCameraControllers() { // 아이콘 변경
        fun setLensButtonIcon() {
            btn_camera_lens_face.setImageDrawable(
                AppCompatResources.getDrawable(
                    applicationContext,
                    if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                        R.drawable.ic_baseline_camera_rear_24
                    else
                        R.drawable.ic_baseline_camera_front_24
                )
            )
        }

        setLensButtonIcon()
        btn_camera_lens_face.setOnClickListener { // 버튼 클릭
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            setLensButtonIcon() // 아이콘 변경
            setupCameraUseCases() // 카메라 변경
        }
        try {
            btn_camera_lens_face.isEnabled = hasBackCamera && hasFrontCamera
        } catch (exception: CameraInfoUnavailableException) {
            btn_camera_lens_face.isEnabled = false
        }
    }

    private fun setupCameraUseCases() { // 카메라 변경
        val cameraSelector: CameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()

        val metrics: DisplayMetrics =
            DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val rotation: Int = viewFinder.display.rotation
        preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .build()

        cameraProvider?.unbindAll()
        try {
            camera = cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
            preview?.setSurfaceProvider(viewFinder.createSurfaceProvider())
        } catch (exc: Exception) {
            Log.e(Constants.TAG, "카메라 작동 실패", exc)
        }
    }


    private fun startCamera() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()

            lensFacing = when {
                hasFrontCamera -> CameraSelector.LENS_FACING_FRONT
                hasBackCamera -> CameraSelector.LENS_FACING_BACK
                else -> throw IllegalStateException("장치를 찾을 수 없습니다.")
            }
            setupCameraControllers()
            setupCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private val allPermissionsGranted: Boolean
        get() {
            return Constants.REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    baseContext, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    private val hasBackCamera: Boolean
        get() {
            return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
        }
    private val hasFrontCamera: Boolean
        get() {
            return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
        }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {

            if (allPermissionsGranted) {
                //모든 권한이 실행됐을 경우
                startCamera()
            } else {
                Toast.makeText(this, "권한이 불충분합니다..", Toast.LENGTH_SHORT).show()
                finish()
            }

        }
    }
}

