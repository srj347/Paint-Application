package com.example.imageannotationrd

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

class ImageEditorView(
    context: Context,
    attributes: AttributeSet? = null
) : View(context, attributes) {
    companion object{
        var pathList = ArrayList<Path>()
        var colorList = ArrayList<Int>()
        var currentPaintColor = R.color.black
    }
    private var path = Path()
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val STROKE_WIDTH = 10f

    private val paint = Paint().apply {
        color = currentPaintColor
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
//        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas!!.drawBitmap(extraBitmap, 0f, 0f, null)
        /*for(i in pathList.indices){
            paintBrush.color = colorList[i]
            canvas!!.drawPath(pathList[i], paintBrush)
            invalidate()
        }*/
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY

            // Draw the path in the extra bitmap to save it.
            pathList.add(path)
            colorList.add(currentPaintColor)
            extraCanvas.drawPath(path, paint)
        }
        // Invalidate() is inside the touchMove() under ACTION_MOVE because there are many other
        // types of motion events passed into this listener, and we don't want to invalidate the
        // view for those.
        invalidate()
    }

    private fun touchUp() {
        // Reset the path so it doesn't get drawn again.
        path.reset()
        Log.d("ImageDebugg", "size of pathlist: ${pathList.size}")
    }

    fun undo(){
        if (pathList.isNotEmpty()) {
            pathList.removeAt(pathList.size - 1)
            colorList.removeAt(colorList.size - 1)
            extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            extraCanvas = Canvas(extraBitmap)
            for (i in pathList.indices) {
                paint.color = colorList[i]
                extraCanvas.drawPath(pathList[i], paint)
            }
            invalidate()
        }
    }

}