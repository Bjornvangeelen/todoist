package com.dagplanner.app.data.repository

import com.dagplanner.app.data.google.GmailService
import com.dagplanner.app.data.model.EmailMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailRepository @Inject constructor(
    private val gmailService: GmailService
) {
    suspend fun fetchInbox(accountName: String, maxResults: Long = 50): Result<List<EmailMessage>> =
        gmailService.fetchInbox(accountName, maxResults)

    suspend fun fetchBody(accountName: String, messageId: String): Result<String> =
        gmailService.fetchBody(accountName, messageId)
}
