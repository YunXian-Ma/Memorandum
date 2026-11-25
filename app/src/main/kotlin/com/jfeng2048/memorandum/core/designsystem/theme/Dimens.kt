package com.jfeng2048.memorandum.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** 8dp 栅格间距 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

/** 圆角风格：输入框 12、卡片 16、标签胶囊 */
object Radius {
    val input = 12.dp
    val card = 16.dp
    val panel = 10.dp
    val tag = 999.dp
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** 保证最小 48dp 可点击热区 */
object TouchTarget {
    val min = 48.dp
    val fab = 56.dp
    val topBarHeight = 52.dp
}
