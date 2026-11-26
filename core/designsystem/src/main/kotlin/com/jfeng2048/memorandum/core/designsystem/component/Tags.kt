package com.jfeng2048.memorandum.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.relationshipTint
import com.jfeng2048.memorandum.domain.model.Relationship

/** 关系标签：浅底 + 深字，只做分类不抢眼 */
@Composable
fun RelationshipTag(
    relationship: Relationship,
    modifier: Modifier = Modifier,
) {
    val tint = relationshipTint(relationship)
    Text(
        text = relationship.name,
        modifier = modifier
            .clip(CircleShape)
            .background(tint.background)
            .padding(horizontal = 9.dp, vertical = 2.5.dp),
        color = tint.foreground,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

/** 分类色点：整个界面里分类只以「点」的形式出现 */
@Composable
fun CategoryDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 6.dp,
    alpha: Float = 1f,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha)),
    )
}

/** 详情页顶部的分类筛选文字（全部 / 各分类） */
@Composable
fun CategoryFilterLabel(
    label: String,
    color: Color?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (color != null) {
            CategoryDot(color = color, alpha = if (selected) 1f else 0.4f)
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = if (selected) Ink else Muted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
