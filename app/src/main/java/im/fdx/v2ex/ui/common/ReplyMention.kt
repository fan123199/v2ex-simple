package im.fdx.v2ex.ui.common

import java.net.URI

internal data class ReplyMention(val username: String, val floor: Int?)

/** Only intercept member-name links; ordinary URLs retain their normal behavior. */
internal fun parseReplyMention(url: String, text: CharSequence, start: Int, end: Int): ReplyMention? {
    val uri = runCatching { URI(url) }.getOrNull() ?: return null
    if (uri.scheme != null && !uri.scheme.equals("http", true) && !uri.scheme.equals("https", true)) return null
    if (uri.host != null && !uri.host.equals("v2ex.com", true) && !uri.host.equals("www.v2ex.com", true)) return null
    if (uri.rawAuthority != null && uri.host == null) return null
    val username = Regex("^/member/([A-Za-z0-9_]+)/?$").matchEntire(uri.path.orEmpty())
        ?.groupValues?.get(1) ?: return null
    if (start < 0 || end > text.length || start >= end) return null
    val label = text.subSequence(start, end).toString().trim().removePrefix("@").trim()
    if (!label.equals(username, ignoreCase = true)) return null
    val floor = Regex("^[\\s\\u00a0]*#([0-9]+)\\b")
        .find(text.subSequence(end, text.length))?.groupValues?.get(1)
        ?.toIntOrNull()?.takeIf { it > 0 }
    return ReplyMention(username, floor)
}
