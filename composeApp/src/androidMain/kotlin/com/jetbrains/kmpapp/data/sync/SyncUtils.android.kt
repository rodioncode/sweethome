package com.jetbrains.kmpapp.data.sync

import java.time.Instant
import java.util.UUID

actual fun nowIso(): String = Instant.now().toString()

actual fun generateId(): String = UUID.randomUUID().toString()
