package com.jfeng2048.memorandum.core.util

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFormatsTest {

    private fun time(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.timeInMillis

    @Test
    fun `同一天显示今天`() {
        val now = time(2026, 11, 24, 12)
        assertEquals("今天", DateFormats.formatUpdated(time(2026, 11, 24, 9), now))
    }

    @Test
    fun `前一天显示昨天`() {
        val now = time(2026, 11, 24, 12)
        assertEquals("昨天", DateFormats.formatUpdated(time(2026, 11, 23, 12), now))
    }

    @Test
    fun `同年只显示月日`() {
        val now = time(2026, 11, 24, 12)
        assertEquals("11.20", DateFormats.formatShort(time(2026, 11, 20), now))
    }

    @Test
    fun `跨年显示完整日期`() {
        val now = time(2026, 11, 24, 12)
        assertEquals("2025.12.31", DateFormats.formatShort(time(2025, 12, 31), now))
    }

    @Test
    fun `建档日期显示完整年月日`() {
        assertEquals("2026.03.12", DateFormats.formatFull(time(2026, 3, 12)))
    }
}
