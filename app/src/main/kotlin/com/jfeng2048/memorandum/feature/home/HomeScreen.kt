package com.jfeng2048.memorandum.feature.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jfeng2048.memorandum.feature.floating.FloatingAssistantService
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jfeng2048.memorandum.core.designsystem.component.CategoryDot
import com.jfeng2048.memorandum.core.designsystem.component.EmptyPeopleState
import com.jfeng2048.memorandum.core.designsystem.component.ErrorView
import com.jfeng2048.memorandum.core.designsystem.component.LoadingView
import com.jfeng2048.memorandum.core.designsystem.component.SearchField
import com.jfeng2048.memorandum.core.designsystem.theme.BrandBlue
import com.jfeng2048.memorandum.core.designsystem.theme.Canvas
import com.jfeng2048.memorandum.core.designsystem.theme.Hairline
import com.jfeng2048.memorandum.core.designsystem.theme.Ink
import com.jfeng2048.memorandum.core.designsystem.theme.Muted
import com.jfeng2048.memorandum.core.designsystem.theme.SurfaceMuted
import com.jfeng2048.memorandum.core.designsystem.theme.TouchTarget
import com.jfeng2048.memorandum.core.designsystem.theme.TextSecondary
import com.jfeng2048.memorandum.core.designsystem.theme.TextTertiary
import com.jfeng2048.memorandum.core.designsystem.theme.TrackGrey
import com.jfeng2048.memorandum.core.designsystem.theme.relationshipTint
import com.jfeng2048.memorandum.domain.model.HomeSort
import com.jfeng2048.memorandum.domain.model.Relationship
import com.jfeng2048.memorandum.feature.home.components.PersonRow

@Composable
fun HomeScreen(
    onOpenPerson: (Long) -> Unit,
    onCreatePerson: () -> Unit,
    onManageRelationships: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.OpenPerson -> onOpenPerson(effect.personId)
            }
        }
    }

    val context = LocalContext.current
    val floatingEnabled by viewModel.floatingEnabled.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    fun startOrStop(enabled: Boolean) {
        viewModel.setFloatingEnabled(enabled)
        if (enabled) FloatingAssistantService.start(context) else FloatingAssistantService.stop(context)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    // 从系统授权页返回后，若已授权则自动启动
    DisposableEffect(lifecycleOwner, floatingEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && floatingEnabled) {
                if (Settings.canDrawOverlays(context)) {
                    FloatingAssistantService.start(context)
                } else {
                    viewModel.setFloatingEnabled(false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeContent(
        state = state,
        floatingEnabled = floatingEnabled,
        onToggleFloating = { want ->
            if (!want) {
                startOrStop(false)
            } else {
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (Settings.canDrawOverlays(context)) {
                    startOrStop(true)
                } else {
                    viewModel.setFloatingEnabled(true)
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        ),
                    )
                }
            }
        },
        onQueryChange = { viewModel.onEvent(HomeEvent.QueryChanged(it)) },
        onFilterChange = { viewModel.onEvent(HomeEvent.FilterChanged(it)) },
        onToggleSort = { viewModel.onEvent(HomeEvent.ToggleSort) },
        onRandom = { viewModel.onEvent(HomeEvent.RandomPick) },
        onOpenPerson = onOpenPerson,
        onCreatePerson = onCreatePerson,
        onManageRelationships = onManageRelationships,
        onRetry = { viewModel.onEvent(HomeEvent.Retry) },
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    floatingEnabled: Boolean,
    onToggleFloating: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onFilterChange: (Long?) -> Unit,
    onToggleSort: () -> Unit,
    onRandom: () -> Unit,
    onOpenPerson: (Long) -> Unit,
    onCreatePerson: () -> Unit,
    onManageRelationships: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "人物",
                        style = MaterialTheme.typography.displaySmall,
                        color = Ink,
                    )
                    Text(
                        text = if (state.sort == HomeSort.RECENT) "最近更新 ⌄" else "按姓名 ⌄",
                        fontSize = 12.5.sp,
                        color = BrandBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onToggleSort)
                            .padding(6.dp),
                    )
                }

                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${state.totalPeople} 个人 · ${state.totalEntries} 条词条",
                        fontSize = 12.5.sp,
                        color = Muted,
                    )
                    Spacer(Modifier.weight(1f))
                    RandomButton(
                        enabled = state.totalPeople > 0,
                        onClick = onRandom,
                    )
                    Spacer(Modifier.width(8.dp))
                    ManageRelationshipsButton(onClick = onManageRelationships)
                }

                Spacer(Modifier.height(12.dp))
                FloatingAssistantCard(enabled = floatingEnabled, onToggle = onToggleFloating)

                Spacer(Modifier.height(12.dp))
                SearchField(value = state.query, onValueChange = onQueryChange)

                Spacer(Modifier.height(14.dp))
                FilterTabs(
                    relationships = state.relationships,
                    totalPeople = state.totalPeople,
                    relationshipCounts = state.relationshipCounts,
                    selectedId = state.selectedRelationshipId,
                    onSelect = onFilterChange,
                )
            }

            HorizontalDivider(color = Hairline)

            Box(modifier = Modifier.weight(1f)) {
                val errorMessage = state.errorMessage
                when {
                    errorMessage != null -> ErrorView(
                        message = errorMessage,
                        onRetry = onRetry,
                    )

                    state.isLoading -> LoadingView()

                    else -> PeopleList(state = state, onOpenPerson = onOpenPerson)
                }
            }
        }

        // 右下角单动作按钮：一屏只服务一件事——新建人物
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 22.dp)
                .size(TouchTarget.fab)
                .shadow(14.dp, CircleShape)
                .clip(CircleShape)
                .background(BrandBlue)
                .clickable(onClick = onCreatePerson),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "新建人物",
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun PeopleList(
    state: HomeUiState,
    onOpenPerson: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 2.dp,
            bottom = 100.dp,
        ),
    ) {
        if (state.people.isEmpty()) {
            item {
                EmptyPeopleState(filtered = state.totalPeople > 0)
            }
        } else {
            itemsIndexed(
                items = state.people,
                key = { _, person -> person.id },
            ) { index, person ->
                PersonRow(
                    person = person,
                    showDivider = index != state.people.lastIndex,
                    onClick = { onOpenPerson(person.id) },
                )
            }
        }
    }
}

