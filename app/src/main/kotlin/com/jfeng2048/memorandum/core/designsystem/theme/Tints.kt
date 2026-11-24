package com.jfeng2048.memorandum.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Relationship

/** 一种色板：浅底色 + 深文字色，用于关系标签与关系头像 */
data class Tint(
    val background: Color,
    val foreground: Color,
)

/**
 * 关系色：只做底色与文字色，不喧哗。
 * 取值与原型 REL_STYLE 一致。
 */
fun relationshipTint(relationship: Relationship): Tint = when (relationship) {
    Relationship.FRIEND -> Tint(Color(0xFFFFE7DE), Color(0xFFB4431F))
    Relationship.COLLEAGUE -> Tint(Color(0xFFE3ECFF), Color(0xFF2C56B8))
    Relationship.FAMILY -> Tint(Color(0xFFE2F1E6), Color(0xFF2E7048))
    Relationship.CLASSMATE -> Tint(Color(0xFFEFE7FF), Color(0xFF6446A0))
    Relationship.CLIENT -> Tint(Color(0xFFFBEFD3), Color(0xFF8F6416))
    Relationship.OTHER -> Tint(Color(0xFFECEDF1), Color(0xFF5A5F68))
}

/**
 * 词条分类色：9 个分类，只以点状出现。
 * 取值与原型 CAT_STYLE 一致。
 */
fun categoryColor(category: EntryCategory): Color = when (category) {
    EntryCategory.CAREER -> Color(0xFF2C56B8)
    EntryCategory.HOBBY -> Color(0xFFE0703A)
    EntryCategory.PERSONALITY -> Color(0xFF8B5BB8)
    EntryCategory.LIFESTYLE -> Color(0xFF3C8C63)
    EntryCategory.DIET -> Color(0xFFC9932B)
    EntryCategory.EDUCATION -> Color(0xFF3A7CA8)
    EntryCategory.WORK -> Color(0xFF5C6AC4)
    EntryCategory.DATE -> Color(0xFFC4433C)
    EntryCategory.OTHER -> Color(0xFF8A8F9B)
}
