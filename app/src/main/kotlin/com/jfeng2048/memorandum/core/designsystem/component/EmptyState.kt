package com.jfeng2048.memorandum.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.CardBorder
import com.jfeng2048.memorandum.core.designsystem.theme.DividerSoft
import com.jfeng2048.memorandum.core.designsystem.theme.Hairline
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.PlaceholderLine
import com.jfeng2048.memorandum.core.designsystem.theme.SurfaceMuted

/**
 * 空状态：三张叠起来的旧卡片，呼应「抽到一张旧卡片」的原型气质。
 */
@Composable
fun EmptyPeopleState(
    filtered: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(width = 72.dp, height = 56.dp)) {
            Box(
                Modifier
                    .offset(x = 6.dp, y = 2.dp)
                    .size(width = 50.dp, height = 32.dp)
                    .rotate(-7f)
                    .background(DividerSoft, RoundedCornerShape(6.dp)),
            )
            Box(
                Modifier
                    .offset(x = 16.dp, y = 7.dp)
                    .size(width = 50.dp, height = 32.dp)
                    .rotate(5f)
                    .background(SurfaceMuted, RoundedCornerShape(6.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(6.dp)),
            )
            Box(
                Modifier
                    .offset(x = 11.dp, y = 13.dp)
                    .size(width = 52.dp, height = 36.dp)
                    .background(Canvas, RoundedCornerShape(7.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(7.dp)),
            ) {
                Column(
                    Modifier.padding(start = 10.dp, top = 11.dp),
                ) {
                    Box(
                        Modifier
                            .size(width = 26.dp, height = 2.dp)
                            .background(PlaceholderLine, RoundedCornerShape(2.dp)),
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        Modifier
                            .size(width = 18.dp, height = 2.dp)
                            .background(DividerSoft, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = if (filtered) "这个人还没被记下来" else "还没有记下任何人",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = if (filtered) {
                "换个名字试试，或者从右下角新建一个。"
            } else {
                "从右下角的 ＋ 开始，记下第一个想记住的人。"
            },
            fontSize = 12.5.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
        )
    }
}
