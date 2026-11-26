package com.jfeng2048.memorandum.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 日期展示规则（与原型一致）：
 * - 今天 / 昨天用相对文案
 * - 同年只显示「11.20」，跨年才显示完整年月日
 * - 建档时间、词条卡片底部使用完整年月日
 *
 * 使用 SimpleDateFormat 而不是 java.time，是为了在 minSdk 24 上不引入脱糖依赖。
 */
object DateFormats {

    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    fun formatFull(millis: Long): String =
        SimpleDateFormat("yyyy.MM.dd", Locale.CHINA).format(Date(millis))

    fun formatShort(millis: Long, now: Long = System.currentTimeMillis()): String {
        val pattern = if (sameYear(millis, now)) "MM.dd" else "yyyy.MM.dd"
        return SimpleDateFormat(pattern, Locale.CHINA).format(Date(millis))
    }

    fun formatUpdated(millis: Long, now: Long = System.currentTimeMillis()): String = when {
        isSameDay(millis, now) -> "今天"
        isSameDay(millis, now - DAY_MILLIS) -> "昨天"
        else -> formatShort(millis, now)
    }

    fun formatRange(createdAt: Long, updatedAt: Long): String =
        "${formatFull(createdAt)} → ${formatFull(updatedAt)}"

    private fun isSameDay(first: Long, second: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = first }
        val b = Calendar.getInstance().apply { timeInMillis = second }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun sameYear(first: Long, second: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = first }
        val b = Calendar.getInstance().apply { timeInMillis = second }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
    }
}
