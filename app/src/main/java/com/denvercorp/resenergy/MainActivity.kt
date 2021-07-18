package com.denvercorp.resenergy

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denvercorp.resenergy.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var imageCapture:ImageCapture?=null

    private lateinit var outputDirectory: File

    private lateinit var editTextBoxInfo: EditText

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

        binding.btnTakePhoto.setOnClickListener{
//            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show()
            editTextBoxInfo = findViewById(R.id.infodata)

            if (!TextUtils.isEmpty(editTextBoxInfo.text)){
                takePhoto()
            } else{
                Toast.makeText(this, "Будь ласка заповніть поле!", Toast.LENGTH_SHORT).show()
            }


        }

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



    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getOutputDirectory():File{

// : File
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
//        getOutputDirectory(),
        val photoFile = File(
            getOutputDirectory(),
            editTextBoxInfo.text.toString() + " " + SimpleDateFormat(
                Constants.FILE_NAME_FORMAT,
                 Locale.getDefault())
                    .format(
                        System.currentTimeMillis()
                    )
                    +".jpg"
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
                val msg = "Фото збережено в:"

                Toast.makeText(this@MainActivity, "$msg $savedUri", Toast.LENGTH_LONG).show()
                editTextBoxInfo.setText("")
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

