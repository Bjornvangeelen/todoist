package com.dagplanner.app.data.google

import android.content.Context
import android.util.Base64
import com.dagplanner.app.data.model.EmailMessage
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.MessagePart
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val APP_NAME = "DagPlanner"
        val SCOPES = listOf(GmailScopes.GMAIL_READONLY)
    }

    private fun buildGmailService(accountName: String): Gmail {
        val credential = GoogleAccountCredential.usingOAuth2(context, SCOPES).apply {
            selectedAccountName = accountName
        }
        return Gmail.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(APP_NAME).build()
    }

    /**
     * Haalt de inbox op met metadata (afzender, onderwerp, datum, snippet).
     */
    suspend fun fetchInbox(
        accountName: String,
        maxResults: Long = 50,
    ): Result<List<EmailMessage>> = withContext(Dispatchers.IO) {
        try {
            val service = buildGmailService(accountName)
            val listResponse = service.users().messages().list("me")
                .setLabelIds(listOf("INBOX"))
                .setMaxResults(maxResults)
                .execute()

            val messages = listResponse.messages ?: emptyList()
            val emails = messages.mapNotNull { msg ->
                try {
                    val full = service.users().messages().get("me", msg.id)
                        .setFormat("METADATA")
                        .setMetadataHeaders(listOf("Subject", "From", "Date", "To"))
                        .execute()
                    mapMessage(full)
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(emails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Haalt de volledige berichttekst op.
     */
    suspend fun fetchBody(
        accountName: String,
        messageId: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = buildGmailService(accountName)
            val msg = service.users().messages().get("me", messageId)
                .setFormat("FULL")
                .execute()
            val body = extractBody(msg.payload) ?: ""
            Result.success(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Hulpfuncties ────────────────────────────────────────────────────────

    private fun mapMessage(msg: com.google.api.services.gmail.model.Message): EmailMessage? {
        val headers = msg.payload?.headers ?: return null
        val h = headers.associateBy({ it.name.lowercase() }, { it.value })

        val subject = h["subject"] ?: "(Geen onderwerp)"
        val fromRaw = h["from"] ?: "Onbekend"
        val to = h["to"]
        val dateStr = h["date"] ?: ""
        val (fromName, fromEmail) = parseFrom(fromRaw)
        val date = parseDate(dateStr)
        val isRead = !(msg.labelIds?.contains("UNREAD") ?: false)

        return EmailMessage(
            id = msg.id,
            threadId = msg.threadId ?: "",
            subject = subject,
            from = fromEmail,
            fromName = fromName,
            to = to,
            date = date,
            snippet = msg.snippet ?: "",
            isRead = isRead,
        )
    }

    private fun extractBody(payload: MessagePart?): String? {
        if (payload == null) return null
        when (payload.mimeType) {
            "text/plain" -> return payload.body?.data?.let { decodeBase64(it) }
            "text/html" -> return payload.body?.data?.let { html ->
                android.text.Html.fromHtml(
                    decodeBase64(html),
                    android.text.Html.FROM_HTML_MODE_COMPACT
                ).toString()
            }
        }
        val parts = payload.parts ?: return null
        // Geef voorkeur aan text/plain, dan text/html, dan recursief
        parts.firstOrNull { it.mimeType == "text/plain" }
            ?.let { extractBody(it) }
            ?.let { return it }
        parts.firstOrNull { it.mimeType == "text/html" }
            ?.let { extractBody(it) }
            ?.let { return it }
        parts.forEach { part ->
            extractBody(part)?.let { return it }
        }
        return null
    }

    private fun decodeBase64(data: String): String {
        val bytes = Base64.decode(data, Base64.URL_SAFE)
        return String(bytes, Charsets.UTF_8)
    }

    private fun parseFrom(from: String): Pair<String?, String> {
        val match = Regex("""^"?([^"<]+)"?\s*<([^>]+)>$""").find(from.trim())
        return if (match != null) {
            Pair(match.groupValues[1].trim().trim('"'), match.groupValues[2].trim())
        } else {
            Pair(null, from.trim())
        }
    }

    private fun parseDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        val formats = listOf(
            "EEE, d MMM yyyy HH:mm:ss Z",
            "EEE, d MMM yyyy HH:mm:ss z",
            "d MMM yyyy HH:mm:ss Z",
        )
        formats.forEach { fmt ->
            try {
                return SimpleDateFormat(fmt, Locale.ENGLISH).parse(dateStr)?.time
                    ?: return@forEach
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }
}
