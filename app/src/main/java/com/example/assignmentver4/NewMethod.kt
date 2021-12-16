package com.example.assignmentver4


import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.assignmentver4.databinding.ActivityNewMethodBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

class NewMethod : AppCompatActivity() {

    private lateinit var binding: ActivityNewMethodBinding
    private var latestUri: Uri? = null
    private var cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){
         result -> if(result){
            latestUri.let {uri: Uri? ->
                var intent = Intent(this,EditActivity::class.java)
                intent.putExtra("imageUri",uri)
                startActivity(intent)
            }
        }

    }
    private var galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){
        uri:Uri? ->
        uri.let {

            var intent = Intent(this,EditActivity::class.java)
            intent.putExtra("imageUri",it)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_AssignmentVer4)

        binding = ActivityNewMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraBtn.setOnClickListener {
            takeImage()
        }
        binding.galleryBtn.setOnClickListener {
            selectImage()
        }

    }

    private fun selectImage(){
        lifecycleScope.launchWhenStarted {
        galleryLauncher.launch("image/*")
        }
    }

    private fun takeImage(){
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let {uri: Uri? ->
                latestUri = uri
                cameraLauncher.launch(uri)
            }
        }

    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)

    }

    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

}