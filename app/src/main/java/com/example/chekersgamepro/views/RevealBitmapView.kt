package com.example.chekersgamepro.views

import android.content.Context
import android.graphics.*
import android.graphics.Canvas.ALL_SAVE_FLAG
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.chekersgamepro.R

class RevealBitmapView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
        View(context, attrs, defStyleAttr) {

//    var revealProgress = 0f
//        set(value) {
//            if (field != value) {
//                field = value
//                invalidate()
//            }
//        }
//
//    var customReveal: ((Canvas, Paint, Float) -> Unit)? = null
//
//    var revealDrawable: Drawable? = null
//    var revealBitmap: Bitmap? = null
//    private val srcBounds = Rect()
//
//    private val maskPaint = Paint()
//    private val maskingPaint = Paint()
//    private val maskBounds = RectF()
//
//    init {
//        maskPaint.isAntiAlias = true
//        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//
//        maskingPaint.isAntiAlias = true
//        maskingPaint.style = Paint.Style.FILL
//        maskingPaint.color = 0xff424242.toInt()
//
//        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RevealBitmapView,
//                defStyleAttr, 0)
//
//        val bitmapRes = typedArray.getResourceId(R.styleable., 0)
//        if (bitmapRes != 0) {
//            ContextCompat.getDrawable(context, bitmapRes)?.toBitmap()?.let {
//                revealBitmap = it
//                srcBounds.right = it.width
//                srcBounds.bottom = it.height
//            }
//        }
//        revealProgress = 0f
//
//        typedArray.recycle()
//
//        isDrawingCacheEnabled = true
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        if (canvas == null) {
//            super.onDraw(canvas)
//            return
//        }
//
//        revealDrawable?.let {
//            if (revealBitmap == null) {
//                val asBmp = it.toBitmap()
//                revealBitmap = asBmp
//                srcBounds.right = asBmp.width
//                srcBounds.bottom = asBmp.height
//            }
//        }
//
//        revealBitmap?.let {
//            maskBounds.right = width.toFloat()
//            maskBounds.bottom = height.toFloat()
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                canvas.saveLayer(0f, 0f, maskBounds.right, maskBounds.bottom, maskingPaint)
//            } else {
//                canvas.saveLayer(0f, 0f, maskBounds.right, maskBounds.bottom, maskingPaint,
//                        ALL_SAVE_FLAG)
//            }
//
//            // Fill canvas with transparent color
//            canvas.drawColor(Color.TRANSPARENT)
//
//            // Draw the portion we want to show
//            customReveal?.invoke(canvas, maskingPaint, revealProgress)
//                    ?: canvas.drawArc(maskBounds, 0f, calculateRevealAngle(), true, maskingPaint)
//
//            // Draw the original bitmap, only filling the area that is supposed to be shown.
//            canvas.drawBitmap(it, srcBounds, maskBounds, maskPaint)
//
//            canvas.restore()
//        }
//    }
//
//    private fun calculateRevealAngle() = revealProgress * 360f
}