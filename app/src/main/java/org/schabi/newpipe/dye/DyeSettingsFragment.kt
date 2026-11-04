/*
 * DyeTube: settings screen for colors and animations.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.dye

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import org.schabi.newpipe.settings.BasePreferenceFragment
import org.schabi.newpipe.util.KEY_THEME_CHANGE

class DyeSettingsFragment : BasePreferenceFragment() {

    /** Changing these needs the screens rebuilt with the new colors, like a theme change. */
    private val restyleKeys = setOf(Dye.KEY_ACCENT, Dye.KEY_GRADIENT_TOOLBAR, Dye.KEY_THUMB_RADIUS)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key in restyleKeys) {
            prefs.edit().putBoolean(KEY_THEME_CHANGE, true).apply()
            activity?.let { ActivityCompat.recreate(it) }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResourceRegistry()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(listener)
        super.onPause()
    }
}
