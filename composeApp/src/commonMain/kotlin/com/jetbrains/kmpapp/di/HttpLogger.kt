package com.jetbrains.kmpapp.di

import io.ktor.client.plugins.logging.Logger

expect fun createHttpLogger(): Logger
