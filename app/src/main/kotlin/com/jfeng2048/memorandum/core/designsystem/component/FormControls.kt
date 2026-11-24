package com.jfeng2048.memorandum.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfeng2048.memorandum.core.designsystem.theme.BrandBlue
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.ChevronGrey
import com.jfeng2048.memorandum.core.designsystem.theme.DisabledGrey
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.SurfaceMuted
import com.jfeng2048.memorandum.core.designsystem.theme.TextSecondary
import com.jfeng2048.memorandum.core.designsystem.theme.TextTertiary
import com.jfeng2048.memorandum.core.designsystem.theme.TrackGrey
import com.jfeng2048.memorandum.core.designsystem.theme.categoryColor
import com.jfeng2048.memorandum.core.designsystem.theme.relationshipTint
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Relationship

/** 返回 + 左侧说明 + 右侧动作，仿原型里的极简导航条 */
@Composable
fun SimpleTopBar(
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 2.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "‹",
                fontSize = 24.sp,
                color = BrandBlue,
                modifier = Modifier.offset(y = (-1).dp),
            )
            Spacer(Modifier.width(2.dp))
            Text(text = backLabel, fontSize = 16.sp, color = BrandBlue)
        }
        Spacer(Modifier.weight(1f))
        actions()
    }
}

/** 顶栏右侧的文字动作，禁用时变浅灰 */
@Composable
fun TopBarAction(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = if (enabled) BrandBlue else ChevronGrey,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
    )
}

/** 分区小标题，例如「姓名 · 必填」 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        color = Muted,
    )
}

/** 灰底无边框输入框，接近原型里的分组输入 */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
    ),
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = TextStyle(
            fontSize = 16.sp,
            lineHeight = 26.sp,
            color = Ink,
        ),
        cursorBrush = SolidColor(BrandBlue),
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceMuted, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, fontSize = 16.sp, color = Muted)
                }
                innerTextField()
            }
        },
    )
}

/** 底部主按钮 */
@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) BrandBlue else DisabledGrey)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** 性别三段选择：白底滑块 + 灰轨道 */
@Composable
fun GenderSegmentedControl(
    selected: Gender,
    onSelect: (Gender) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TrackGrey, RoundedCornerShape(10.dp))
            .padding(3.dp),
    ) {
        Gender.entries.forEach { gender ->
            val active = gender == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (active) Modifier.shadow(1.dp, RoundedCornerShape(8.dp)) else Modifier,
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) Canvas else Color.Transparent)
                    .clickable { onSelect(gender) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = gender.label,
                    fontSize = 13.5.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (active) Ink else TextSecondary,
                )
            }
        }
    }
}

/** 关系选择胶囊：直接使用关系色 */
@Composable
fun RelationshipChip(
    relationship: Relationship,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = relationshipTint(relationship)
    val contentAlpha = if (selected) 1f else 0.62f
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(tint.background.copy(alpha = contentAlpha))
            .then(
                if (selected) {
                    Modifier.border(1.5.dp, tint.foreground, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = relationship.label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = tint.foreground.copy(alpha = contentAlpha),
        )
    }
}

/** 分类选择胶囊：选中哪个分类，就用哪个分类的颜色 */
@Composable
fun CategoryChip(
    category: EntryCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = categoryColor(category)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) color.copy(alpha = 0.10f) else TrackGrey)
            .then(
                if (selected) Modifier.border(1.5.dp, color, CircleShape) else Modifier,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryDot(color = color, size = 5.dp, alpha = if (selected) 1f else 0.5f)
        Spacer(Modifier.width(5.dp))
        Text(
            text = category.label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) color else TextTertiary,
        )
    }
}
