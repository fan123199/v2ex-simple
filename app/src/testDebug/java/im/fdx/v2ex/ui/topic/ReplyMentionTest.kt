package im.fdx.v2ex.ui.topic

import im.fdx.v2ex.data.model.Member
import im.fdx.v2ex.data.model.Reply
import im.fdx.v2ex.ui.common.parseReplyMention
import org.junit.Assert.*
import org.junit.Test

class ReplyMentionTest {
    @Test
    fun relativeAndAbsoluteMemberLinksResolveWithFloor() {
        listOf("/member/Alice", "https://www.v2ex.com/member/Alice", "//v2ex.com/member/Alice/", "http://v2ex.com/member/Alice").forEach { url ->
            val mention = parseReplyMention(url, "@Alice\u00a0#12 hello", 1, 6)
            assertEquals("Alice", mention?.username)
            assertEquals(12, mention?.floor)
        }
        assertNull(parseReplyMention("/member/Alice", "@Alice hello", 0, 6)?.floor)
    }

    @Test
    fun eachOccurrenceUsesItsOwnFollowingFloor() {
        val text = "@Alice #1 @Alice #9"
        assertEquals(1, parseReplyMention("/member/Alice", text, 1, 6)?.floor)
        assertEquals(9, parseReplyMention("/member/Alice", text, 11, 16)?.floor)
    }

    @Test
    fun unrelatedLinksAreNotIntercepted() {
        listOf("https://other.example/member/Alice", "https://v2ex.com.evil.test/member/Alice", "javascript:/member/Alice", "/t/123", "/member/Alice/posts").forEach {
            assertNull(parseReplyMention(it, "@Alice", 0, 6))
        }
        assertNull(parseReplyMention("/member/Alice", "profile link", 0, 12))
    }

    private fun reply(id: String, name: String, floor: Int) =
        Reply(id = id, member = Member(username = name)).apply { setRowNum(floor) }

    @Test
    fun explicitFloorWinsOverMoreRecentReplyAndIsNotAnArrayIndex() {
        val first = reply("1", "Alice", 3)
        val recent = reply("2", "alice", 8)
        val source = reply("3", "Bob", 12)
        val replies = listOf(first, recent, source)
        assertSame(first, findMentionedReply(replies, "3", "ALICE", 3))
        assertSame(recent, findMentionedReply(replies, "3", "Alice", null))
    }

    @Test
    fun invalidFloorFallsBackOnlyToEarlierRepliesFromThatUser() {
        val earlier = reply("1", "Alice", 1)
        val source = reply("2", "Bob", 2)
        val future = reply("3", "Alice", 3)
        val replies = listOf(earlier, source, future)
        assertSame(earlier, findMentionedReply(replies, "2", "Alice", 3))
        assertSame(earlier, findMentionedReply(replies, "2", "Alice", 2))
        assertNull(findMentionedReply(replies, "2", "Bob", null))
        assertNull(findMentionedReply(replies, "1", "Alice", null))
        assertNull(findMentionedReply(replies, "missing", "Alice", null))
        assertNull(findMentionedReply(replies, "2", "missing", null))
    }
}
