package com.jfeng2048.memorandum.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfeng2048.memorandum.core.designsystem.theme.relationshipTint
import com.jfeng2048.memorandum.domain.model.Relationship

/**
 * 关系色头像：取姓名首字，底色与文字色跟着关系走。
 * ring = true 时加一圈白边 + 关系色外环（详情页用）。
 */
@Composable
fun PersonAvatar(
    name: String,
    relationship: Relationship,
    size: Dp,
    modifier: Modifier = Modifier,
    ring: Boolean = false,
) {
    val tint = relationshipTint(relationship)
    val initial = name.trim().firstOrNull()?.toString() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (ring) {
                    Modifier
                        .background(tint.foreground.copy(alpha = 0.16f), CircleShape)
                        .padding(3.5.dp)
                } else {
                    Modifier
                },
            )
            .clip(CircleShape)
            .background(tint.background)
            .then(if (ring) Modifier.border(2.dp, Color.White, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = tint.foreground,
            fontSize = (size.value * 0.38f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
