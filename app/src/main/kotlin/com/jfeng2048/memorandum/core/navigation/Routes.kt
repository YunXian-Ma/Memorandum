package com.jfeng2048.memorandum.core.navigation

/** 单 Activity + Navigation Compose 的路由表 */
object Routes {

    const val ARG_PERSON_ID = "personId"
    const val ARG_ENTRY_ID = "entryId"

    const val HOME = "home"
    const val RELATIONSHIPS = "relationships"
    const val CATEGORIES = "categories"
    const val PERSON_CREATE = "person/create"
    const val PERSON_DETAIL = "person/{personId}"
    const val PERSON_EDIT = "person/{personId}/edit"
    const val ENTRY_CREATE = "person/{personId}/entry/create"
    const val ENTRY_EDIT = "person/{personId}/entry/{entryId}/edit"

    fun personDetail(personId: Long): String = "person/$personId"

    fun personEdit(personId: Long): String = "person/$personId/edit"

    fun entryCreate(personId: Long): String = "person/$personId/entry/create"

    fun entryEdit(personId: Long, entryId: Long): String =
        "person/$personId/entry/$entryId/edit"
}
