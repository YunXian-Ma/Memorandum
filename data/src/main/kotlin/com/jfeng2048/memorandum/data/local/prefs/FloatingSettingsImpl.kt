package com.jfeng2048.memorandum.data.local.prefs

import com.jfeng2048.memorandum.domain.repository.FloatingSettings
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloatingSettingsImpl @Inject constructor(
    private val preferences: AppPreferences,
) : FloatingSettings {
    override val enabledFlow: StateFlow<Boolean> = preferences.floatingEnabledFlow
    override val themeIdFlow: StateFlow<String> = preferences.floatingThemeIdFlow

    override var enabled: Boolean
        get() = preferences.floatingAssistantEnabled
        set(value) { preferences.floatingAssistantEnabled = value }

    override var themeId: String
        get() = preferences.floatingThemeId
        set(value) { preferences.floatingThemeId = value }

    override var ballIsLeft: Boolean
        get() = preferences.floatingBallIsLeft
        set(value) { preferences.floatingBallIsLeft = value }

    override var ballYRatio: Float
        get() = preferences.floatingBallYRatio
        set(value) { preferences.floatingBallYRatio = value }
}
