package com.vinrak.mytextrecognition

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.ByteArrayOutputStream

const val GALLERY_PICKER_RESULT = 102
const val CAMERA_PICKER_RESULT = 103

fun log(mTag: String, message: String) {
    Log.d(mTag, message)
}

//for below android Q
fun getImageUri(inContext: Context, bitmap: Bitmap, mTag: String): Uri? {
    log(mTag, "run for api less than 29")
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path: String = MediaStore.Images.Media.insertImage(
        inContext.contentResolver,
        bitmap,
        "profile_picture",
        null
    )
    return Uri.parse(path)
}

//for android Q and above
fun getImageUri(activity: Activity, bitmap: Bitmap, mTag: String, cxt: Context): Uri? {
    val relativeLocation = Environment.DIRECTORY_DCIM// + File.pathSeparator + "profile_picture"
    //val relativeLocation = cxt.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath + File.separator + "profile_picture"
    log(mTag, "relativeLocation = $relativeLocation")
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //this one
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
    val resolver = activity.contentResolver
    //val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    try {
        uri?.let { _uri ->
            val stream = resolver.openOutputStream(_uri)
            stream?.let { _stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 80, _stream)) {
                    log(mTag, "Failed to save bitmap.")
                }
            } ?: run {
                log(mTag, "Failed to get output stream.")
            }
        } ?: run { log(mTag, "Failed to create new MediaStore record") }
    } catch (e: java.lang.Exception) {
        if (uri != null) {
            resolver.delete(uri, null, null)
        }
    } finally {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
    }
    return uri
}