package com.jfeng2048.memorandum.feature.home

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
import androidx.compose.foundation.layout.offset
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

    HomeContent(
        state = state,
        onQueryChange = { viewModel.onEvent(HomeEvent.QueryChanged(it)) },
        onFilterChange = { viewModel.onEvent(HomeEvent.FilterChanged(it)) },
        onToggleSort = { viewModel.onEvent(HomeEvent.ToggleSort) },
        onRandom = { viewModel.onEvent(HomeEvent.RandomPick) },
        onOpenPerson = onOpenPerson,
        onCreatePerson = onCreatePerson,
        onRetry = { viewModel.onEvent(HomeEvent.Retry) },
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (Relationship?) -> Unit,
    onToggleSort: () -> Unit,
    onRandom: () -> Unit,
    onOpenPerson: (Long) -> Unit,
    onCreatePerson: () -> Unit,
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
                    RandomButton(onClick = onRandom)
                }

                Spacer(Modifier.height(14.dp))
                SearchField(value = state.query, onValueChange = onQueryChange)

                Spacer(Modifier.height(14.dp))
                FilterTabs(selected = state.relationship, onSelect = onFilterChange)
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
                .size(56.dp)
                .shadow(14.dp, CircleShape)
                .clip(CircleShape)
                .background(BrandBlue)
                .clickable(onClick = onCreatePerson),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "＋",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.offset(y = (-1).dp),
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
private fun RandomButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(TrackGrey)
            .clickable(onClick = onClick)
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
private fun FilterTabs(
    selected: Relationship?,
    onSelect: (Relationship?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        FilterTab(
            label = "全部",
            dotColor = null,
            selected = selected == null,
            onClick = { onSelect(null) },
        )
        Relationship.entries.forEach { relationship ->
            FilterTab(
                label = relationship.label,
                dotColor = relationshipTint(relationship).foreground,
                selected = selected == relationship,
                onClick = { onSelect(relationship) },
            )
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    dotColor: Color?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = dotColor ?: BrandBlue
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
                text = label,
                fontSize = 13.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accent else TextSecondary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (selected) accent else Color.Transparent),
        )
    }
}
