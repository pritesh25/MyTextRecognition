package com.vinrak.mytextrecognition

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer

class MainActivity : AppCompatActivity() {

    private val mTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonGallery).setOnClickListener {
            galleryPicture()
        }

        findViewById<Button>(R.id.buttonCamera).setOnClickListener {
            cameraPicture()
        }

    }

    private fun galleryPicture() {
        try {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PICKER_RESULT)
        } catch (e: Exception) {
            Log.d(mTag, "(ivGalleryPicker) catch error = ${e.message}")

        }
    }

    private fun cameraPicture() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_PICKER_RESULT)
        } catch (e: Exception) {
            Log.d(mTag, "(ivCameraPicker) catch error = ${e.message}")
        }
    }

    private fun runTextRecognition(uriPath: Uri) {
        //val image = InputImage.fromBitmap(mSelectedImage, 0)
        val image = InputImage.fromFilePath(this, uriPath)
        val recognizer: TextRecognizer = TextRecognition.getClient()
        recognizer.process(image)
                .addOnSuccessListener(
                        OnSuccessListener<Any?> { texts ->
                            //mTextButton.setEnabled(true)
                            processTextRecognitionResult(texts as Text)
                        })
                .addOnFailureListener(
                        OnFailureListener { e -> // Task failed with an exception
                            //mTextButton.setEnabled(true)
                            Log.d(mTag, "catch error = ${e.printStackTrace()}")
                        })
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks: List<Text.TextBlock> = texts.textBlocks
        if (blocks.isEmpty()) {
            //showToast("No text found")
            Toast.makeText(this, "No text found", Toast.LENGTH_LONG).show()
            Log.d(mTag, "No text found")
            return
        }
        //mGraphicOverlay.clear()
        for (i in blocks.indices) {
            val lines: List<Text.Line> = blocks[i].getLines()
            for (j in lines.indices) {
                val elements: List<Text.Element> = lines[j].getElements()
                for (k in elements.indices) {
                    //val textGraphic: Graphic = TextGraphic(mGraphicOverlay, elements[k])
                    //mGraphicOverlay.add(textGraphic)
                    Log.d(mTag, "element = ${elements[k].text}")
                }
            }
        }
    }

    private val isInternetAvailable = true
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        when (requestCode) {
            GALLERY_PICKER_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {

                    if (isInternetAvailable) {
                        intent?.let {
                            log(mTag, "image uri path  = ${intent.data}")
                            //log(mTag, "image real path = ${cxt.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath + File.separator + "profile_picture.jpg"}")
                            //log(mTag, "kaustubh real path = ${getRealPathFromURI(cxt, intent.data)}")
                            //val inputStream = contentResolver.openInputStream(intent.data!!)
                            //updateProfilePicture(getBytes(inputStream!!))
                            runTextRecognition(intent.data!!)
                        } ?: kotlin.run {
                            log(mTag, "intent is null")
                        }
                    } else {
                        log(mTag, "no internet connection")
                    }
                } else {
                    log(mTag, "no image selected from gallery")
                }
            }
            CAMERA_PICKER_RESULT -> {

                if (resultCode == Activity.RESULT_OK) {

                    if (isInternetAvailable) {

                        try {
                            val bitmapPhoto = intent!!.extras!!.get("data") as Bitmap
                            log(mTag, "camera image uri = $bitmapPhoto")

                            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                log(mTag, "android Q or above run")
                                getImageUri(this, bitmapPhoto, mTag, this)
                            } else {
                                log(mTag, "below android Q run")
                                getImageUri(this, bitmapPhoto, mTag)
                            }
                            Log.d(mTag, "camera image uri = $uri")
                            //val inputStream = contentResolver.openInputStream(uri!!)
                            //updateProfilePicture(getBytes(inputStream!!))
                            runTextRecognition(uri!!)
                        } catch (e: java.lang.Exception) {
                            log(mTag, "CAMERA_PICKER_RESULT catch error = ${e.message}")
                        }
                    } else {
                        log(mTag, "no internet connection")
                    }
                } else {
                    log(mTag, "no image selected from camera")
                }
            }
            else -> {
                log(mTag, "wrong option selected")
            }
        }
    }

}