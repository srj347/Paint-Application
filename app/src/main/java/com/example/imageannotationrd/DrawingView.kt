package com.example.imageannotationrd

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

open class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

//    private var redoStack: Stack<Path> = Stack()
//    private var undoStack: Stack<Path> = Stack()
//    private var drawingPath: Path = Path()
//    private var drawingPaint: Paint = Paint()

    private var undoStack: Stack<Pair<Path, Paint>> = Stack()
    private var redoStack: Stack<Pair<Path, Paint>> = Stack()
    private var drawingPath: Path? = null
    private var drawingPaint: Paint? = null

    private var backgroundImage: Bitmap? = null
    private var currentBitmap: Bitmap? = null



    private var pathPaintMap: MutableMap<Path, Paint> = mutableMapOf()

    private var currentColor: Int = Color.BLACK
    private var isDrawingEnabled: Boolean = false

    init {
        drawingPaint = createPaint(currentColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        backgroundImage?.let {
            val centreX = (width  - backgroundImage!!.width) /2
            val centreY = (height - backgroundImage!!.height) /2

            canvas.drawBitmap(it, centreX.toFloat(), centreY.toFloat(), null)
        }

        for (pair in undoStack) {
            canvas.drawPath(pair.first, pair.second)
        }
        drawingPath?.let {
            canvas.drawPath(it, drawingPaint!!)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDrawingEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    touchMove(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    touchEnd(event.x, event.y)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun touchStart(x: Float, y: Float){
        drawingPath = Path()
        drawingPath!!.moveTo(x, y)
        drawingPaint = createPaint(currentColor)
//        pathPaintMap[drawingPath!!] = drawingPaint!!
        invalidate()
    }

    private fun touchMove(x: Float, y: Float){
        drawingPath?.let {
            it.lineTo(x, y)
            invalidate()
        }
    }

    private fun touchEnd(x: Float, y: Float){
        drawingPath?.let {
            it.lineTo(x, y)
            pathPaintMap[it] = Paint(drawingPaint)
            undoStack.push(Pair(Path(it), Paint(drawingPaint)))
            redoStack.clear()
            drawingPath = null
            invalidate()
        }
    }

    fun undo() {
        if (!undoStack.isEmpty()) {
            val pathPaintPair = undoStack.pop()
            redoStack.push(Pair(Path(pathPaintPair.first), Paint(drawingPaint)))
            drawingPaint = Paint(pathPaintPair.second)
            invalidate()
        }
    }

    fun redo() {
        if (!redoStack.isEmpty()) {
            val pathPaintPair = redoStack.pop()
            undoStack.push(Pair(Path(pathPaintPair.first), Paint(drawingPaint)))
            drawingPaint = Paint(pathPaintPair.second)
            invalidate()
        }
    }


    fun showColorPicker() {
        val colors = arrayOf(
            ContextCompat.getColor(context, R.color.color_red),
            ContextCompat.getColor(context, R.color.color_blue),
            ContextCompat.getColor(context, R.color.color_green),
            ContextCompat.getColor(context, R.color.color_yellow),
            ContextCompat.getColor(context, R.color.color_black),
            ContextCompat.getColor(context, R.color.color_gray)
        )

        val colorNames = arrayOf(
            "Red",
            "Blue",
            "Green",
            "Yellow",
            "Black",
            "Gray"
        )

        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Choose a color")
        builder.setItems(colorNames) { _, which ->
            setColor(colors[which])
        }
        builder.show()
        invalidate()
    }

    private fun createPaint(color: Int): Paint {
        val paint = Paint()
        paint.color = color
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        return paint
    }

    fun setColor(color: Int) {
        currentColor = color
        drawingPaint = createPaint(currentColor)
    }

    fun setBackgroundBitmap(backgroundBitmap: Bitmap){
        backgroundImage = backgroundBitmap
    }

    fun enableDrawing(){
        isDrawingEnabled = true
    }
    fun disableDrawing(){
        isDrawingEnabled = false
    }

    fun saveDrawingToDisk(width: Int, height: Int, ){
        // create a bitmap of the current view
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)

        // create a file to save the bitmap to
        val file = createImageFile()
        try {
            // write the bitmap data to the file
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            // show a success message
            Toast.makeText(context, "Image saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // show an error message
            Log.e("DrawingView", "Error saving drawing", e)
            Toast.makeText(context, "Error saving drawing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "Drawing_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        )
    }
}