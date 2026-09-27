package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryBlue

@Composable
fun AvatarView(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    isAiAssistant: Boolean = false,
    hasStatusUpdate: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .testTag("avatar_$name"),
        contentAlignment = Alignment.Center
    ) {
        val outerModifier = if (hasStatusUpdate) {
            Modifier
                .size(size)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(PrimaryBlue, AccentCyan, AccentPurple, PrimaryBlue)
                    ),
                    shape = CircleShape
                )
        } else Modifier

        Box(
            modifier = outerModifier
                .size(if (hasStatusUpdate) size - 4.dp else size)
                .clip(CircleShape)
                .background(
                    if (isAiAssistant) {
                        Brush.linearGradient(listOf(AccentPurple, AccentCyan, PrimaryBlue))
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF3B82F6),
                                Color(0xFF1D4ED8)
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAiAssistant) {
                // Try rendering generated AI avatar drawable or fallback icon
                Image(
                    painter = painterResource(id = R.drawable.avatar_ai_bot_1790503258883),
                    contentDescription = "AI Assistant Avatar",
                    modifier = Modifier
                        .size(if (hasStatusUpdate) size - 4.dp else size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initials = name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()

                Text(
                    text = if (initials.isNotEmpty()) initials else "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            }
        }

        // Online green dot
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(size * 0.22f)
                        .clip(CircleShape)
                        .background(if (isAiAssistant) AccentPurple else OnlineGreen)
                )
            }
        }
    }
}
