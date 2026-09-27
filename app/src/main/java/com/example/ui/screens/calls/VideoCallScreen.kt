package com.example.ui.screens.calls

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveCallState
import com.example.ui.components.AvatarView
import com.example.ui.theme.CallRed
import com.example.ui.theme.PrimaryBlue

@Composable
fun VideoCallScreen(
    callState: ActiveCallState,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleFlipCamera: () -> Unit
) {
    BackHandler {
        onEndCall()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "videoGlow")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_call_screen")
    ) {
        // Main Remote Video Feed (Simulation)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B).copy(alpha = 0.8f + 0.2f * gradientShift),
                            Color(0xFF0F172A)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AvatarView(
                    name = callState.contactName,
                    size = 110.dp,
                    isOnline = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = callState.contactName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (callState.isConnected) "Connected • 1080p HD" else "Connecting video stream...",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // Top Status Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val formattedDuration = String.format(
                    "%02d:%02d",
                    callState.durationSec / 60,
                    callState.durationSec % 60
                )
                Text(
                    text = if (callState.isConnected) formattedDuration else "Ringing...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quality Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryBlue.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HD Video",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Self View PiP (Floating local camera window)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 16.dp)
                .size(width = 110.dp, height = 155.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B))
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable { onToggleFlipCamera() }
                .testTag("video_pip_window"),
            contentAlignment = Alignment.Center
        ) {
            if (callState.isCameraOn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF334155),
                                    Color(0xFF1E293B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (callState.isFrontCamera) "Self (Front)" else "Back Camera",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "Camera Off",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Bottom Controls Bar (Frosted glass floating panel)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip Camera
                IconButton(
                    onClick = onToggleFlipCamera,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("video_flip_camera_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Flip Camera",
                        tint = Color.White
                    )
                }

                // Camera Toggle
                IconButton(
                    onClick = onToggleCamera,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (!callState.isCameraOn) Color.White else Color.White.copy(alpha = 0.15f))
                        .testTag("video_toggle_camera_btn")
                ) {
                    Icon(
                        imageVector = if (callState.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Toggle Camera",
                        tint = if (!callState.isCameraOn) Color.Black else Color.White
                    )
                }

                // Mute Mic Toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (callState.isMuted) Color.White else Color.White.copy(alpha = 0.15f))
                        .testTag("video_toggle_mute_btn")
                ) {
                    Icon(
                        imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Mic",
                        tint = if (callState.isMuted) Color.Black else Color.White
                    )
                }

                // Speaker Toggle
                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (callState.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.15f))
                        .testTag("video_toggle_speaker_btn")
                ) {
                    Icon(
                        imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Toggle Speaker",
                        tint = if (callState.isSpeakerOn) Color.Black else Color.White
                    )
                }

                // End Call Red Button
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = CallRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("video_end_call_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
