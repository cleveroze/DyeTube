/*
 * DyeTube: animations everywhere.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.dye

import android.animation.AnimatorInflater
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.LayoutAnimationController
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.RecyclerView
import org.schabi.newpipe.R

/**
 * Colored rings where you tap, cards that flow in and spring when pressed. Everything is off when
 * the "Animations" switch is off.
 */
object DyeMotion {

    // ------------------------------------------------------------ rings on tap

    /** Called from MainActivity.dispatchTouchEvent; never consumes the touch. */
    @JvmStatic
    fun onTouch(activity: Activity, ev: MotionEvent) {
        val action = ev.actionMasked
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN) {
            return
        }
        if (!Dye.touchRipples(activity)) {
            return
        }
        val decor = activity.window?.decorView as? ViewGroup ?: return
        var layer = decor.findViewWithTag<RippleLayer>(RippleLayer.TAG)
        if (layer == null) {
            layer = RippleLayer(activity)
            decor.addView(
                layer,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        } else if (decor.getChildAt(decor.childCount - 1) !== layer) {
            layer.bringToFront()
        }
        // Activity touch coordinates are relative to the window, the same as the decor view
        val index = ev.actionIndex
        layer.add(ev.getX(index), ev.getY(index))
    }

    private class RippleLayer(context: Context) : View(context) {

        companion object {
            const val TAG = "dye_ripples"
            const val DURATION_MS = 520f
        }

        private val ripples = ArrayList<FloatArray>() // x, y, start time
        private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val dot = Paint(Paint.ANTI_ALIAS_FLAG)
        private val density = resources.displayMetrics.density

        init {
            tag = TAG
            isClickable = false
            isFocusable = false
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        override fun onTouchEvent(event: MotionEvent) = false // the touch goes on to the app under it

        fun add(x: Float, y: Float) {
            if (ripples.size > 8) {
                ripples.removeAt(0)
            }
            ripples.add(floatArrayOf(x, y, SystemClock.uptimeMillis().toFloat()))
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            if (ripples.isEmpty()) {
                return
            }
            val now = SystemClock.uptimeMillis().toFloat()
            val it = ripples.iterator()
            while (it.hasNext()) {
                val r = it.next()
                val p = (now - r[2]) / DURATION_MS
                if (p >= 1f) {
                    it.remove()
                    continue
                }
                val ease = 1f - (1f - p) * (1f - p) * (1f - p)
                // Three rings in the logo colors, one after another, like dye spreading in water
                for (k in 0 until 3) {
                    val q = ease - k * 0.18f
                    if (q <= 0f) {
                        continue
                    }
                    ring.color = Dye.BRAND_GRADIENT[k]
                    ring.strokeWidth = density * 2.4f * (1f - p) + 0.5f
                    ring.alpha = ((150 - k * 30) * (1f - p)).toInt()
                    canvas.drawCircle(r[0], r[1], density * (6f + 44f * q), ring)
                }
                if (p < 0.3f) {
                    dot.color = Dye.BRAND_GRADIENT[1]
                    dot.alpha = (110 * (1f - p / 0.3f)).toInt()
                    canvas.drawCircle(r[0], r[1], density * 6f * (1f - p), dot)
                }
            }
            if (ripples.isNotEmpty()) {
                postInvalidateOnAnimation()
            }
        }
    }

    // ------------------------------------------------------------ lists

    /** Items slide up, grow and fade in one after another when a list is first shown. */
    @JvmStatic
    fun listEntrance(list: RecyclerView?) {
        if (list == null || !Dye.motion(list.context)) {
            return
        }
        val density = list.resources.displayMetrics.density
        val set = AnimationSet(true).apply {
            addAnimation(TranslateAnimation(0f, 0f, 36f * density, 0f))
            addAnimation(ScaleAnimation(0.94f, 1f, 0.94f, 1f, 0f, 0f))
            addAnimation(AlphaAnimation(0f, 1f))
            duration = 380
            interpolator = DecelerateInterpolator(2f)
        }
        list.layoutAnimation = LayoutAnimationController(set, 0.1f)
        list.scheduleLayoutAnimation()
    }

    /** Cards shrink a little while pressed and spring back. */
    @JvmStatic
    fun pressSpring(view: View?) {
        if (view == null || !Dye.motion(view.context)) {
            return
        }
        view.stateListAnimator = AnimatorInflater.loadStateListAnimator(view.context, R.animator.dye_press)
    }
}
