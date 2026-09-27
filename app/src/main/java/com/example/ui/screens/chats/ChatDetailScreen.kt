package com.example.ui.screens.chats

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ContactEntity
import com.example.data.local.MessageEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.SmartReplyChips
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    contact: ContactEntity,
    messages: List<MessageEntity>,
    smartReplies: List<String>,
    isAiTyping: Boolean = false,
    onBack: () -> Unit,
    onSendMessage: (String, String, Int) -> Unit,
    onReactMessage: (Long, String) -> Unit,
    onStartVoiceCall: (Long) -> Unit,
    onStartVideoCall: (Long) -> Unit,
    onRephraseRequest: (String, String) -> Unit
) {
    BackHandler {
        onBack()
    }

    var inputText by remember { mutableStateOf("") }
    var showAiMenu by remember { mutableStateOf(false) }
    var playingVoiceMsgId by remember { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isAiTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val triggerSend = {
        if (inputText.isNotBlank()) {
            val textToSend = inputText
            inputText = ""
            onSendMessage(textToSend, "TEXT", 0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* Contact info */ }
                    ) {
                        AvatarView(
                            name = contact.name,
                            size = 40.dp,
                            isOnline = contact.isOnline,
                            isAiAssistant = contact.isAiAssistant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (contact.isAiAssistant) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = AccentPurple,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isAiTyping) "typing..." else if (contact.isAiAssistant) "All-Knowledge AI Active" else if (contact.isOnline) "Online" else contact.lastSeen,
                                fontSize = 11.sp,
                                color = if (isAiTyping || contact.isOnline) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onStartVoiceCall(contact.id) },
                        modifier = Modifier.testTag("chat_voice_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = PrimaryBlue
                        )
                    }
                    IconButton(
                        onClick = { onStartVideoCall(contact.id) },
                        modifier = Modifier.testTag("chat_video_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Quick Topic Suggestion Chips for AI Assistant
            if (contact.isAiAssistant && messages.size <= 4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val promptIdeas = listOf(
                        "Tell me about Solar System 🪐",
                        "Write Python code 💻",
                        "Who was Albert Einstein? 🧠",
                        "Tell me a funny joke 😂",
                        "Shayari सुनाओ ✍️",
                        "Speed of Light kya hai? ⚡",
                        "Health tips for today 🥗"
                    )
                    promptIdeas.forEach { idea ->
                        SuggestionChip(
                            onClick = { onSendMessage(idea, "TEXT", 0) },
                            label = { Text(idea, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = AccentPurple.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubbleItem(
                        message = msg,
                        isAiChat = contact.isAiAssistant,
                        isPlayingVoice = playingVoiceMsgId == msg.id,
                        onToggleVoice = {
                            playingVoiceMsgId = if (playingVoiceMsgId == msg.id) null else msg.id
                        },
                        onReact = { reaction -> onReactMessage(msg.id, reaction) }
                    )
                }

                // Live AI Typing Animated Bubble
                if (isAiTyping && contact.isAiAssistant) {
                    item {
                        AiTypingIndicator()
                    }
                }
            }

            // Contextual Smart Replies
            AnimatedVisibility(
                visible = smartReplies.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SmartReplyChips(
                    replies = smartReplies,
                    onReplySelected = { reply ->
                        onSendMessage(reply, "TEXT", 0)
                    }
                )
            }

            // Bottom Message Input Box (Optimized for Fast Typing & Instant Send)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Tool Popup Menu button
                Box {
                    IconButton(
                        onClick = { showAiMenu = true },
                        modifier = Modifier.testTag("chat_ai_tools_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Message Tools",
                            tint = AccentPurple
                        )
                    }

                    DropdownMenu(
                        expanded = showAiMenu,
                        onDismissRequest = { showAiMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("✨ Rephrase Professional") },
                            onClick = {
                                showAiMenu = false
                                if (inputText.isNotBlank()) {
                                    onRephraseRequest(inputText, "Professional")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("😄 Rephrase Friendly") },
                            onClick = {
                                showAiMenu = false
                                if (inputText.isNotBlank()) {
                                    onRephraseRequest(inputText, "Friendly")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🇮🇳 Translate to Hindi") },
                            onClick = {
                                showAiMenu = false
                                if (inputText.isNotBlank()) {
                                    onRephraseRequest(inputText, "Hindi")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🇺🇸 Translate to English") },
                            onClick = {
                                showAiMenu = false
                                if (inputText.isNotBlank()) {
                                    onRephraseRequest(inputText, "English")
                                }
                            }
                        )
                    }
                }

                // Text Input Field with Fast Keyboard Send Handling
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { triggerSend() }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    placeholder = {
                        Text(
                            text = if (contact.isAiAssistant) "Ask Aura AI anything in the world..." else "Type a message...",
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedBorderColor = PrimaryBlue.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                if (inputText.isNotBlank()) {
                    // Send button
                    IconButton(
                        onClick = { triggerSend() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Voice Note Record / Send Simulator
                    IconButton(
                        onClick = {
                            onSendMessage("Voice note (0:05)", "VOICE", 5)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .testTag("chat_voice_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Voice Note",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(400, delayMillis = 140, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(400, delayMillis = 280, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("ai_typing_indicator")
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AccentPurple,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Aura AI is answering",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.offset(y = dot1.dp).size(5.dp).clip(CircleShape).background(AccentPurple))
            Box(Modifier.offset(y = dot2.dp).size(5.dp).clip(CircleShape).background(AccentPurple))
            Box(Modifier.offset(y = dot3.dp).size(5.dp).clip(CircleShape).background(AccentPurple))
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    isAiChat: Boolean,
    isPlayingVoice: Boolean,
    onToggleVoice: () -> Unit,
    onReact: (String) -> Unit
) {
    val isUser = message.senderType == "USER"
    val isAi = message.senderType == "AI"
    var showReactionPicker by remember { mutableStateOf(false) }

    val bubbleAlignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_bubble_${message.id}"),
        horizontalAlignment = bubbleAlignment
    ) {
        Box {
            Card(
                shape = bubbleShape,
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isUser -> PrimaryBlue
                        isAi -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 320.dp)
                    .clickable { showReactionPicker = !showReactionPicker }
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (isAi) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = AccentPurple,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Aura AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }
                    }

                    if (message.messageType == "VOICE") {
                        // Voice Note player UI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = onToggleVoice,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isUser) Color.White.copy(alpha = 0.25f) else PrimaryBlue.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = if (isPlayingVoice) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = if (isUser) Color.White else PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                // Waveform simulation
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val waveHeights = listOf(10, 18, 14, 24, 16, 20, 12, 22, 14, 8)
                                    waveHeights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(h.dp)
                                                .background(
                                                    if (isUser) Color.White.copy(alpha = 0.8f) else PrimaryBlue.copy(alpha = 0.7f),
                                                    RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isPlayingVoice) "Playing 0:0${message.voiceDurationSec}" else "Voice Note 0:0${message.voiceDurationSec}",
                                    fontSize = 11.sp,
                                    color = if (isUser) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = message.content,
                            fontSize = 15.sp,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
                        Text(
                            text = timeStr,
                            fontSize = 10.sp,
                            color = if (isUser) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (isUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reaction Badge or Reaction Picker
        if (message.reaction.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, start = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = message.reaction, fontSize = 12.sp)
            }
        }

        if (showReactionPicker) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val reactions = listOf("❤️", "👍", "🔥", "😂", "😮", "🙏")
                reactions.forEach { r ->
                    Text(
                        text = r,
                        fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            onReact(r)
                            showReactionPicker = false
                        }
                    )
                }
            }
        }
    }
}
