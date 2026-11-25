package com.jfeng2048.memorandum.data.local.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 极轻量的本地标记。
 * 只用来记录「演示数据是否已经写入过」，不涉及账号或隐私信息，
 * 因此用 SharedPreferences 而不是 DataStore。
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences =
        context.getSharedPreferences("memorandum_prefs", Context.MODE_PRIVATE)

    var isSeeded: Boolean
        get() = preferences.getBoolean(KEY_SEEDED, false)
        set(value) = preferences.edit().putBoolean(KEY_SEEDED, value).apply()

    private companion object {
        /**
         * 带版本号：数据库结构升级（fallbackToDestructiveMigration 会清库）后，
         * 更换 key 等同于把标记重置为 false，保证默认关系与演示数据重新写入。
         */
        const val KEY_SEEDED = "seed_done_v4"
    }
}
