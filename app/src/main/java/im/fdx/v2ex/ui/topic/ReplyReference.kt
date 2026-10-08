package im.fdx.v2ex.ui.topic

import im.fdx.v2ex.data.model.Reply

internal fun findMentionedReply(
    replies: List<Reply>,
    sourceId: String,
    username: String,
    floor: Int?
): Reply? {
    val sourceIndex = replies.indexOfFirst { it.id == sourceId }
    if (sourceIndex < 0) return null
    val precedingReplies = replies.take(sourceIndex)
        .filter { it.member?.username.equals(username, ignoreCase = true) }
    return floor?.let { number -> precedingReplies.find { it.getRowNum() == number } }
        ?: precedingReplies.lastOrNull()
}
