package com.example.model

/**
 * State representation for the In-App WebView Browser screen.
 */
data class InAppBrowserState(
    val url: String,
    val title: String? = null
)
