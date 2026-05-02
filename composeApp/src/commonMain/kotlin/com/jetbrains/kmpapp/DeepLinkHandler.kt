package com.jetbrains.kmpapp

import kotlinx.coroutines.flow.MutableStateFlow

object DeepLinkHandler {
    val pendingDeepLink = MutableStateFlow<String?>(null)

    fun handleDeepLink(url: String) {
        pendingDeepLink.value = url
    }
}

sealed class ParsedDeepLink {
    data class Invite(val token: String) : ParsedDeepLink()
    data class Wishlist(val token: String) : ParsedDeepLink()
}

/**
 * Парсит поддерживаемые URL:
 *  - familytodo://invite/{token}
 *  - sweethome://wish/{token}
 *  - https://app.sweethome.app/wish/{token}
 */
fun parseDeepLink(url: String): ParsedDeepLink? {
    val inviteToken = url.removePrefix("familytodo://invite/").takeIf { it != url && it.isNotEmpty() }
    if (inviteToken != null) return ParsedDeepLink.Invite(inviteToken)

    val sweethomeWish = url.removePrefix("sweethome://wish/").takeIf { it != url && it.isNotEmpty() }
    if (sweethomeWish != null) return ParsedDeepLink.Wishlist(sweethomeWish.substringBefore('?').substringBefore('#'))

    val httpsWish = listOf(
        "https://app.sweethome.app/wish/",
        "http://app.sweethome.app/wish/",
    ).firstNotNullOfOrNull { prefix ->
        url.removePrefix(prefix).takeIf { it != url && it.isNotEmpty() }
    }
    if (httpsWish != null) return ParsedDeepLink.Wishlist(httpsWish.substringBefore('/').substringBefore('?').substringBefore('#'))

    return null
}
