package com.example.chekersgamepro.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class TouchListener(private val click: View.OnClickListener, private val decreaseFactor: Float) : View.OnTouchListener {

    private var scaleX: PropertyValuesHolder = PropertyValuesHolder.ofFloat(View.SCALE_X, decreaseFactor)
    private var scaleY: PropertyValuesHolder = PropertyValuesHolder.ofFloat(View.SCALE_Y, decreaseFactor)

    private lateinit var rect: Rect

    private lateinit var animator: ObjectAnimator


    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean =
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> actionDown(view)
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> actionUp(view, motionEvent)
                else -> false
            }


    private fun actionDown(view: View): Boolean {
        rect = Rect(view.left, view.top, view.right, view.bottom)

        animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY)
        animator.interpolator = FastOutSlowInInterpolator()
        animator.setValues(scaleX, scaleY)

        animator.setDuration(100).start()
        return true

    }

    private fun actionUp(view: View, motionEvent: MotionEvent): Boolean {

        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)
        animator.setValues(scaleX, scaleY)
        if (motionEvent.action != MotionEvent.ACTION_CANCEL
                && rect.contains(view.left + motionEvent.x.toInt(), view.top + motionEvent.y.toInt())) {

            click.onClick(view)
        }
        animator.setDuration(100).start()

        return false

    }

    private fun actionCancel(): Boolean = false


}