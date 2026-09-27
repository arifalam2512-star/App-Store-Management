package com.example.ui.screens.calls

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CallLogEntity
import com.example.data.local.ContactEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsListScreen(
    callLogs: List<CallLogEntity>,
    contacts: List<ContactEntity>,
    onStartVoiceCall: (Long) -> Unit,
    onStartVideoCall: (Long) -> Unit,
    onOpenDialpad: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceLogs = callLogs.filter { it.callType == "VOICE" }
    val contactsMap = contacts.associateBy { it.id }

    Box(modifier = modifier.fillMaxSize()) {
        if (voiceLogs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No recent voice calls",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the dialpad button below to place a voice call",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("calls_list_lazy_column")
            ) {
                item {
                    Text(
                        text = "Recent Voice Calls",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(voiceLogs, key = { it.id }) { log ->
                    val contact = contactsMap[log.contactId]
                    val contactName = contact?.name ?: "Unknown"

                    CallLogRowItem(
                        log = log,
                        contactName = contactName,
                        isOnline = contact?.isOnline ?: false,
                        onVoiceCall = { onStartVoiceCall(log.contactId) },
                        onVideoCall = { onStartVideoCall(log.contactId) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Dialpad FAB
        FloatingActionButton(
            onClick = onOpenDialpad,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_open_dialpad"),
            containerColor = PrimaryBlue,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Dialpad,
                contentDescription = "Open Dialpad"
            )
        }
    }
}

@Composable
fun CallLogRowItem(
    log: CallLogEntity,
    contactName: String,
    isOnline: Boolean,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    val isMissed = log.direction == "MISSED"
    val isIncoming = log.direction == "INCOMING"
    val isVideo = log.callType == "VIDEO"

    val directionIcon = when {
        isMissed -> Icons.AutoMirrored.Filled.CallMissed
        isIncoming -> Icons.AutoMirrored.Filled.CallReceived
        else -> Icons.AutoMirrored.Filled.CallMade
    }

    val directionColor = when {
        isMissed -> CallRed
        isIncoming -> CallGreen
        else -> PrimaryBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (isVideo) onVideoCall else onVoiceCall)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("call_log_item_${log.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            name = contactName,
            size = 50.dp,
            isOnline = isOnline
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contactName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (isMissed) CallRed else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = directionIcon,
                    contentDescription = log.direction,
                    tint = directionColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val timeStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.timestamp))
                val durationStr = if (log.durationSec > 0) " (${log.durationSec / 60}m ${log.durationSec % 60}s)" else ""
                Text(
                    text = "$timeStr$durationStr",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Call back buttons
        Row {
            IconButton(
                onClick = onVoiceCall,
                modifier = Modifier.testTag("call_back_voice_${log.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Voice Call",
                    tint = PrimaryBlue
                )
            }
            IconButton(
                onClick = onVideoCall,
                modifier = Modifier.testTag("call_back_video_${log.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video Call",
                    tint = PrimaryBlue
                )
            }
        }
    }
}
