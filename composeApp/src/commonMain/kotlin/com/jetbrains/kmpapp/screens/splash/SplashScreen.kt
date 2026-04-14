package com.jetbrains.kmpapp.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, PrimaryGreenDark),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // Concentric circles with house icon
            Box(contentAlignment = Alignment.Center) {
                // Outer circle
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                ) {}
                // Middle circle
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.20f),
                ) {}
                // Inner circle
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.30f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🏠",
                            fontSize = 30.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "SweetHome",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ваш дом в порядке",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
        }

        // Version at bottom
        Text(
            text = "v 1.0.0",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        )
    }
}
