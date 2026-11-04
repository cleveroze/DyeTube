/*
 * DyeTube: settings and theming that make the app "dyed" to your taste.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.dye

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceManager
import org.schabi.newpipe.App
import org.schabi.newpipe.R
import org.schabi.newpipe.util.ThemeHelper

/** DyeTube preferences and the look they produce. */
object Dye {

    const val KEY_ACCENT = "dye_accent"
    const val KEY_GRADIENT_TOOLBAR = "dye_gradient_toolbar"
    const val KEY_MOTION = "dye_motion"
    const val KEY_TOUCH_RIPPLES = "dye_touch_ripples"
    const val KEY_LAUNCH_ANIMATION = "dye_launch_animation"
    const val KEY_THUMB_RADIUS = "dye_thumb_radius"

    /** Accent id -> light and dark toolbar colors, and the theme overlays that carry them. */
    class Accent(val id: String, val light: Int, val dark: Int, val lightStyle: Int, val darkStyle: Int)

    val ACCENTS = listOf(
        Accent("dye", 0xFFE0307F.toInt(), 0xFF8C2A62.toInt(), R.style.DyeAccent_Dye_Light, R.style.DyeAccent_Dye_Dark),
        Accent("violet", 0xFF9C3FD1.toInt(), 0xFF5E2A80.toInt(), R.style.DyeAccent_Violet_Light, R.style.DyeAccent_Violet_Dark),
        Accent("sunset", 0xFFF4583C.toInt(), 0xFF933524.toInt(), R.style.DyeAccent_Sunset_Light, R.style.DyeAccent_Sunset_Dark),
        Accent("ocean", 0xFF2F7DE1.toInt(), 0xFF1F4E8C.toInt(), R.style.DyeAccent_Ocean_Light, R.style.DyeAccent_Ocean_Dark),
        Accent("mint", 0xFF19A97B.toInt(), 0xFF136B4F.toInt(), R.style.DyeAccent_Mint_Light, R.style.DyeAccent_Mint_Dark),
        Accent("ruby", 0xFFE53935.toInt(), 0xFF992722.toInt(), R.style.DyeAccent_Ruby_Light, R.style.DyeAccent_Ruby_Dark),
        Accent("gold", 0xFFD9A21B.toInt(), 0xFF8A6611.toInt(), R.style.DyeAccent_Gold_Light, R.style.DyeAccent_Gold_Dark),
        Accent("graphite", 0xFF455A64.toInt(), 0xFF263238.toInt(), R.style.DyeAccent_Graphite_Light, R.style.DyeAccent_Graphite_Dark),
    )

    /** The logo gradient: purple, pink, orange. */
    val BRAND_GRADIENT = intArrayOf(0xFFB03CCF.toInt(), 0xFFF5368F.toInt(), 0xFFFF5A4E.toInt())

    private fun prefs(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @JvmStatic
    fun accent(context: Context): Accent {
        val id = prefs(context).getString(KEY_ACCENT, "dye")
        return ACCENTS.firstOrNull { it.id == id } ?: ACCENTS[0]
    }

    @JvmStatic
    fun motion(context: Context) = prefs(context).getBoolean(KEY_MOTION, true)

    @JvmStatic
    fun touchRipples(context: Context) = motion(context) && prefs(context).getBoolean(KEY_TOUCH_RIPPLES, true)

    @JvmStatic
    fun launchAnimation(context: Context) = prefs(context).getBoolean(KEY_LAUNCH_ANIMATION, true)

    @JvmStatic
    fun thumbRadiusDp(context: Context) =
        prefs(context).getString(KEY_THUMB_RADIUS, "12")?.toIntOrNull() ?: 12

    /** Screen change animations: rise and spring with animations on, NewPipe's plain fade otherwise. */
    @JvmStatic
    fun screenIn(): Int =
        if (motion(App.instance)) R.animator.dye_screen_in else R.animator.custom_fade_in

    @JvmStatic
    fun screenOut(): Int =
        if (motion(App.instance)) R.animator.dye_screen_out else R.animator.custom_fade_out

    /** Called right after ThemeHelper sets the base theme: recolors toolbars, status bar and switches. */
    @JvmStatic
    fun applyAccent(context: Context) {
        val accent = accent(context)
        val style = if (ThemeHelper.isLightThemeSelected(context)) accent.lightStyle else accent.darkStyle
        context.theme.applyStyle(style, true)
    }

    /** The main toolbar gets the accent as a soft gradient (the logo colors for the default accent). */
    @JvmStatic
    fun decorateToolbar(activity: Activity, toolbar: Toolbar?) {
        if (toolbar == null || !prefs(activity).getBoolean(KEY_GRADIENT_TOOLBAR, true)) {
            return
        }
        val accent = accent(activity)
        val base = if (ThemeHelper.isLightThemeSelected(activity)) accent.light else accent.dark
        val colors = if (accent.id == "dye") {
            if (ThemeHelper.isLightThemeSelected(activity)) BRAND_GRADIENT
            else BRAND_GRADIENT.map { ColorUtils.blendARGB(it, Color.BLACK, 0.4f) }.toIntArray()
        } else {
            intArrayOf(ColorUtils.blendARGB(base, Color.WHITE, 0.18f), base, ColorUtils.blendARGB(base, Color.BLACK, 0.18f))
        }
        toolbar.background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
    }

    /** Rounds the thumbnail inside a list item, if it has one. */
    @JvmStatic
    fun roundThumbnailIn(item: View?) = roundThumbnail(item?.findViewById(R.id.itemThumbnailView))

    /** Rounds the corners of a thumbnail by the user's chosen radius. */
    @JvmStatic
    fun roundThumbnail(view: View?) {
        if (view == null) {
            return
        }
        val radius = thumbRadiusDp(view.context) * view.resources.displayMetrics.density
        if (radius <= 0f) {
            return
        }
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(v: View, outline: Outline) {
                outline.setRoundRect(0, 0, v.width, v.height, radius)
            }
        }
        view.clipToOutline = true
    }
}
