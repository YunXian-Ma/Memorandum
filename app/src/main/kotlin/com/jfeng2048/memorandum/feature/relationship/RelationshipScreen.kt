package com.jfeng2048.memorandum.feature.relationship

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jfeng2048.memorandum.core.designsystem.component.CategoryDot
import com.jfeng2048.memorandum.core.designsystem.component.ConfirmDialog
import com.jfeng2048.memorandum.core.designsystem.component.ErrorView
import com.jfeng2048.memorandum.core.designsystem.component.FormTextField
import com.jfeng2048.memorandum.core.designsystem.component.LoadingView
import com.jfeng2048.memorandum.core.designsystem.component.SimpleTopBar
import com.jfeng2048.memorandum.core.designsystem.component.ToastHost
import com.jfeng2048.memorandum.core.designsystem.component.TopBarAction
import com.jfeng2048.memorandum.core.designsystem.theme.BrandBlue
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.ChevronGrey
import com.jfeng2048.memorandum.core.designsystem.theme.DangerRed
import com.jfeng2048.memorandum.core.designsystem.theme.Hairline
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.TextTertiary
import com.jfeng2048.memorandum.core.designsystem.theme.relationshipTint

/**
 * 关系管理：用户可以自己新增、重命名、删除关系。
 * 被人物使用的关系统一不允许删除，避免人物失去归属。
 */
@Composable
fun RelationshipScreen(
    onBack: () -> Unit,
    viewModel: RelationshipViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RelationshipEffect.ShowToast -> toast = effect.message
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleTopBar(backLabel = "人物", onBack = onBack) {
                TopBarAction(
                    text = "新建",
                    onClick = { viewModel.onEvent(RelationshipEvent.StartCreate) },
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "关系",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "这里维护的关系会出现在首页筛选和人物表单里。",
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    color = Muted,
                )
            }

            Spacer(Modifier.height(10.dp))

            val errorMessage = state.errorMessage
            when {
                errorMessage != null -> ErrorView(
                    message = errorMessage,
                    onRetry = { viewModel.onEvent(RelationshipEvent.Retry) },
                )

                state.isLoading -> LoadingView(modifier = Modifier.weight(1f))

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                ) {
                    itemsIndexed(
                        items = state.items,
                        key = { _, item -> item.id },
                    ) { index, item ->
                        RelationshipRow(
                            item = item,
                            showDivider = index != state.items.lastIndex,
                            onRename = {
                                viewModel.onEvent(RelationshipEvent.StartRename(item.id))
                            },
                            onDelete = {
                                viewModel.onEvent(RelationshipEvent.RequestDelete(item.id))
                            },
                        )
                    }
                    item {
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "被人物使用的关系不能删除；系统至少保留一个关系。",
                            fontSize = 11.5.sp,
                            lineHeight = 18.sp,
                            color = TextTertiary,
                        )
                    }
                }
            }
        }

        ToastHost(
            message = toast,
            onDismiss = { toast = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
        )
    }

    state.draft?.let { draft ->
        RelationshipDraftDialog(
            draft = draft,
            onNameChange = { viewModel.onEvent(RelationshipEvent.DraftChanged(it)) },
            onConfirm = { viewModel.onEvent(RelationshipEvent.ConfirmDraft) },
            onDismiss = { viewModel.onEvent(RelationshipEvent.DismissDraft) },
        )
    }

    state.pendingDelete?.let { item ->
        ConfirmDialog(
            title = "删除「${item.name}」？",
            message = if (item.peopleCount > 0) {
                "还有 ${item.peopleCount} 个人在用这个关系，需要先给他们换一个关系。"
            } else {
                "删除后不可恢复。"
            },
            onConfirm = { viewModel.onEvent(RelationshipEvent.ConfirmDelete) },
            onDismiss = { viewModel.onEvent(RelationshipEvent.DismissDelete) },
        )
    }
}

@Composable
private fun RelationshipRow(
    item: RelationshipItemUi,
    showDivider: Boolean,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val tint = relationshipTint(item.colorIndex)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(color = tint.foreground, size = 8.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.peopleCount} 个人",
                    fontSize = 11.sp,
                    color = Muted,
                )
            }
            Text(
                text = "重命名",
                fontSize = 12.5.sp,
                color = BrandBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onRename)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "删除",
                fontSize = 12.5.sp,
                color = DangerRed,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onDelete)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(color = Hairline)
        }
    }
}

@Composable
private fun RelationshipDraftDialog(
    draft: RelationshipDraft,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (draft.isRename) "重命名关系" else "新建关系",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        },
        text = {
            Column {
                FormTextField(
                    value = draft.name,
                    onValueChange = onNameChange,
                    placeholder = "例如：球友、邻居",
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "颜色会按顺序自动分配。",
                    fontSize = 11.5.sp,
                    color = Muted,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = draft.canConfirm,
            ) {
                Text(
                    text = "确定",
                    color = if (draft.canConfirm) BrandBlue else ChevronGrey,
                    fontWeight = FontWeight.SemiBold,
                )
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
