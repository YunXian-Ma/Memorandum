package com.jfeng2048.memorandum.feature.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfeng2048.memorandum.core.designsystem.component.CategoryDot
import com.jfeng2048.memorandum.core.designsystem.theme.BrandBlue
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.DangerRed
import com.jfeng2048.memorandum.core.designsystem.theme.Hairline
import com.jfeng2048.memorandum.core.designsystem.theme.HandleGrey
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.TextCardBody
import com.jfeng2048.memorandum.core.designsystem.theme.categoryColor
import com.jfeng2048.memorandum.core.util.DateFormats
import com.jfeng2048.memorandum.domain.model.PersonEntry

/**
 * 词条详情：不是新页面，而是一张浮起来的卡片（与原型一致）。
 * 点击遮罩或「完成」关闭，编辑 / 删除入口都在卡片底部。
 */
@Composable
fun EntryCardOverlay(
    entry: PersonEntry?,
    onDismiss: () -> Unit,
    onEdit: (PersonEntry) -> Unit,
    onDelete: (PersonEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = entry != null,
        modifier = modifier.fillMaxSize(),
        enter = fadeIn(tween(160)) + scaleIn(tween(240), initialScale = 0.955f),
        exit = fadeOut(tween(120)),
    ) {
        val current = entry ?: return@AnimatedVisibility
        val color = categoryColor(current.category)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(24.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Canvas)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 34.dp, height = 3.dp)
                        .clip(CircleShape)
                        .background(HandleGrey),
                )
                Spacer(Modifier.height(15.dp))

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryDot(color = color, size = 6.dp)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = current.category.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = color,
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = current.title,
                    fontSize = 21.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )

                if (current.content.isNotBlank()) {
                    Spacer(Modifier.height(9.dp))
                    Text(
                        text = current.content,
                        fontSize = 14.5.sp,
                        lineHeight = 26.sp,
                        color = TextCardBody,
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "建档 ${DateFormats.formatFull(current.createdAt)}  →  更新 ${
                        DateFormats.formatShort(current.updatedAt)
                    }",
                    fontSize = 10.5.sp,
                    letterSpacing = 0.3.sp,
                    color = Muted,
                )

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Hairline)
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CardAction(text = "编辑", color = BrandBlue) { onEdit(current) }
                    Spacer(Modifier.width(20.dp))
                    CardAction(text = "删除", color = DangerRed) { onDelete(current) }
                    Spacer(Modifier.weight(1f))
                    CardAction(text = "完成", color = Muted, onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun CardAction(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 8.dp),
    )
}
