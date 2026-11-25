package com.jfeng2048.memorandum.feature.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import com.jfeng2048.memorandum.core.designsystem.component.CategoryDot
import com.jfeng2048.memorandum.core.designsystem.theme.ChevronGrey
import com.jfeng2048.memorandum.core.designsystem.theme.Hairline
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.TextSecondary
import com.jfeng2048.memorandum.core.designsystem.theme.categoryColor
import com.jfeng2048.memorandum.core.util.DateFormats
import com.jfeng2048.memorandum.domain.model.PersonEntry

/** 详情页词条行：分类色点 + 词条名 + 分类/更新时间 */
@Composable
fun EntryRow(
    entry: PersonEntry,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(Modifier.padding(top = 7.dp)) {
                CategoryDot(
                    color = categoryColor(entry.category),
                    size = 7.dp,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                )
                if (entry.content.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = entry.content,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${entry.category.label} · ${DateFormats.formatUpdated(entry.updatedAt)}",
                    fontSize = 11.5.sp,
                    color = Muted,
                )
            }
            Text(
                text = "›",
                fontSize = 18.sp,
                color = ChevronGrey,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(color = Hairline)
        }
    }
}
