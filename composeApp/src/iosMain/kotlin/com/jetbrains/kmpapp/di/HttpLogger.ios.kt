package com.jetbrains.kmpapp.di

import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.SIMPLE

actual fun createHttpLogger(): Logger = Logger.SIMPLE
