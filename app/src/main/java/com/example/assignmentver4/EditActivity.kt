package com.example.assignmentver4

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.example.assignmentver4.databinding.ActivityEditBinding
import com.theartofdev.edmodo.cropper.CropImage
import java.io.IOException
import java.util.*


class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cropActivityLauncher : ActivityResultLauncher<Uri?>

    private var take: Uri? =null

    private var cropActivityResult = object : ActivityResultContract<Uri,Uri?>(){
        override fun createIntent(context: Context, input: Uri?): Intent {
            return CropImage.activity(input)
                .getIntent(this@EditActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        take = intent?.extras?.get("imageUri") as Uri

        binding.editImage.setImageURI(take)

        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
        }

        cropActivityLauncher = registerForActivityResult(cropActivityResult){
            it?.let {
                binding.editImage.setImageURI(it)
            }

        }

        binding.rotateImage.setOnClickListener {
            binding.editImage.rotation += 90
        }
        binding.cropImage.setOnClickListener {

            cropActivityLauncher.launch(take)

        }
        binding.undoImage.setOnClickListener {
            binding.editImage.setImageURI(take)
        }
        binding.saveImage.setOnClickListener {
            updateOrRequestPermissions()
            when{
                writePermissionGranted ->savePhotoToExternalStorage(UUID.randomUUID().toString(),binding.editImage.drawToBitmap())
            }
            var intent = Intent(this,NewMethod::class.java)
                Toast.makeText(applicationContext,"Image Saved Successfully",Toast.LENGTH_SHORT)
                startActivity(intent)
        }
    }
    private fun updateOrRequestPermissions() {

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if(!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    private fun savePhotoToExternalStorage(displayName: String, bmp: Bitmap): Boolean {
        val imageCollection = sdk29andUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }
        return try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                    contentResolver.openOutputStream(uri).use { outputStream ->
                        if (!bmp.compress(Bitmap.CompressFormat.PNG, 95, outputStream)) {
                            throw IOException("Couldn't save bitmap")
                        } else Toast.makeText(
                            applicationContext,
                            "Picture Saved Successfully",
                            Toast.LENGTH_SHORT
                        )
                    }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        } catch(e: IOException) {
            e.printStackTrace()
            false
        }
    }




}