@Composable
private fun FloatingAssistantCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Color(0xFFF2F6FF) else SurfaceMuted)
            .border(1.dp, Hairline, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "全局悬浮助手",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (enabled) "已开启 · 在聊天软件里点悬浮球收集" else "开启后可在其他 App 上快速收集对话",
                fontSize = 11.5.sp,
                color = Muted,
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = BrandBlue,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = TrackGrey,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Hairline,
            ),
        )
    }
}

@Composable
private fun RandomButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(TrackGrey)
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(start = 8.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(text = "随便翻一个", fontSize = 12.sp, color = TextTertiary)
    }
}

@Composable
private fun ManageRelationshipsButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(TrackGrey)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "关系管理", fontSize = 12.sp, color = TextTertiary)
    }
}

@Composable
private fun FilterTabs(
    relationships: List<Relationship>,
    totalPeople: Int,
    relationshipCounts: Map<Long, Int>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        FilterTab(
            label = "全部",
            count = totalPeople,
            dotColor = null,
            selected = selectedId == null,
            onClick = { onSelect(null) },
        )
        relationships.forEach { relationship ->
            FilterTab(
                label = relationship.name,
                count = relationshipCounts[relationship.id] ?: 0,
                dotColor = relationshipTint(relationship).foreground,
                selected = selectedId == relationship.id,
                onClick = { onSelect(relationship.id) },
            )
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    count: Int,
    dotColor: Color?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = dotColor ?: BrandBlue
    val textColor by animateColorAsState(
        targetValue = if (selected) accent else TextSecondary,
        animationSpec = tween(160),
        label = "filterTabTextColor",
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) accent else Color.Transparent,
        animationSpec = tween(180),
        label = "filterTabIndicatorColor",
    )
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .clickable(onClick = onClick)
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (dotColor != null) {
                CategoryDot(
                    color = dotColor,
                    alpha = if (selected) 1f else 0.45f,
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = if (count > 0) "$label $count" else label,
                fontSize = 13.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(indicatorColor),
        )
    }
}
