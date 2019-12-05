package com.coding180.image2text

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.filedialog.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class TextActivity : AppCompatActivity() {

    lateinit var txt: TextView
    lateinit var button: Button
    lateinit var filedialog: AlertDialog
    lateinit var editext: EditText
    lateinit var button2: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                this@TextActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@TextActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1000
            )
        }

        txt = findViewById(R.id.txt)
        button = findViewById(R.id.button)

        val bundle: Bundle? = intent.extras
        val getmsg = bundle!!.getString("text")
        txt.text = getmsg

        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.filedialog, null)
        builder.setView(dialogView)
        builder.setCancelable(true)
        filedialog = builder.create()
        editext = dialogView.findViewById(R.id.editext)
        button2 = dialogView.findViewById(R.id.button2)

        button.setOnClickListener {
            filedialog.show()
        }

        button2.setOnClickListener {
            val filename = editext.text.toString()
            keep(filename, getmsg!!)

            filedialog.dismiss()
        }
    }

    fun keep(filename: String?, content: String) {
            val fileName = "$filename.txt"
            val file = File(Environment.getExternalStorageDirectory().absolutePath, fileName)
            val conTent = content + txt.text.toString()
            val fos = FileOutputStream(file)
            fos.write(conTent.toByteArray())
            fos.close()
            Toast.makeText(this, "Saved Successfully in Internal Storage", Toast.LENGTH_LONG).show()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else
                    return
            }
        }
    }
}