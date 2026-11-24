package com.jfeng2048.memorandum.feature.entryform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jfeng2048.memorandum.core.designsystem.component.CategoryChip
import com.jfeng2048.memorandum.core.designsystem.component.FormTextField
import com.jfeng2048.memorandum.core.designsystem.component.LoadingView
import com.jfeng2048.memorandum.core.designsystem.component.PrimaryButton
import com.jfeng2048.memorandum.core.designsystem.component.SectionLabel
import com.jfeng2048.memorandum.core.designsystem.component.SimpleTopBar
import com.jfeng2048.memorandum.core.designsystem.component.ToastHost
import com.jfeng2048.memorandum.core.designsystem.component.TopBarAction
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.DangerRed
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.domain.model.EntryCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryFormScreen(
    onBack: () -> Unit,
    viewModel: EntryFormViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EntryFormEffect.Saved -> onBack()
                is EntryFormEffect.ShowToast -> toast = effect.message
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
            SimpleTopBar(
                backLabel = state.personName.ifBlank { "返回" },
                onBack = onBack,
            ) {
                TopBarAction(
                    text = "保存",
                    enabled = state.canSave,
                    onClick = { viewModel.onEvent(EntryFormEvent.Save) },
                )
            }

            if (state.isLoading) {
                LoadingView(modifier = Modifier.weight(1f))
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state.isEditing) "编辑词条" else "新词条",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "给 ${state.personName} 记一条，之后想得起来。",
                        fontSize = 12.5.sp,
                        color = Muted,
                    )

                    Spacer(Modifier.height(26.dp))
                    SectionLabel(text = "名称 · 必填")
                    Spacer(Modifier.height(9.dp))
                    FormTextField(
                        value = state.title,
                        onValueChange = { viewModel.onEvent(EntryFormEvent.TitleChanged(it)) },
                        placeholder = "例如：不吃香菜",
                    )

                    Spacer(Modifier.height(22.dp))
                    SectionLabel(text = "分类")
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        EntryCategory.entries.forEach { category ->
                            CategoryChip(
                                category = category,
                                selected = state.category == category,
                                onClick = {
                                    viewModel.onEvent(EntryFormEvent.CategoryChanged(category))
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                    SectionLabel(text = "详细描述 · 选填")
                    Spacer(Modifier.height(9.dp))
                    FormTextField(
                        value = state.content,
                        onValueChange = { viewModel.onEvent(EntryFormEvent.ContentChanged(it)) },
                        placeholder = "具体的细节，越具体越有用。",
                        singleLine = false,
                        minLines = 6,
                    )

                    state.errorMessage?.let { message ->
                        Spacer(Modifier.height(14.dp))
                        Text(text = message, fontSize = 12.sp, color = DangerRed)
                    }

                    Spacer(Modifier.height(26.dp))
                    PrimaryButton(
                        text = if (state.isSaving) "保存中…" else "保存词条",
                        enabled = state.canSave,
                        onClick = { viewModel.onEvent(EntryFormEvent.Save) },
                    )
                    Spacer(Modifier.height(13.dp))
                    Text(
                        text = "保存后回到 ${state.personName}，下次就不用再问一遍。",
                        fontSize = 11.5.sp,
                        color = Muted,
                    )
                    Spacer(Modifier.height(28.dp))
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
}
