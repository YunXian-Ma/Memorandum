package com.jfeng2048.memorandum.service.floating

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import com.jfeng2048.memorandum.domain.model.EntryCategory
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfeng2048.memorandum.core.floatingtheme.FloatingAvatar
import com.jfeng2048.memorandum.core.floatingtheme.FloatingHomeIcon
import com.jfeng2048.memorandum.core.floatingtheme.FloatingPet
import com.jfeng2048.memorandum.core.floatingtheme.PetState
import com.jfeng2048.memorandum.core.floatingtheme.FloatingTheme
import com.jfeng2048.memorandum.core.designsystem.component.CategoryDot
import com.jfeng2048.memorandum.core.designsystem.component.PersonAvatar
import com.jfeng2048.memorandum.core.designsystem.component.RelationshipTag
import com.jfeng2048.memorandum.core.designsystem.theme.BrandBlue
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.DisabledGrey
import com.jfeng2048.memorandum.core.designsystem.theme.Hairline
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.PersonNameTextStyle
import com.jfeng2048.memorandum.core.designsystem.theme.SurfaceMuted
import com.jfeng2048.memorandum.core.designsystem.theme.TextBody
import com.jfeng2048.memorandum.core.designsystem.theme.TextSecondary
import com.jfeng2048.memorandum.core.designsystem.theme.TrackGrey
import com.jfeng2048.memorandum.core.designsystem.theme.categoryColor
import com.jfeng2048.memorandum.domain.model.ConversationSource
import com.jfeng2048.memorandum.domain.model.Person

enum class FloatingMode { COLLECT, CREATE, PROFILE }

/** 收集内容的类型：整段对话，或一条人物词条 */
enum class CollectKind { CONVERSATION, ENTRY }

/** 面板状态放在 Service 里，收起再展开时草稿不丢 */
data class FloatingPanelState(
    val mode: FloatingMode = FloatingMode.COLLECT,
    val selectedPersonId: Long? = null,
    val draft: String = "",
    val source: ConversationSource = ConversationSource.MANUAL,
    val kind: CollectKind = CollectKind.CONVERSATION,
    val entryTitle: String = "",
    val entryContent: String = "",
    val categoryId: Long? = null,
)

@Composable
fun FloatingBall(
    theme: FloatingTheme,
    isDragging: Boolean,
    isOverHome: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isOverHome -> 0.86f
            isDragging -> 1.08f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "petBallScale",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isOverHome) theme.palette.roofColor else theme.palette.primaryColor,
        animationSpec = tween(160),
        label = "petBallBorder",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                ) { change, amount ->
                    change.consume()
                    onDrag(amount.x, amount.y)
                }
            }
            .scale(scale)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(theme.palette.softBg)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        FloatingPet(
            theme = theme,
            state = when {
                isOverHome -> PetState.HAPPY
                isDragging -> PetState.DRAGGING
                else -> PetState.IDLE
            },
            size = 40.dp,
        )
    }
}

/**
 * 拖拽悬浮宠物时在屏幕底部弹出的「宠物的家」回收区。
 * 拖入该区域松手即表示宠物回家休息（关闭悬浮窗）。
 */
