package com.example.sistemamovimiento

data class FakeEvent(
    val title: String,
    val location: String,
    val timestamp: String,
    val imageRes: Int? = null
)
