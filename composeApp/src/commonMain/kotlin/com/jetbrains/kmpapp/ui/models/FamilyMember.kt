package com.jetbrains.kmpapp.ui.models

data class FamilyMember(
    val id: String,
    val name: String,
    val initial: String,
    val role: Role,
    val avatarPalette: Palette,
    val age: Int? = null,
    val pet: Pet? = null,
)

enum class Role(val displayName: String) {
    ADMIN("Админ"),
    PARENT("Родитель"),
    TEEN("Подросток"),
    KID("Ребёнок"),
    GUEST("Гость");

    val isKidMode: Boolean get() = this == KID
}
