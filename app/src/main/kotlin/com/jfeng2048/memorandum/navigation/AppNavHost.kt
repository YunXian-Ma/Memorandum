package com.jfeng2048.memorandum.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jfeng2048.memorandum.core.navigation.Routes
import com.jfeng2048.memorandum.feature.category.CategoryScreen
import com.jfeng2048.memorandum.feature.person.detail.DetailScreen
import com.jfeng2048.memorandum.feature.entry.EntryFormScreen
import com.jfeng2048.memorandum.feature.home.HomeScreen
import com.jfeng2048.memorandum.feature.person.form.PersonFormScreen
import com.jfeng2048.memorandum.feature.relationship.RelationshipScreen

/**
 * 全应用只有这一个 NavHost，页面之间不共享 Activity。
 * 路由顺序里 PERSON_CREATE 放在 PERSON_DETAIL 之前，避免 "person/create" 被参数路由抢先匹配。
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenPerson = { personId ->
                    navController.navigate(Routes.personDetail(personId))
                },
                onCreatePerson = {
                    navController.navigate(Routes.PERSON_CREATE)
                },
                onManageRelationships = {
                    navController.navigate(Routes.RELATIONSHIPS)
                },
            )
        }

        composable(Routes.RELATIONSHIPS) {
            RelationshipScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CATEGORIES) {
            CategoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PERSON_CREATE) {
            PersonFormScreen(
                onBack = { navController.popBackStack() },
                onCreated = { personId ->
                    navController.navigate(Routes.personDetail(personId)) {
                        // 新建完成后，返回键回到首页而不是表单
                        popUpTo(Routes.HOME)
                    }
                },
            )
        }

        composable(
            route = Routes.PERSON_DETAIL,
            arguments = listOf(
                navArgument(Routes.ARG_PERSON_ID) { type = NavType.LongType },
            ),
        ) {
            DetailScreen(
                onBack = { navController.popBackStack() },
                onEditPerson = { personId ->
                    navController.navigate(Routes.personEdit(personId))
                },
                onAddEntry = { personId ->
                    navController.navigate(Routes.entryCreate(personId))
                },
                onEditEntry = { personId, entryId ->
                    navController.navigate(Routes.entryEdit(personId, entryId))
                },
            )
        }

        composable(
            route = Routes.PERSON_EDIT,
            arguments = listOf(
                navArgument(Routes.ARG_PERSON_ID) { type = NavType.LongType },
            ),
        ) {
            PersonFormScreen(
                onBack = { navController.popBackStack() },
                // 编辑保存后由 ViewModel 触发返回，这里不会被调用
                onCreated = {},
            )
        }

        composable(
            route = Routes.ENTRY_CREATE,
            arguments = listOf(
                navArgument(Routes.ARG_PERSON_ID) { type = NavType.LongType },
            ),
        ) {
            EntryFormScreen(
                onBack = { navController.popBackStack() },
                onManageCategories = { navController.navigate(Routes.CATEGORIES) },
            )
        }

        composable(
            route = Routes.ENTRY_EDIT,
            arguments = listOf(
                navArgument(Routes.ARG_PERSON_ID) { type = NavType.LongType },
                navArgument(Routes.ARG_ENTRY_ID) { type = NavType.LongType },
            ),
        ) {
            EntryFormScreen(
                onBack = { navController.popBackStack() },
                onManageCategories = { navController.navigate(Routes.CATEGORIES) },
            )
        }
    }
}
