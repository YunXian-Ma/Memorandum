package com.jfeng2048.memorandum.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfeng2048.memorandum.core.designsystem.theme.BrandBlue
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.DangerRed
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.SuccessGreen
import kotlinx.coroutines.delay

/** 原型里的「记下了」小药丸，2.1 秒后自动消失 */
@Composable
fun ToastHost(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(2_100)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { height -> height / 2 },
        exit = fadeOut(tween(150)),
    ) {
        Row(
            modifier = Modifier
                .shadow(12.dp, CircleShape)
                .background(Ink, CircleShape)
                .padding(start = 13.dp, end = 17.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = message.orEmpty(),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** 删除等危险操作的二次确认 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "删除",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 12.5.sp,
                lineHeight = 21.sp,
                color = Muted,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = DangerRed, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = Muted)
            }
        },
        containerColor = Canvas,
        shape = RoundedCornerShape(14.dp),
    )
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = BrandBlue,
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "读取本地数据时出了点问题",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = message,
            fontSize = 12.5.sp,
            color = Muted,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(18.dp))
        TextButton(onClick = onRetry) {
            Text(text = "重试", color = BrandBlue, fontSize = 14.sp)
        }
    }
}
