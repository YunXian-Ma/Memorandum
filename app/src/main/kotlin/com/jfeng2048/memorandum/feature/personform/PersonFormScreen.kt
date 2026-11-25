package com.jfeng2048.memorandum.feature.personform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jfeng2048.memorandum.core.designsystem.component.FormTextField
import com.jfeng2048.memorandum.core.designsystem.component.GenderSegmentedControl
import com.jfeng2048.memorandum.core.designsystem.component.LoadingView
import com.jfeng2048.memorandum.core.designsystem.component.PersonAvatar
import com.jfeng2048.memorandum.core.designsystem.component.PrimaryButton
import com.jfeng2048.memorandum.core.designsystem.component.RelationshipChip
import com.jfeng2048.memorandum.core.designsystem.component.SectionLabel
import com.jfeng2048.memorandum.core.designsystem.component.SimpleTopBar
import com.jfeng2048.memorandum.core.designsystem.component.ToastHost
import com.jfeng2048.memorandum.core.designsystem.component.TopBarAction
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.DangerRed
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted

/**
 * 新建 / 编辑人物。
 * 姓名必填，关系与性别有默认值，其余字段都是可选的。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonFormScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: PersonFormViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PersonFormEffect.Created -> onCreated(effect.personId)
                PersonFormEffect.Saved -> onBack()
                is PersonFormEffect.ShowToast -> toast = effect.message
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
            SimpleTopBar(backLabel = "取消", onBack = onBack) {
                TopBarAction(
                    text = if (state.isEditing) "保存" else "创建",
                    enabled = state.canSave,
                    onClick = { viewModel.onEvent(PersonFormEvent.Save) },
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
                        text = if (state.isEditing) "编辑人物" else "记一个人",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )

                    Spacer(Modifier.height(22.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PersonAvatar(
                            name = state.name.ifBlank { "?" },
                            relationship = state.primarySelectedRelationship,
                            size = 62.dp,
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "头像先跟着关系走\n之后可以接系统相册",
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = Muted,
                        )
                    }

                    Spacer(Modifier.height(26.dp))
                    SectionLabel(text = "姓名 · 必填")
                    Spacer(Modifier.height(9.dp))
                    FormTextField(
                        value = state.name,
                        onValueChange = { viewModel.onEvent(PersonFormEvent.NameChanged(it)) },
                        placeholder = "写你平时怎么叫他",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )

                    Spacer(Modifier.height(22.dp))
                    SectionLabel(text = "关系 · 可多选")
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.relationships.forEach { relationship ->
                            RelationshipChip(
                                relationship = relationship,
                                selected = relationship.id in state.selectedRelationshipIds,
                                onClick = {
                                    viewModel.onEvent(
                                        PersonFormEvent.RelationshipToggled(relationship.id),
                                    )
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                    SectionLabel(text = "性别")
                    Spacer(Modifier.height(10.dp))
                    GenderSegmentedControl(
                        selected = state.gender,
                        onSelect = { viewModel.onEvent(PersonFormEvent.GenderChanged(it)) },
                    )

                    Spacer(Modifier.height(22.dp))
                    SectionLabel(text = "个人简介 · 选填")
                    Spacer(Modifier.height(9.dp))
                    FormTextField(
                        value = state.bio,
                        onValueChange = { viewModel.onEvent(PersonFormEvent.BioChanged(it)) },
                        placeholder = "一句话记住他。例如：大学室友，做建筑设计，周三打球。",
                        singleLine = false,
                        minLines = 4,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${state.bio.length} 字",
                        fontSize = 11.sp,
                        color = Muted,
                        modifier = Modifier.align(Alignment.End),
                    )

                    state.errorMessage?.let { message ->
                        Spacer(Modifier.height(14.dp))
                        Text(text = message, fontSize = 12.sp, color = DangerRed)
                    }

                    Spacer(Modifier.height(26.dp))
                    PrimaryButton(
                        text = when {
                            state.isSaving -> "保存中…"
                            state.isEditing -> "保存修改"
                            else -> "创建人物"
                        },
                        enabled = state.canSave,
                        onClick = { viewModel.onEvent(PersonFormEvent.Save) },
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
