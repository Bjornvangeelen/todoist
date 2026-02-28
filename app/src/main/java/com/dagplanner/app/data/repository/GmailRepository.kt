package com.dagplanner.app.data.repository

import com.dagplanner.app.data.google.GmailService
import com.dagplanner.app.data.model.EmailMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailRepository @Inject constructor(
    private val gmailService: GmailService
) {
    suspend fun fetchInbox(
        accountName: String,
        maxResults: Long = 25,
        pageToken: String? = null,
    ): Result<GmailService.InboxPage> = gmailService.fetchInbox(accountName, maxResults, pageToken)

    suspend fun fetchBody(accountName: String, messageId: String): Result<String> =
        gmailService.fetchBody(accountName, messageId)

    suspend fun trashMessage(accountName: String, messageId: String): Result<Unit> =
        gmailService.trashMessage(accountName, messageId)

    suspend fun sendReply(accountName: String, originalEmail: EmailMessage, replyText: String): Result<Unit> =
        gmailService.sendReply(accountName, originalEmail, replyText)
}
