package com.dagplanner.app.data.model

data class EmailMessage(
    val id: String,
    val threadId: String,
    val subject: String,
    val from: String,
    val fromName: String?,
    val to: String?,
    val date: Long,
    val snippet: String,
    val isRead: Boolean,
    val body: String? = null,
)
