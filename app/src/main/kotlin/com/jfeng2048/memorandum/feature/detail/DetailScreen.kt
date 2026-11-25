package com.jfeng2048.memorandum.feature.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import com.jfeng2048.memorandum.domain.model.PersonConversation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jfeng2048.memorandum.core.designsystem.component.CategoryFilterLabel
import com.jfeng2048.memorandum.core.designsystem.component.ConfirmDialog
import com.jfeng2048.memorandum.core.designsystem.component.ErrorView
import com.jfeng2048.memorandum.core.designsystem.component.LoadingView
import com.jfeng2048.memorandum.core.designsystem.component.PersonAvatar
import com.jfeng2048.memorandum.core.designsystem.component.RelationshipChip
import com.jfeng2048.memorandum.core.designsystem.component.RelationshipTag
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
import com.jfeng2048.memorandum.core.designsystem.theme.NumericTextStyle
import com.jfeng2048.memorandum.core.designsystem.theme.PersonNameTextStyle
import com.jfeng2048.memorandum.core.designsystem.theme.SurfaceMuted
import com.jfeng2048.memorandum.core.designsystem.theme.SurfaceStrong
import com.jfeng2048.memorandum.core.designsystem.theme.TextBody
import com.jfeng2048.memorandum.core.designsystem.theme.TrackGrey
import com.jfeng2048.memorandum.core.designsystem.theme.TextSecondary
import com.jfeng2048.memorandum.core.designsystem.theme.TouchTarget
import com.jfeng2048.memorandum.core.designsystem.theme.categoryColor
import com.jfeng2048.memorandum.core.designsystem.theme.relationshipTint
import com.jfeng2048.memorandum.core.util.DateFormats
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.Relationship
import com.jfeng2048.memorandum.feature.detail.components.EntryCardOverlay
import com.jfeng2048.memorandum.feature.detail.components.EntryRow

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onEditPerson: (Long) -> Unit,
    onAddEntry: (Long) -> Unit,
    onEditEntry: (Long, Long) -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var toast by remember { mutableStateOf<String?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var deleteEntryId by remember { mutableStateOf<Long?>(null) }
    var deleteConversationId by remember { mutableStateOf<Long?>(null) }
    var confirmDeletePerson by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DetailEffect.Back -> onBack()
                is DetailEffect.EditPerson -> onEditPerson(effect.personId)
                is DetailEffect.AddEntry -> onAddEntry(effect.personId)
                is DetailEffect.EditEntry -> onEditEntry(effect.personId, effect.entryId)
                is DetailEffect.ShowToast -> toast = effect.message
            }
        }
    }

    // 词条卡片打开时，系统返回键先关卡片
    BackHandler(enabled = state.selectedEntryId != null) {
        viewModel.onEvent(DetailEvent.SelectEntry(null))
    }

    val person = state.person
    val errorMessage = state.errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        when {
            errorMessage != null -> ErrorView(
                message = errorMessage,
                onRetry = { viewModel.onEvent(DetailEvent.Retry) },
            )

            state.isLoading -> LoadingView()

            person == null -> NotFoundContent(onBack = onBack)

            else -> DetailContent(
                state = state,
                person = person,
                menuOpen = menuOpen,
                onMenuOpenChange = { menuOpen = it },
                onBack = { viewModel.onEvent(DetailEvent.Back) },
                onEditPerson = { viewModel.onEvent(DetailEvent.EditPerson) },
                onOpenRelationshipEditor = { viewModel.onEvent(DetailEvent.OpenRelationshipEditor) },
                onRequestDeletePerson = { confirmDeletePerson = true },
                onAddEntry = { viewModel.onEvent(DetailEvent.AddEntry) },
                onSelectCategory = { viewModel.onEvent(DetailEvent.SelectCategory(it)) },
                onSelectEntry = { viewModel.onEvent(DetailEvent.SelectEntry(it)) },
                onSwitchTab = { viewModel.onEvent(DetailEvent.SwitchTab(it)) },
                onLongPressConversation = { deleteConversationId = it },
            )
        }

        EntryCardOverlay(
            entry = state.selectedEntry,
            onDismiss = { viewModel.onEvent(DetailEvent.SelectEntry(null)) },
            onEdit = { entry ->
                viewModel.onEvent(DetailEvent.SelectEntry(null))
                state.person?.let { onEditEntry(it.id, entry.id) }
            },
            onDelete = { entry -> deleteEntryId = entry.id },
        )

        ToastHost(
            message = toast,
            onDismiss = { toast = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
        )
    }

    if (confirmDeletePerson) {
        ConfirmDialog(
            title = "删除「${state.person?.name.orEmpty()}」？",
            message = "这个人的全部词条也会一起删除，且无法恢复。",
            onConfirm = {
                confirmDeletePerson = false
                viewModel.onEvent(DetailEvent.DeletePerson)
            },
            onDismiss = { confirmDeletePerson = false },
        )
    }

    if (state.relationshipSheetOpen) {
        EditRelationshipsDialog(
            relationships = state.allRelationships,
            selectedIds = state.draftRelationshipIds,
            onToggle = { viewModel.onEvent(DetailEvent.ToggleDraftRelationship(it)) },
            onConfirm = { viewModel.onEvent(DetailEvent.SaveRelationships) },
            onDismiss = { viewModel.onEvent(DetailEvent.DismissRelationshipEditor) },
        )
    }

    deleteConversationId?.let { id ->
        ConfirmDialog(
            title = "删除这段对话？",
            message = "删除后无法恢复，但不会影响人物档案。",
            onConfirm = {
                deleteConversationId = null
                viewModel.onEvent(DetailEvent.DeleteConversation(id))
            },
            onDismiss = { deleteConversationId = null },
        )
    }

    deleteEntryId?.let { entryId ->
        ConfirmDialog(
            title = "删除这条词条？",
            message = "删除后无法恢复，但不会影响人物档案。",
            onConfirm = {
                deleteEntryId = null
                viewModel.onEvent(DetailEvent.DeleteEntry(entryId))
            },
            onDismiss = { deleteEntryId = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    state: DetailUiState,
    person: Person,
    menuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onEditPerson: () -> Unit,
    onOpenRelationshipEditor: () -> Unit,
    onRequestDeletePerson: () -> Unit,
    onAddEntry: () -> Unit,
    onSelectCategory: (Long?) -> Unit,
    onSelectEntry: (Long) -> Unit,
    onSwitchTab: (DetailTab) -> Unit,
    onLongPressConversation: (Long) -> Unit,
) {
    val primaryRelationship = person.primaryRelationship
    val tint = relationshipTint(primaryRelationship)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 36.dp),
    ) {
        item {
            SimpleTopBar(backLabel = "人物", onBack = onBack) {
                TopBarAction(text = "编辑", onClick = onEditPerson)
                Spacer(Modifier.width(6.dp))
                Box {
                    IconButton(
                        onClick = { onMenuOpenChange(true) },
                        modifier = Modifier.size(TouchTarget.min),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "更多",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { onMenuOpenChange(false) },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "设置关系",
                                    color = Ink,
                                    fontSize = 14.sp,
                                )
                            },
                            onClick = {
                                onMenuOpenChange(false)
                                onOpenRelationshipEditor()
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "删除人物",
                                    color = DangerRed,
                                    fontSize = 14.sp,
                                )
                            },
                            onClick = {
                                onMenuOpenChange(false)
                                onRequestDeletePerson()
                            },
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PersonAvatar(
                        name = person.name,
                        relationship = primaryRelationship,
                        size = 64.dp,
                        ring = true,
                    )
                    Spacer(Modifier.width(15.dp))
                    Column {
                        Text(
                            text = person.name,
                            style = PersonNameTextStyle,
                            color = Ink,
                        )
                        Spacer(Modifier.height(7.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            itemVerticalAlignment = Alignment.CenterVertically,
                        ) {
                            person.relationships.forEach { relationship ->
                                RelationshipTag(
                                    relationship = relationship,
                                    modifier = Modifier.clickable(onClick = onOpenRelationshipEditor),
                                )
                            }
                            Text(
                                text = "+ 改关系",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandBlue,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(SurfaceMuted)
                                    .clickable(onClick = onOpenRelationshipEditor)
                                    .padding(horizontal = 8.dp, vertical = 2.5.dp),
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "${person.gender.label} · 更新于 ${
                                DateFormats.formatUpdated(person.updatedAt)
                            }",
                            fontSize = 12.sp,
                            color = Muted,
                        )
                    }
                }

                if (person.bio.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = person.bio,
                        fontSize = 14.sp,
                        lineHeight = 24.5.sp,
                        color = TextBody,
                    )
                }

                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        value = person.entryCount.toString(),
                        label = "条词条",
                        background = tint.background,
                        foreground = tint.foreground,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = person.categoryCount.toString(),
                        label = "个分类",
                        background = SurfaceStrong,
                        foreground = TextBody,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = DateFormats.formatFull(person.createdAt),
                        label = "建档",
                        background = SurfaceStrong,
                        foreground = TextBody,
                        modifier = Modifier.weight(1.25f),
                    )
                }
            }
        }

        item {
            TabSwitcher(
                tab = state.tab,
                entryCount = person.entryCount,
                conversationCount = person.conversationCount,
                onSwitch = onSwitchTab,
            )
        }

        if (state.tab == DetailTab.CONVERSATIONS) {
            if (person.conversations.isEmpty()) {
                item {
                    Text(
                        text = "还没有收集过对话。开启悬浮助手，在聊天软件里一键收进来。",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                        fontSize = 13.sp,
                        color = Muted,
                    )
                }
            } else {
                items(items = person.conversations, key = { it.id }) { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        onLongPress = { onLongPressConversation(conversation.id) },
                    )
                }
            }
        } else {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "词条",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    color = Muted,
                )
                Spacer(Modifier.width(12.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    CategoryFilterLabel(
                        label = "全部",
                        color = null,
                        selected = state.selectedCategoryId == null,
                        onClick = { onSelectCategory(null) },
                    )
                    state.categories.forEach { category ->
                        CategoryFilterLabel(
                            label = category.label,
                            color = categoryColor(category),
                            selected = state.selectedCategoryId == category.id,
                            onClick = { onSelectCategory(category.id) },
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = Hairline)
        }

        val entries = state.visibleEntries
        if (entries.isEmpty()) {
            item {
                Text(
                    text = if (state.selectedCategoryId == null) {
                        "还没有词条，先记一条吧。"
                    } else {
                        "这个分类下还没有词条。"
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                    fontSize = 13.sp,
                    color = Muted,
                )
            }
        } else {
            itemsIndexed(
                items = entries,
                key = { _, entry -> entry.id },
            ) { index, entry ->
                EntryRow(
                    entry = entry,
                    showDivider = index != entries.lastIndex,
                    onClick = { onSelectEntry(entry.id) },
                )
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceMuted)
                        .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddEntry)
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "添加词条",
                        color = BrandBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 13.dp, vertical = 9.dp),
    ) {
        Text(
            text = value,
            style = NumericTextStyle,
            color = foreground,
            maxLines = 1,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = foreground.copy(alpha = 0.66f),
        )
    }
}

