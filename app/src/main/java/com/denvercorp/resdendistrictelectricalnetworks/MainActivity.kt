package com.denvercorp.resdendistrictelectricalnetworks

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denvercorp.resdendistrictelectricalnetworks.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var imageCapture:ImageCapture?=null

    private lateinit var outputDirectory: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionGranted()){
            startCamera()
//            Toast.makeText(this, "We Have All Permissions", Toast.LENGTH_SHORT).show()
        }else{
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera(){

        val cameraProviderFeature = ProcessCameraProvider.getInstance(this)
//        Toast.makeText(this, "Started Camera", Toast.LENGTH_SHORT).show()
        cameraProviderFeature.addListener({
//            Toast.makeText(this, "Added listener", Toast.LENGTH_SHORT).show()
            val cameraProvider: ProcessCameraProvider = cameraProviderFeature.get()

            val preview = Preview.Builder().build().also{ mPreview -> mPreview.setSurfaceProvider(
                    binding.viewFinder.surfaceProvider
                    )}
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try{

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            }catch (e:Exception){
                Log.d(Constants.TAG, "startCamera Fail:", e)
            }
        }, ContextCompat.getMainExecutor(this))

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        outputDirectory = getOutputDirectory()

        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS){

           if (allPermissionGranted()){
               startCamera()
           }else{
//               Toast.makeText(this, "Permissions denied by user", Toast.LENGTH_SHORT).show()
               finish()
           }

       }

        binding.btnTakePhoto.setOnClickListener{
//            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show()
            takePhoto()
        }

    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let{mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply{
                mkdirs()
            }
        }

        return if(mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto(){
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            getOutputDirectory(),
            SimpleDateFormat(
                Constants.FILE_NAME_FORMAT,
                Locale.getDefault())
                    .format(
                        System.currentTimeMillis()
                    )
                    +"someday.jpg"
        )

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

    imageCapture.takePicture(
        outputOption, ContextCompat.getMainExecutor(this),
        object: ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults){


                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo Saved"

                Toast.makeText(this@MainActivity, "$msg $savedUri", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: ImageCaptureException){
                Log.e(Constants.TAG, "onError: ${exception.message}", exception)
            }

        }
    )
    }

    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all{
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }



}

