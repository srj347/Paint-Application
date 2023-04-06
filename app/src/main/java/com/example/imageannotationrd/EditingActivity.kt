package com.example.imageannotationrd

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.FileInputStream


class EditingActivity : AppCompatActivity() {
    lateinit var undoBtn: ImageButton
    lateinit var redoBtn: ImageButton
    lateinit var drawBtn: ImageButton
    lateinit var saveBtn: ImageButton
    lateinit var cancelBtn: TextView
    lateinit var colorPickerBtn: ImageButton

    lateinit var drawingView: DrawingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        undoBtn = findViewById(R.id.undo_img_btn)
        redoBtn = findViewById(R.id.redo_img_btn)
        drawBtn = findViewById(R.id.draw_img_btn)
        saveBtn = findViewById(R.id.save_img_btn)
        cancelBtn = findViewById(R.id.cancel_textview)
        colorPickerBtn = findViewById(R.id.colorpicker_img_btn)
        drawingView = findViewById(R.id.paint_view)


        drawingView.setBackgroundBitmap(getImageBitmapFromIntent())

        saveBtn.setOnClickListener{
            drawingView.saveDrawingToDisk(drawingView.width, drawingView.height)
        }
        undoBtn.setOnClickListener{
            drawingView.undo()
        }
        redoBtn.setOnClickListener{
            drawingView.redo()
        }
        colorPickerBtn.setOnClickListener{
            drawingView.showColorPicker()
        }
        drawBtn.setOnClickListener{
            // enable drawing on the DrawingView
            drawingView.enableDrawing()
        }
        cancelBtn.setOnClickListener{
            finish()
        }
    }


    private fun getImageBitmapFromIntent(): Bitmap{
        var bitmap: Bitmap? = null
        val filename = intent.getStringExtra("selected_image")
        try {
            val `is`: FileInputStream = openFileInput(filename)
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap!!
    }
}