package com.jfeng2048.memorandum.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfeng2048.memorandum.core.designsystem.component.PersonAvatar
import com.jfeng2048.memorandum.core.designsystem.theme.ChevronGrey
import com.jfeng2048.memorandum.core.designsystem.theme.Hairline
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.TextSecondary
import com.jfeng2048.memorandum.core.designsystem.theme.relationshipTint
import com.jfeng2048.memorandum.core.util.DateFormats
import com.jfeng2048.memorandum.feature.home.PersonCardUi

/** 首页人物行：关系色头像 + 姓名 + 一行简介 + 关系/词条数/更新时间 */
@Composable
fun PersonRow(
    person: PersonCardUi,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = relationshipTint(person.relationship)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonAvatar(
                name = person.name,
                relationship = person.relationship,
                size = 44.dp,
            )
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = person.bio.ifBlank { "还没有写简介" },
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = person.relationship.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tint.foreground,
                    )
                    Text(
                        text = " · ${person.entryCount} 条 · ${DateFormats.formatUpdated(person.updatedAt)}",
                        fontSize = 11.sp,
                        color = Muted,
                    )
                }
            }
            Text(
                text = "›",
                fontSize = 18.sp,
                color = ChevronGrey,
            )
        }
        if (showDivider) {
            HorizontalDivider(color = Hairline)
        }
    }
}
