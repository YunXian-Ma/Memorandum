package com.jfeng2048.memorandum.feature.entryform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.jfeng2048.memorandum.core.designsystem.theme.BrandBlue
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.DangerRed
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.TextSecondary
import com.jfeng2048.memorandum.core.designsystem.theme.TrackGrey
import com.jfeng2048.memorandum.domain.model.EntryCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryFormScreen(
    onBack: () -> Unit,
    onManageCategories: () -> Unit,
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
                        placeholder = titlePlaceholderFor(state.selectedCategory),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                    if (!state.isEditing && state.title.isBlank()) {
                        val presets = titlePresetsFor(state.selectedCategory)
                        if (presets.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                presets.forEach { preset ->
                                    Text(
                                        text = preset,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(TrackGrey)
                                            .clickable {
                                                viewModel.onEvent(EntryFormEvent.TitleChanged(preset))
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionLabel(text = "分类")
                        Text(
                            text = "管理分类",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onManageCategories)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.categories.forEach { category ->
                            CategoryChip(
                                category = category,
                                selected = state.categoryId == category.id,
                                onClick = {
                                    viewModel.onEvent(EntryFormEvent.CategoryChanged(category.id))
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

private fun titlePlaceholderFor(category: EntryCategory?): String = when (category?.name) {
    "职业" -> "例如：产品经理 / 独立建筑师"
    "兴趣爱好" -> "例如：周末打半场篮球"
    "性格特点" -> "例如：慢热但很靠谱"
    "生活习惯" -> "例如：早睡早起 / 养了只猫"
    "饮食偏好" -> "例如：不吃香菜 / 喝美式不加糖"
    "教育经历" -> "例如：浙江大学 · 工业设计"
    "工作经历" -> "例如：曾在杭州做过三年设计"
    "重要日期" -> "例如：生日 10月14日"
    else -> "例如：住城西，开车半小时"
}

private fun titlePresetsFor(category: EntryCategory?): List<String> = when (category?.name) {
    "职业" -> listOf("职业方向", "擅长领域")
    "兴趣爱好" -> listOf("喜欢运动", "喜欢听的歌", "最近在读")
    "性格特点" -> listOf("说话风格", "相处细节")
    "生活习惯" -> listOf("作息习惯", "常去的地方")
    "饮食偏好" -> listOf("忌口", "喜欢的咖啡", "爱吃的菜系")
    "教育经历" -> listOf("毕业院校", "所学专业")
    "工作经历" -> listOf("当前公司", "过往经历")
    "重要日期" -> listOf("生日", "纪念日")
    else -> listOf("常住城市", "备忘提醒")
}