@Composable
fun ThemeHomeDock(
    theme: FloatingTheme,
    hovered: Boolean,
) {
    val scale by animateFloatAsState(
        targetValue = if (hovered) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 360f),
        label = "petHomeScale",
    )
    val bgColor by animateColorAsState(
        targetValue = if (hovered) theme.palette.softBg else Canvas.copy(alpha = 0.96f),
        animationSpec = tween(160),
        label = "petHomeBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (hovered) theme.palette.roofColor else Hairline,
        animationSpec = tween(160),
        label = "petHomeBorder",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 26.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .scale(scale)
                .shadow(if (hovered) 14.dp else 8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(bgColor)
                .border(BorderStroke(if (hovered) 1.5.dp else 1.dp, borderColor), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FloatingHomeIcon(
                theme = theme,
                hovered = hovered,
                size = 44.dp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (hovered) "松手让${theme.copy.displayName}回家" else theme.copy.homeSubtitle,
                fontSize = 12.5.sp,
                fontWeight = if (hovered) FontWeight.Bold else FontWeight.Medium,
                color = if (hovered) theme.palette.roofColor else TextSecondary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingPanel(
    theme: FloatingTheme,
    state: FloatingPanelState,
    people: List<Person>,
    categories: List<EntryCategory>,
    onStateChange: (FloatingPanelState) -> Unit,
    onReadClipboard: () -> Unit,
    onSaveConversation: () -> Unit,
    onSaveEntry: () -> Unit,
    onQuickCreate: (String, String) -> Unit,
    onCollapse: () -> Unit,
) {
    val selected = people.firstOrNull { it.id == state.selectedPersonId }
    // 点击面板外的暗层收起
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onCollapse,
            )
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        var shown by remember { mutableStateOf(false) }
        androidx.compose.runtime.LaunchedEffect(Unit) { shown = true }
        val pop by animateFloatAsState(
            targetValue = if (shown) 1f else 0.82f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 380f),
            label = "panelPop",
        )
        Column(
            modifier = Modifier
                .scale(pop)
                .alpha(((pop - 0.82f) / 0.18f).coerceIn(0f, 1f))
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Canvas)
                .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(16.dp),
        ) {
            when (state.mode) {
                FloatingMode.COLLECT -> CollectMode(
                    theme = theme,
                    state = state,
                    people = people,
                    categories = categories,
                    selected = selected,
                    onStateChange = onStateChange,
                    onReadClipboard = onReadClipboard,
                    onSave = if (state.kind == CollectKind.ENTRY) onSaveEntry else onSaveConversation,
                    onCollapse = onCollapse,
                )

                FloatingMode.CREATE -> CreateMode(
                    onBack = { onStateChange(state.copy(mode = FloatingMode.COLLECT)) },
                    onCreate = onQuickCreate,
                )

                FloatingMode.PROFILE -> ProfileMode(
                    person = selected,
                    onBack = { onStateChange(state.copy(mode = FloatingMode.COLLECT)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.CollectMode(
    theme: FloatingTheme,
    state: FloatingPanelState,
    people: List<Person>,
    categories: List<EntryCategory>,
    selected: Person?,
    onStateChange: (FloatingPanelState) -> Unit,
    onReadClipboard: () -> Unit,
    onSave: () -> Unit,
    onCollapse: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val shown = people.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
    val isEntry = state.kind == CollectKind.ENTRY
    val canSave = selected != null && if (isEntry) {
        state.entryTitle.isNotBlank() && state.categoryId != null
    } else {
        state.draft.isNotBlank()
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(theme.palette.softBg)
                .border(1.dp, theme.palette.primaryColor.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            FloatingAvatar(theme = theme, size = 28.dp)
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("记一笔", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(
                text = if (selected == null) {
                    theme.copy.companionTip
                } else {
                    "正在记录 ${selected.name} 的故事"
                },
                fontSize = 11.sp,
                color = Muted,
            )
        }
        TextAction("看档案", enabled = selected != null) {
            onStateChange(state.copy(mode = FloatingMode.PROFILE))
        }
        Spacer(Modifier.width(8.dp))
        TextAction("收起", onClick = onCollapse)
    }

    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel("1  这是谁的故事", Modifier.weight(1f))
        TextAction("＋ 新朋友") { onStateChange(state.copy(mode = FloatingMode.CREATE)) }
    }
    Spacer(Modifier.height(8.dp))
    PanelInput(
        value = query,
        onValueChange = { query = it },
        placeholder = "搜索人物姓名…",
        singleLine = true,
    )
    Spacer(Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .heightIn(max = 88.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        if (shown.isEmpty()) {
            Text("没有匹配的人物，先添加新朋友吧", fontSize = 12.sp, color = Muted)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            shown.forEach { person ->
                val on = person.id == state.selectedPersonId
                val scale by animateFloatAsState(
                    targetValue = if (on) 1.06f else 1f,
                    animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f),
                    label = "chipScale",
                )
                Row(
                    modifier = Modifier
                        .scale(scale)
                        .clip(CircleShape)
                        .background(if (on) BrandBlue else TrackGrey)
                        .clickable {
                            onStateChange(state.copy(selectedPersonId = if (on) null else person.id))
                        }
                        .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PersonAvatar(person.name, person.primaryRelationship, 22.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        person.name,
                        fontSize = 13.sp,
                        color = if (on) Color.White else Ink,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(14.dp))
    KindSwitcher(kind = state.kind) { onStateChange(state.copy(kind = it)) }
    Spacer(Modifier.height(12.dp))

    if (isEntry) {
        SectionLabel("2  选个分类")
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                val on = category.id == state.categoryId
                val color = categoryColor(category)
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (on) color.copy(alpha = 0.16f) else TrackGrey)
                        .border(
                            BorderStroke(1.dp, if (on) color else Color.Transparent),
                            CircleShape,
                        )
                        .clickable {
                            onStateChange(state.copy(categoryId = if (on) null else category.id))
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryDot(color = color, size = 7.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(category.label, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        SectionLabel("3  记点什么")
        Spacer(Modifier.height(8.dp))
        PanelInput(
            value = state.entryTitle,
            onValueChange = { onStateChange(state.copy(entryTitle = it)) },
            placeholder = "词条标题，如：爱喝美式、生日 5/20",
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        PanelInput(
            value = state.entryContent,
            onValueChange = { onStateChange(state.copy(entryContent = it)) },
            placeholder = "补充细节（选填）",
            singleLine = false,
            modifier = Modifier.heightIn(min = 64.dp, max = 120.dp),
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("2  对话内容 · ${state.source.shortLabel()}", Modifier.weight(1f))
            TextAction("读取剪贴板", onClick = onReadClipboard)
        }
        Spacer(Modifier.height(8.dp))
        PanelInput(
            value = state.draft,
            onValueChange = {
                val source = if (it.isBlank()) ConversationSource.MANUAL else state.source
                onStateChange(state.copy(draft = it, source = source))
            },
            placeholder = "点「读取剪贴板」，或直接粘贴 / 输入对话",
            singleLine = false,
            modifier = Modifier.heightIn(min = 96.dp, max = 150.dp),
        )
    }

    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "清空",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(TrackGrey)
                .clickable {
                    onStateChange(
                        state.copy(
                            draft = "", source = ConversationSource.MANUAL,
                            entryTitle = "", entryContent = "",
                        ),
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (canSave) BrandBlue else DisabledGrey)
                .clickable(enabled = canSave, onClick = onSave)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when {
                    selected == null -> "请先选择归属人物"
                    isEntry && state.categoryId == null -> "再选个分类"
                    isEntry -> "记下词条给 ${selected.name}"
                    else -> "保存对话给 ${selected.name}"
                },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun KindSwitcher(kind: CollectKind, onChange: (CollectKind) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TrackGrey)
            .padding(3.dp),
    ) {
        listOf(
            CollectKind.ENTRY to "\u2728 记词条",
            CollectKind.CONVERSATION to "\uD83D\uDCAC 记对话",
        ).forEach { (value, label) ->
            val on = kind == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) Canvas else Color.Transparent)
                    .clickable { onChange(value) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                    color = if (on) Ink else TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.CreateMode(
    onBack: () -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val canCreate = name.isNotBlank()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "快速添加人物",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        TextAction("返回", onClick = onBack)
    }
    Spacer(Modifier.height(14.dp))
    SectionLabel("姓名 · 必填")
    Spacer(Modifier.height(8.dp))
    PanelInput(name, { name = it }, "对方的名字", singleLine = true)
    Spacer(Modifier.height(12.dp))
    SectionLabel("个人简介 · 选填")
    Spacer(Modifier.height(8.dp))
    PanelInput(bio, { bio = it }, "一句话认识 TA", singleLine = false,
        modifier = Modifier.heightIn(min = 72.dp, max = 120.dp))
    Spacer(Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (canCreate) BrandBlue else DisabledGrey)
            .clickable(enabled = canCreate) { onCreate(name, bio) }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("创建并选中", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.ProfileMode(
    person: Person?,
    onBack: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "人物档案",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        TextAction("回收集", onClick = onBack)
    }
    Spacer(Modifier.height(14.dp))
    if (person == null) {
        Text("还没有选中人物", fontSize = 13.sp, color = Muted)
        return
    }
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PersonAvatar(person.name, person.primaryRelationship, 48.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(person.name, style = PersonNameTextStyle, color = Ink)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    person.relationships.forEach { RelationshipTag(relationship = it) }
                }
            }
        }
        if (person.bio.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(person.bio, fontSize = 13.sp, lineHeight = 20.sp, color = TextBody)
        }
        Spacer(Modifier.height(14.dp))
        SectionLabel("记忆词条 · ${person.entryCount}")
        Spacer(Modifier.height(6.dp))
        if (person.entries.isEmpty()) {
            Text("还没有词条", fontSize = 12.sp, color = Muted)
        }
        person.entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryDot(color = categoryColor(entry.category), size = 6.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${entry.category.label} · ${entry.title}",
                    fontSize = 13.sp,
                    color = Ink,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Muted,
        modifier = modifier,
    )
}

@Composable
private fun TextAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = BrandBlue,
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
    )
}

@Composable
private fun PanelInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMuted)
            .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        if (value.isEmpty()) {
            Text(placeholder, fontSize = 14.sp, color = Muted)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = TextStyle(fontSize = 14.sp, color = Ink, lineHeight = 20.sp),
            cursorBrush = SolidColor(BrandBlue),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun ConversationSource.shortLabel() =
    if (this == ConversationSource.CLIPBOARD) "剪贴板" else "手动输入"
