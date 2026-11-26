package com.jfeng2048.memorandum.feature.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.jfeng2048.memorandum.core.designsystem.theme.categoryColor

/**
 * 词条分类管理：用户可以自己新增、重命名、删除词条分类。
 * 被词条使用的分类不允许删除，避免词条失去归属。
 */
@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CategoryEffect.ShowToast -> toast = effect.message
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
            SimpleTopBar(backLabel = "返回", onBack = onBack) {
                TopBarAction(
                    text = "新建",
                    onClick = { viewModel.onEvent(CategoryEvent.StartCreate) },
                )
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "词条分类",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "这里维护的分类会出现在词条表单和人物详情筛选里。",
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
                    onRetry = { viewModel.onEvent(CategoryEvent.Retry) },
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
                        CategoryRow(
                            item = item,
                            showDivider = index != state.items.lastIndex,
                            canDelete = item.entryCount == 0 && state.items.size > 1,
                            onRename = {
                                viewModel.onEvent(CategoryEvent.StartRename(item.id))
                            },
                            onDelete = {
                                viewModel.onEvent(CategoryEvent.RequestDelete(item.id))
                            },
                        )
                    }
                    item {
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "被词条使用的分类不能删除；系统至少保留一个分类。",
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
        CategoryDraftDialog(
            draft = draft,
            onNameChange = { viewModel.onEvent(CategoryEvent.DraftChanged(it)) },
            onConfirm = { viewModel.onEvent(CategoryEvent.ConfirmDraft) },
            onDismiss = { viewModel.onEvent(CategoryEvent.DismissDraft) },
        )
    }

    state.pendingDelete?.let { item ->
        ConfirmDialog(
            title = "删除「${item.name}」？",
            message = if (item.entryCount > 0) {
                "还有 ${item.entryCount} 条词条在用这个分类，需要先改掉再删。"
            } else {
                "删除后不可恢复。"
            },
            onConfirm = { viewModel.onEvent(CategoryEvent.ConfirmDelete) },
            onDismiss = { viewModel.onEvent(CategoryEvent.DismissDelete) },
        )
    }
}

@Composable
private fun CategoryRow(
    item: CategoryItemUi,
    showDivider: Boolean,
    canDelete: Boolean,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = categoryColor(item.colorIndex)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f))
                    .border(BorderStroke(1.dp, color.copy(alpha = 0.25f)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CategoryDot(color = color, size = 8.dp)
            }
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
                    text = "${item.entryCount} 条词条",
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
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "删除",
                fontSize = 12.5.sp,
                color = if (canDelete) DangerRed else ChevronGrey,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = canDelete, onClick = onDelete)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(color = Hairline)
        }
    }
}

@Composable
private fun CategoryDraftDialog(
    draft: CategoryDraft,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (draft.isRename) "重命名分类" else "新建分类",
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
                    placeholder = "例如：禁忌、礼物灵感",
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
