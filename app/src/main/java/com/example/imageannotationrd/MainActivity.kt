package com.example.imageannotationrd

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var selectImageBtn: Button

    val PICK_IMAGE_REQUEST = 1

    var imageUriFromCamera: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectImageBtn = findViewById(R.id.pick_image_btn)

        selectImageBtn.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.putExtra("return-data", true)

            val chooserIntent = Intent.createChooser(intent, "Select Image")
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, arrayOf(
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                )
            )

            // Create a file to store the image that will be captured
            val imageFile = createImageFile()

            // Get the URI of the file
            imageUriFromCamera = Uri.fromFile(imageFile)
            Log.d("Debugg", "file url is: $imageUriFromCamera.toString()")

            // Set the output of the camera app to be the URI of the file
            chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFromCamera)

            startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            var selectedImage: Bitmap? = null

            if(data?.data != null){
                try {
                    val imageUri = data.data
                    selectedImage = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            else{
//                selectedImage = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUriFromCamera!!))
//                Log.d("Debugg", "selected image bitmap from camera is: ${selectedImage.toString()}")

//                val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
//                val imageUriFromCamera: Uri? = saveImageToMediaStore(bitmap)
//                selectedImage = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUriFromCamera!!))
                selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(imageUriFromCamera.toString()));

            }
            openImageForEditing(selectedImage!!)

        }
    }

    private fun openImageForEditing(image: Bitmap){
        val intent = Intent(applicationContext, EditingActivity::class.java)
        val fileName = saveImageToDisk(image)
        intent.putExtra("selected_image", fileName)
        startActivity(intent)
    }

    private fun saveImageToDisk(image: Bitmap): String{
        val filename = "bitmap.png"
        try {
            //Write file
            val stream: FileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE)
            image.compress(Bitmap.CompressFormat.PNG, 100, stream)

            //Cleanup
            stream.close()
            image.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return filename
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
}