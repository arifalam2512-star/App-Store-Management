package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.screens.aitools.AiToolsHubScreen
import com.example.ui.screens.calls.CallsListScreen
import com.example.ui.screens.calls.DialpadBottomSheet
import com.example.ui.screens.calls.VideoCallScreen
import com.example.ui.screens.calls.VideoCallTabScreen
import com.example.ui.screens.calls.VoiceCallScreen
import com.example.ui.screens.chats.ChatDetailScreen
import com.example.ui.screens.chats.ChatsListScreen
import com.example.ui.screens.chats.NewChatDialog
import com.example.ui.screens.status.CreateStatusDialog
import com.example.ui.screens.status.StatusListScreen
import com.example.ui.screens.status.StatusViewerScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeChatContactId by viewModel.activeChatContactId.collectAsStateWithLifecycle()
    val activeContact by viewModel.activeContact.collectAsStateWithLifecycle()
    val activeChatMessages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val smartReplies by viewModel.smartReplies.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val activeStatusView by viewModel.activeStatusView.collectAsStateWithLifecycle()
    val showCreateStatusDialog by viewModel.showCreateStatusDialog.collectAsStateWithLifecycle()
    val showDialpad by viewModel.showDialpad.collectAsStateWithLifecycle()
    val showNewChatDialog by viewModel.showNewChatDialog.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val statuses by viewModel.statuses.collectAsStateWithLifecycle()
    val aiToolState by viewModel.aiToolState.collectAsStateWithLifecycle()
    val isAiTyping by viewModel.isAiTyping.collectAsStateWithLifecycle()

    // 1. Fullscreen Video Call overlay
    if (activeCall != null && activeCall!!.isVideo) {
        VideoCallScreen(
            callState = activeCall!!,
            onEndCall = { viewModel.endCall() },
            onToggleMute = { viewModel.toggleMuteCall() },
            onToggleSpeaker = { viewModel.toggleSpeakerCall() },
            onToggleCamera = { viewModel.toggleCameraCall() },
            onToggleFlipCamera = { viewModel.toggleFlipCameraCall() }
        )
        return
    }

    // 2. Fullscreen Voice Call overlay
    if (activeCall != null && !activeCall!!.isVideo) {
        VoiceCallScreen(
            callState = activeCall!!,
            onEndCall = { viewModel.endCall() },
            onToggleMute = { viewModel.toggleMuteCall() },
            onToggleSpeaker = { viewModel.toggleSpeakerCall() },
            onSwitchToVideo = {
                val contactId = activeCall!!.contactId
                viewModel.endCall()
                viewModel.startVideoCall(contactId)
            }
        )
        return
    }

    // 3. Fullscreen Status Viewer overlay
    if (activeStatusView != null) {
        StatusViewerScreen(
            status = activeStatusView!!,
            onClose = { viewModel.closeStatusViewer() },
            onReply = { reply ->
                // Send reply to contact
                val contactId = if (activeStatusView!!.contactId != 0L) activeStatusView!!.contactId else 2L
                viewModel.openChat(contactId)
                viewModel.sendMessage(
                    "Replied to status: \"${activeStatusView!!.caption}\"\n\n$reply"
                )
            }
        )
        return
    }

    // 4. Chat Detail Screen
    if (activeChatContactId != null && activeContact != null) {
        ChatDetailScreen(
            contact = activeContact!!,
            messages = activeChatMessages,
            smartReplies = smartReplies,
            onBack = { viewModel.closeChat() },
            onSendMessage = { text, type, duration ->
                viewModel.sendMessage(text, type, duration)
            },
            onReactMessage = { id, emoji ->
                viewModel.reactToMessage(id, emoji)
            },
            onStartVoiceCall = { id -> viewModel.startVoiceCall(id) },
            onStartVideoCall = { id -> viewModel.startVideoCall(id) },
            onRephraseRequest = { text, tone ->
                viewModel.runAiRephrase(text, tone)
                viewModel.selectTab(4) // switch to AI tools
            }
        )
        return
    }

    // 5. Main Root Navigation Container
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SmartMsg",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.selectTab(4) },
                        modifier = Modifier.testTag("topbar_ai_tools_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Tools",
                            tint = AccentPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                // Chats Tab
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = PrimaryBlue) {
                                    Text("3", color = Color.White)
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats")
                        }
                    },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_chats")
                )

                // Voice Calls Tab
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        Icon(Icons.Default.Call, contentDescription = "Calls")
                    },
                    label = { Text("Calls") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_calls")
                )

                // Separate Video Call Tab
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    },
                    label = { Text("Video") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_video_call")
                )

                // Status Tab
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = {
                        Icon(Icons.Default.DonutLarge, contentDescription = "Status")
                    },
                    label = { Text("Status") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_status")
                )

                // AI Tools Tab
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Tools")
                    },
                    label = { Text("AI Tools") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentPurple,
                        indicatorColor = AccentPurple.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_ai_tools")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> ChatsListScreen(
                    contacts = contacts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onContactClick = { id -> viewModel.openChat(id) },
                    onNewChatClick = { viewModel.openNewChatDialog() }
                )
                1 -> CallsListScreen(
                    callLogs = callLogs,
                    contacts = contacts,
                    onStartVoiceCall = { id -> viewModel.startVoiceCall(id) },
                    onStartVideoCall = { id -> viewModel.startVideoCall(id) },
                    onOpenDialpad = { viewModel.openDialpad() }
                )
                2 -> VideoCallTabScreen(
                    contacts = contacts,
                    callLogs = callLogs,
                    onStartVideoCall = { id -> viewModel.startVideoCall(id) }
                )
                3 -> StatusListScreen(
                    statuses = statuses,
                    onOpenStatusViewer = { status -> viewModel.openStatusViewer(status) },
                    onOpenCreateStatus = { viewModel.openCreateStatusDialog() }
                )
                4 -> AiToolsHubScreen(
                    aiToolState = aiToolState,
                    onRephrase = { text, tone -> viewModel.runAiRephrase(text, tone) },
                    onTranslate = { text, lang -> viewModel.runAiTranslate(text, lang) },
                    onSummarize = { text -> viewModel.runAiSummarize(text) },
                    onPolishGrammar = { text -> viewModel.runAiGrammar(text) },
                    onGenerateStatus = { mood -> viewModel.runAiStatusIdea(mood) },
                    onClearResult = { viewModel.clearAiToolResult() }
                )
            }
        }
    }

    // Dialogs & BottomSheets
    if (showCreateStatusDialog) {
        CreateStatusDialog(
            onDismiss = { viewModel.closeCreateStatusDialog() },
            onPostStatus = { text, bgHex -> viewModel.postStatus(text, bgHex) },
            onGenerateAiStatus = { mood -> viewModel.runAiStatusIdea(mood) }
        )
    }

    if (showDialpad) {
        DialpadBottomSheet(
            onDismiss = { viewModel.closeDialpad() },
            onCallNumber = { num, isVideo ->
                viewModel.closeDialpad()
                if (isVideo) viewModel.startVideoCall(2L)
                else viewModel.startVoiceCall(2L)
            }
        )
    }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { viewModel.closeNewChatDialog() },
            onCreateContact = { name, phone ->
                viewModel.createAndOpenChat(name, phone)
            }
        )
    }
}
