package im.fdx.v2ex.ui.login

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebLoginSessionTest {
    private val authenticatedPage = """
        <div id="Rightbar"><div class="box"><div class="cell">
          <a href="/member/alice"><img alt="alice" src="//cdn.v2ex.com/alice.png"></a>
          <a href="/member/alice">alice</a>
        </div></div></div>
        <a href="/signout?once=123">登出</a>
    """.trimIndent()
    private val cookies = parseWebLoginCookies("A2=valid-session; PB3_SESSION=browser-session")
    // Synthetic Tornado signed-cookie shape; this is not an account credential.
    private val quotedSession = "\"2|1:0|10:1791321600|2:A2|8:YWxpY2U=|64:${"a".repeat(64)}\""

    @Test
    fun onlyExactHttpsV2exOriginsAreAccepted() {
        listOf("https://www.v2ex.com/", "https://v2ex.com/auth/google", "https://edge.v2ex.com/2fa")
            .forEach { assertTrue(it, isTrustedWebLoginUrl(it)) }
        listOf(
            "http://www.v2ex.com/", "https://www.v2ex.com.evil.test/", "https://evilv2ex.com/",
            "https://accounts.google.com/", "https://www.v2ex.com:8443/", "https://fake@www.v2ex.com/",
            "https://www.v2ex.com@evil.test/", "javascript:alert(1)", "not a URL"
        ).forEach { assertFalse(it, isTrustedWebLoginUrl(it)) }
    }

    @Test
    fun sessionCookieMustHaveExactNameAndNonemptyValue() {
        assertTrue(hasWebLoginSessionCookie("PB3_SESSION=abc; A2=session=with=padding"))
        listOf("", "A2=", "A2=   ", "A2=\"\"", "notA2=token", "a2=token", "other=A2=token")
            .forEach { assertFalse(it, hasWebLoginSessionCookie(it)) }
    }

    @Test
    fun quotedSignedSessionIsRecognizedAndPreservedThroughVerification() {
        val header = "A2=$quotedSession; PB3_SESSION=browser-session"
        assertTrue(hasWebLoginSessionCookie(header))
        val parsed = parseWebLoginCookies(header)
        assertEquals(quotedSession, parsed.single { it.name == "A2" }.value)
        val result = parseVerifiedWebLoginResponse(200, authenticatedPage, parsed)
        assertNotNull(result)
        assertEquals(quotedSession, result!!.cookies.single { it.name == "A2" }.value)
        assertEquals(header, result.cookies.joinToString("; ") { "${it.name}=${it.value}" })
    }

    @Test
    fun otherQuotedCookiesDoNotBlockSessionRecognition() {
        val header = "A2=valid-session; preferences=\"compact\"; optional=\"\""
        assertTrue(hasWebLoginSessionCookie(header))
        val parsed = parseWebLoginCookies(header)
        assertEquals("\"compact\"", parsed.single { it.name == "preferences" }.value)
        assertEquals("\"\"", parsed.single { it.name == "optional" }.value)
        assertNotNull(parseVerifiedWebLoginResponse(200, authenticatedPage, parsed))
    }

    @Test
    fun malformedQuotedCookieValuesAreRejected() {
        listOf(
            "A2=\"token", "A2=token\"", "A2=\"tok\"en\"", "A2=\"\"token\"\"",
            "A2=\"a b\"", "A2=\"a\\b\"", "A2=\"a,b\"", "A2=\"a;b\"",
            "A2=\"token\r\nInjected: bad\"", "A2=\"one\"; A2=\"two\"",
            "A2=token; preferences=\"unterminated"
        ).forEach { assertFalse(it, hasWebLoginSessionCookie(it)) }
    }

    @Test
    fun quotedEmptySessionCannotCompleteLoginOrSurviveCookieRotation() {
        val empty = Cookie.Builder().name("A2").value("\"\"")
            .hostOnlyDomain("www.v2ex.com").path("/").secure().build()
        assertFalse(hasWebLoginSessionCookie("A2=\"\""))
        assertNull(parseVerifiedWebLoginResponse(200, authenticatedPage, listOf(empty)))
        assertNull(parseVerifiedWebLoginResponse(200, authenticatedPage, cookies, listOf(empty)))
    }

    @Test
    fun invalidOrAmbiguousCookieHeadersAreRejected() {
        listOf("A2=one; A2=two", "A2=token\r\nInjected: bad", "A2=token; malformed", "A2=a b")
            .forEach { assertFalse(it, hasWebLoginSessionCookie(it)) }
        assertEquals(1, parseWebLoginCookies("A2=same; A2=same").size)
    }

    @Test
    fun cookiesAreScopedToCanonicalHttpsRootAndPreserveEqualsInValue() {
        val parsed = parseWebLoginCookies("A2=session==; PB3_SESSION=abc")
        assertEquals("session==", parsed.first().value)
        parsed.forEach {
            assertEquals("www.v2ex.com", it.domain)
            assertEquals("/", it.path)
            assertTrue(it.secure)
            assertTrue(it.hostOnly)
            assertFalse(it.matches("https://accounts.google.com/".toHttpUrl()))
        }
    }

    @Test
    fun authenticatedHomepageReturnsMemberAndCandidateCookies() {
        val result = parseVerifiedWebLoginResponse(200, authenticatedPage, cookies)
        assertNotNull(result)
        assertEquals("alice", result!!.member.username)
        assertEquals("//cdn.v2ex.com/alice.png", result.member.avatar_normal)
        assertEquals(cookies, result.cookies)
    }

    @Test
    fun redirectsErrorsAndTwoFactorChallengeCannotCompleteLogin() {
        listOf(301, 302, 303, 307, 401, 403, 500).forEach {
            assertNull(parseVerifiedWebLoginResponse(it, authenticatedPage, cookies))
        }
        assertNull(parseVerifiedWebLoginResponse(200, authenticatedPage + "<form action='/2fa'><input name='code'></form>", cookies))
        assertNull(parseVerifiedWebLoginResponse(200, authenticatedPage, emptyList()))
    }

    @Test
    fun anonymousPageOrSidebarImageCannotImpersonateCurrentMember() {
        listOf(
            "<a href='/member/alice'>alice</a><a href='/signin'>登录</a>",
            "<div id='Rightbar'><div class='cell'><img alt='advertisement'></div></div>",
            authenticatedPage.replace("/member/alice", "/member/someone_else"),
            authenticatedPage.replace("/member/alice", "https://evil.test/member/alice")
        ).forEach { assertNull(parseVerifiedWebLoginResponse(200, it, cookies)) }
    }

    @Test
    fun responseCookieRotationIsReturnedAndDeletedSessionFailsVerification() {
        // This pure test exercises our merge rules. Android OkHttp's Cookie.parse(Domain=...)
        // requires an application context to load its public suffix asset, absent in JVM tests.
        val rotated = Cookie.Builder().name("A2").value("rotated").domain("v2ex.com")
            .path("/").secure().httpOnly().build()
        val result = parseVerifiedWebLoginResponse(200, authenticatedPage, cookies, listOf(rotated))!!
        val session = result.cookies.single { it.name == "A2" }
        assertEquals("rotated", session.value)
        assertEquals("www.v2ex.com", session.domain)
        assertTrue(session.httpOnly)
        val deleted = Cookie.Builder().name("A2").value("").hostOnlyDomain("www.v2ex.com")
            .path("/").expiresAt(0L).build()
        assertNull(parseVerifiedWebLoginResponse(200, authenticatedPage, cookies, listOf(deleted)))
        val expired = Cookie.Builder().name("A2").value("expired").hostOnlyDomain("www.v2ex.com")
            .path("/").expiresAt(1L).build()
        assertNull(parseVerifiedWebLoginResponse(200, authenticatedPage, cookies, listOf(expired)))
    }

    @Test
    fun unrelatedResponseCookiesCannotReplaceVerifiedSession() {
        val foreign = Cookie.Builder().name("A2").value("foreign").domain("google.com").build()
        val nested = Cookie.Builder().name("A2").value("nested").domain("www.v2ex.com").path("/2fa").build()
        val result = parseVerifiedWebLoginResponse(200, authenticatedPage, cookies, listOf(foreign, nested))!!
        assertEquals("valid-session", result.cookies.single { it.name == "A2" }.value)
    }
}
