/*
 * DyeTube: launch animation, drawn in code so it stays sharp on any screen.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.dye

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 1 s including the fade-out: three drops of dye (purple, pink, orange) fly into the middle, the
 * gradient play triangle springs out of them with a turn, colored rings spread and the app fades in.
 * A tap skips it.
 */
class DyeLaunchAnimation private constructor(context: Context) : View(context) {

    companion object {
        private const val DURATION_MS = 800L
        private const val FADE_MS = 200L
        private const val BACKGROUND = 0xFF141217.toInt() // same as the splash screen

        @JvmStatic
        fun show(activity: Activity) {
            if (!Dye.launchAnimation(activity)) {
                return
            }
            val decor = activity.window?.decorView as? ViewGroup ?: return
            val view = DyeLaunchAnimation(activity)
            decor.addView(
                view,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            view.start()
        }

        private fun clamp(x: Float) = max(0f, min(1f, x))
        private fun range(t: Float, from: Float, to: Float) = clamp((t - from) / (to - from))
        private fun easeOut(x: Float) = 1f - (1f - x) * (1f - x) * (1f - x)
        private fun easeIn(x: Float) = x * x
        private fun overshoot(x: Float): Float {
            val s = 2.4f
            val y = x - 1f
            return y * y * ((s + 1f) * y + s) + 1f
        }
    }

    private val dropPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val trianglePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val triangle = Path()
    private var builtFor = -1f
    private var t = 0f // seconds since start
    private var animator: ValueAnimator? = null
    private var finishing = false

    init {
        isClickable = true // the app under it is not touched while the logo plays
        setOnClickListener { finish() }
    }

    private fun start() {
        animator = ValueAnimator.ofFloat(0f, DURATION_MS / 1000f).apply {
            duration = DURATION_MS
            interpolator = LinearInterpolator()
            addUpdateListener {
                t = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = finish()
            })
            start()
        }
    }

    private fun finish() {
        if (finishing) {
            return
        }
        finishing = true
        animator?.removeAllListeners()
        animator?.cancel()
        animate().alpha(0f).scaleX(1.08f).scaleY(1.08f).setDuration(FADE_MS).withEndAction {
            (parent as? ViewGroup)?.removeView(this)
        }.start()
    }

    /** Rounded play triangle centered at (0, 0) with the given height, pointing right like the logo. */
    private fun buildTriangle(height: Float) {
        val w = height * 0.866f
        // Corners of the logo: top left, tip on the right, bottom left; centered on the triangle's middle
        val pts = arrayOf(
            floatArrayOf(-w / 3f, -height / 2f),
            floatArrayOf(w * 2f / 3f, 0f),
            floatArrayOf(-w / 3f, height / 2f)
        )
        val round = height * 0.13f
        triangle.reset()
        for (i in pts.indices) {
            val p = pts[(i + pts.size - 1) % pts.size]
            val v = pts[i]
            val n = pts[(i + 1) % pts.size]
            val a = toward(v, p, round)
            val b = toward(v, n, round)
            if (i == 0) triangle.moveTo(a[0], a[1]) else triangle.lineTo(a[0], a[1])
            triangle.quadTo(v[0], v[1], b[0], b[1])
        }
        triangle.close()
        trianglePaint.shader = LinearGradient(
            -w / 3f, 0f, w * 2f / 3f, 0f, Dye.BRAND_GRADIENT, null, Shader.TileMode.CLAMP
        )
        builtFor = height
    }

    private fun toward(from: FloatArray, to: FloatArray, dist: Float): FloatArray {
        val dx = to[0] - from[0]
        val dy = to[1] - from[1]
        val len = sqrt(dx * dx + dy * dy)
        return floatArrayOf(from[0] + dx / len * dist, from[1] + dy / len * dist)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val size = w * 0.3f
        canvas.drawColor(BACKGROUND)

        // 0.00-0.32 s: three drops fly in from three sides and shrink into the middle
        val gather = range(t, 0f, 0.32f)
        if (gather < 1f) {
            for (k in 0 until 3) {
                val angle = -PI / 2 + k * 2 * PI / 3
                val dist = w * 0.42f * (1f - easeIn(gather))
                val x = cx + (cos(angle) * dist).toFloat()
                val y = cy + (sin(angle) * dist).toFloat()
                dropPaint.color = Dye.BRAND_GRADIENT[k]
                dropPaint.alpha = (255 * (1f - gather * 0.3f)).toInt()
                canvas.drawCircle(x, y, w * 0.05f * (1f - gather * 0.6f), dropPaint)
            }
        }

        // 0.30-0.80 s: rings in the three colors spread from the middle
        for (k in 0 until 3) {
            val p = range(t, 0.30f + k * 0.08f, 0.80f)
            if (p <= 0f || p >= 1f) {
                continue
            }
            ringPaint.color = Dye.BRAND_GRADIENT[k]
            ringPaint.strokeWidth = w * 0.01f * (1f - p) + 1f
            ringPaint.alpha = (220 * (1f - p)).toInt()
            canvas.drawCircle(cx, cy, size * (0.5f + 1.2f * easeOut(p)), ringPaint)
        }

        // 0.28-0.62 s: the triangle springs out with a turn; 0.62-0.80 s: a small "play" nudge to the right
        val pop = range(t, 0.28f, 0.62f)
        if (pop > 0f) {
            if (builtFor != size) {
                buildTriangle(size)
            }
            val scale = overshoot(pop)
            val nudge = range(t, 0.62f, 0.80f)
            val shift = size * 0.08f * sin(nudge * PI).toFloat()
            canvas.save()
            canvas.translate(cx + shift, cy)
            canvas.rotate(-120f * (1f - easeOut(pop)))
            canvas.scale(scale, scale)
            canvas.drawPath(triangle, trianglePaint)
            canvas.restore()
        }
    }
}