@Composable
private fun NotFoundContent(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        SimpleTopBar(backLabel = "人物", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "没有找到这个人物",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "可能已经被删除了。",
                fontSize = 12.5.sp,
                color = Muted,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditRelationshipsDialog(
    relationships: List<Relationship>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "设置人物关系",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        },
        text = {
            Column {
                Text(
                    text = "一个人可以同时属于多个关系，至少保留一个。",
                    fontSize = 12.sp,
                    color = Muted,
                )
                Spacer(Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    relationships.forEach { relationship ->
                        RelationshipChip(
                            relationship = relationship,
                            selected = relationship.id in selectedIds,
                            onClick = { onToggle(relationship.id) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedIds.isNotEmpty(),
            ) {
                Text(
                    text = "保存",
                    color = if (selectedIds.isNotEmpty()) BrandBlue else ChevronGrey,
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

@Composable
private fun TabSwitcher(
    tab: DetailTab,
    entryCount: Int,
    conversationCount: Int,
    onSwitch: (DetailTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TrackGrey)
            .padding(3.dp),
    ) {
        listOf(
            DetailTab.ENTRIES to "记忆词条 ($entryCount)",
            DetailTab.CONVERSATIONS to "原始对话 ($conversationCount)",
        ).forEach { (value, label) ->
            val on = tab == value
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (on) Canvas else Color.Transparent)
                    .clickable { onSwitch(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                    color = if (on) Ink else TextSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationCard(
    conversation: PersonConversation,
    onLongPress: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceMuted)
            .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(12.dp))
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = conversation.source.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandBlue,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = DateFormats.formatFull(conversation.createdAt),
                fontSize = 11.sp,
                color = Muted,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = conversation.content,
            fontSize = 13.5.sp,
            lineHeight = 21.sp,
            color = TextBody,
        )
    }
}
