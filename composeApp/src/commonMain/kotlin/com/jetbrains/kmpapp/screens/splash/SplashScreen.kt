package com.jetbrains.kmpapp.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isAuthenticated: Boolean,
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGuestLogin: () -> Unit,
) {
    var animIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        animIn = true
    }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            delay(800)
            onNavigateToMain()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (animIn) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "splash_alpha",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (animIn) 0f else 24f,
        animationSpec = tween(durationMillis = 500),
        label = "splash_offset",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, PrimaryGreenDark),
                )
            ),
    ) {
        // Decorative circles — top right
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 80.dp, y = (-80).dp)
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.06f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 40.dp, y = (-40).dp)
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.08f), CircleShape),
        )
        // Decorative circles — bottom left
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-60).dp, y = 100.dp)
                .align(Alignment.BottomStart)
                .background(PrimaryGreenDark.copy(alpha = 0.5f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .align(Alignment.BottomStart)
                .background(PrimaryGreenDark.copy(alpha = 0.4f), CircleShape),
        )

        // Logo + title — center
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alpha)
                .offset(y = offsetY.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                ) {}
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.20f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🏠", fontSize = 36.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "SweetHome",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ваш дом в порядке",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.75f),
            )
        }

        // Buttons — bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .alpha(alpha)
                .offset(y = offsetY.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = PrimaryGreen,
                ),
            ) {
                Text(
                    text = "Войти",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.4f),
                ),
            ) {
                Text(
                    text = "Начать бесплатно",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            TextButton(onClick = onGuestLogin) {
                Text(
                    text = "Войти как гость →",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }

        Text(
            text = "v 1.0.0",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
        )
    }
}
