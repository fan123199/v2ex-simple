package im.fdx.v2ex.ui.login

import im.fdx.v2ex.data.model.Member
import im.fdx.v2ex.data.network.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

data class WebLoginResult(val member: Member, val cookies: List<Cookie>)

private val webLoginUrl = "https://www.v2ex.com/".toHttpUrl()
private val webLoginHosts = setOf("www.v2ex.com", "v2ex.com", "edge.v2ex.com")
private val cookieNamePattern = Regex("[!#$%&'*+.^_`|~0-9a-zA-Z-]+")

// Keep candidate browser credentials out of the app's shared cookie jar and HTTP logs.
private val webLoginClient by lazy {
    OkHttpClient.Builder()
        .cookieJar(CookieJar.NO_COOKIES)
        .followRedirects(false)
        .followSslRedirects(false)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()
}

fun isTrustedWebLoginUrl(pageUrl: String): Boolean {
    val url = pageUrl.toHttpUrlOrNull() ?: return false
    return url.isHttps && url.port == 443 && url.host in webLoginHosts &&
        url.username.isEmpty() && url.password.isEmpty()
}

fun hasWebLoginSessionCookie(cookieHeader: String): Boolean =
    parseWebLoginCookies(cookieHeader).any { it.name == "A2" && it.hasNonemptyValue() }

/** Returns a verified session for the caller to persist; never changes the app's login state. */
suspend fun verifyWebLogin(pageUrl: String, cookieHeader: String): WebLoginResult? {
    if (!isTrustedWebLoginUrl(pageUrl)) return null
    val cookies = parseWebLoginCookies(cookieHeader)
    if (cookies.none { it.name == "A2" && it.hasNonemptyValue() }) return null

    return withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(webLoginUrl)
            .header("Cookie", cookies.joinToString("; ") { "${it.name}=${it.value}" })
            .header("Cache-Control", "no-cache")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.82 Safari/537.36")
            .build()
        webLoginClient.newCall(request).execute().use { response ->
            // In particular, a redirect to /2fa is not a completed login.
            if (response.code != 200) return@use null
            parseVerifiedWebLoginResponse(
                statusCode = response.code,
                html = response.body.string(),
                cookies = cookies,
                responseCookies = Cookie.parseAll(webLoginUrl, response.headers)
            )
        }
    }
}

internal fun parseWebLoginCookies(cookieHeader: String): List<Cookie> {
    if (cookieHeader.any { it.code < 0x20 || it.code > 0x7e }) return emptyList()
    val values = linkedMapOf<String, String>()
    for (part in cookieHeader.split(';')) {
        val field = part.trim()
        if (field.isEmpty()) continue
        val separator = field.indexOf('=')
        if (separator <= 0) return emptyList()
        val name = field.substring(0, separator).trim()
        val value = field.substring(separator + 1).trim()
        // Cookie values may have paired outer quotes, as Tornado's signed A2 cookie does.
        // Validate their contents but preserve the original value when forwarding the session.
        val content = value.removeSurrounding("\"")
        if (!cookieNamePattern.matches(name) || content.any { it == '"' || it == '\\' || it == ',' || it == ' ' }) {
            return emptyList()
        }
        // The browser header omits paths; differing duplicate values cannot be safely disambiguated.
        if (name in values && values[name] != value) return emptyList()
        values[name] = value
    }
    return values.map { (name, value) -> canonicalCookie(name, value) }
}

internal fun parseVerifiedWebLoginResponse(
    statusCode: Int,
    html: String,
    cookies: List<Cookie>,
    responseCookies: List<Cookie> = emptyList(),
    nowMillis: Long = System.currentTimeMillis()
): WebLoginResult? {
    if (statusCode != 200) return null
    val document = Jsoup.parse(html, webLoginUrl.toString())
    if (document.select("form[action]").any {
            webLoginUrl.resolve(it.attr("action"))?.encodedPath?.trimEnd('/') == "/2fa"
        }) return null

    val member = Parser(html).getMember()
    if (member.username.isBlank()) return null
    val accountCell = document.getElementById("Rightbar")?.getElementsByClass("cell")?.first()
        ?: return null
    // getMember() alone accepts any first image alt text, including an anonymous sidebar advert.
    val hasOwnProfile = accountCell.select("a[href]").any { link ->
        val url = webLoginUrl.resolve(link.attr("href"))
        url != null && isTrustedWebLoginUrl(url.toString()) &&
            url.pathSegments == listOf("member", member.username)
    }
    if (!hasOwnProfile) return null

    val verifiedCookies = cookies.associateByTo(linkedMapOf()) { it.name }
    responseCookies.forEach { cookie ->
        if (cookie.matches(webLoginUrl)) {
            if (cookie.expiresAt <= nowMillis || !cookie.hasNonemptyValue()) {
                verifiedCookies.remove(cookie.name)
            } else {
                verifiedCookies[cookie.name] = canonicalCookie(cookie.name, cookie.value, cookie)
            }
        }
    }
    if (verifiedCookies["A2"]?.hasNonemptyValue() != true) return null
    return WebLoginResult(member, verifiedCookies.values.toList())
}

private fun Cookie.hasNonemptyValue(): Boolean = value.isNotEmpty() && value != "\"\""

private fun canonicalCookie(name: String, value: String, source: Cookie? = null): Cookie =
    Cookie.Builder()
        .name(name)
        .value(value)
        .hostOnlyDomain(webLoginUrl.host)
        .path("/")
        .secure()
        .apply {
            if (source?.httpOnly == true) httpOnly()
            if (source?.persistent == true) expiresAt(source.expiresAt)
        }
        .build()
