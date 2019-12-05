package com.coding180.image2text

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    lateinit var check: Button
    lateinit var imgview: ImageView
    lateinit var imageBitmap: Bitmap
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var toolbar: Toolbar
    lateinit var textView: TextView
    lateinit var relay: RelativeLayout
     var uri: Uri? = null
    var counter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        check = findViewById(R.id.check)
        imgview = findViewById(R.id.imgview)
        toolbar = findViewById(R.id.toolbar)
        textView = findViewById(R.id.textview)
        relay = findViewById(R.id.relay)

        check.setOnClickListener {
            when (counter) {
                0 -> return@setOnClickListener
                1 -> check()
                2 -> checkURI()
            }
        }
        toolbar.title = "Image2Text"
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(menuItem: MenuItem?): Boolean {

                when (menuItem?.itemId) {
                    R.id.camera -> dispatchTakePictureIntent()
                    R.id.gallery -> gallery()
                }
                return true
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imgview.visible(true)
            check.visible(true)
            textView.visible(false)
            imageBitmap = data?.extras?.get("data") as Bitmap
            imgview.setImageBitmap(imageBitmap)
            counter = 1
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMG_CODE) {

            if (data == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            } else {
                imgview.visible(true)
                check.visible(true)
                uri = data.data
                imgview.setImageURI(uri)
                counter = 2

            }
        }
    }

    companion object {
        private val IMG_CODE = 1001
        private val PERMISSION_CODE = 1000
    }

    private fun dispatchTakePictureIntent() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }

            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.CAMERA), 123
                )

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun check() {
        val img: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap)
        val checker: FirebaseVisionTextDetector = FirebaseVision.getInstance().visionTextDetector
        checker.detectInImage(img).addOnSuccessListener(object : OnSuccessListener<FirebaseVisionText> {
            override fun onSuccess(firebaseVisionText: FirebaseVisionText?) {
                processText(firebaseVisionText)
            }

        }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(p0: java.lang.Exception) {

            }

        })
    }

    private fun checkURI() {
        val img: FirebaseVisionImage = FirebaseVisionImage.fromFilePath(this, uri!!)
        val checker: FirebaseVisionTextDetector = FirebaseVision.getInstance().visionTextDetector
        checker.detectInImage(img).addOnSuccessListener(object : OnSuccessListener<FirebaseVisionText> {
            override fun onSuccess(firebaseVisionText: FirebaseVisionText?) {
                processText(firebaseVisionText)
            }

        }).addOnFailureListener { }
    }

    fun processText(text: FirebaseVisionText?) {
        val blocks: MutableList<FirebaseVisionText.Block> = text!!.blocks

        if (blocks.size == 0) {
            Toast.makeText(this, "No Text Found :) ", Toast.LENGTH_LONG).show()
            return
        }
        val builder = StringBuilder()
        for (block: FirebaseVisionText.Block in text.blocks) {
            val txt = block.text
            builder.append(txt)
            builder.append("\n")
            }

            val intent = Intent(this, TextActivity::class.java)
            intent.putExtra("text", builder.toString())
            startActivity(intent)
        }

    fun View.visible(view: Boolean) {
        visibility = if (view) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    fun gallery() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE
                    )

                } else {
                    pickpic()
                }

    }

    fun pickpic() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMG_CODE)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }

        } else {
            }

        if (requestCode == PERMISSION_CODE && grantResults.isNotEmpty()) if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMG_CODE)
        }
    }
}