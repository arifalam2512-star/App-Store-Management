package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CallLogEntity
import com.example.data.local.ContactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.StatusEntity
import com.example.data.repository.SmartMsgRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveCallState(
    val contactId: Long,
    val contactName: String,
    val contactPhone: String,
    val isVideo: Boolean = false,
    val isConnected: Boolean = false,
    val durationSec: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraOn: Boolean = true,
    val isFrontCamera: Boolean = true
)

data class AiToolResult(
    val toolType: String = "",
    val inputText: String = "",
    val outputText: String = "",
    val isLoading: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmartMsgRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SmartMsgRepository(db)
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
        }
    }

    // Navigation
    private val _selectedTab = MutableStateFlow(0) // 0: Chats, 1: Calls, 2: Status, 3: AI Tools
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _activeChatContactId = MutableStateFlow<Long?>(null)
    val activeChatContactId: StateFlow<Long?> = _activeChatContactId.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    private val _activeStatusView = MutableStateFlow<StatusEntity?>(null)
    val activeStatusView: StateFlow<StatusEntity?> = _activeStatusView.asStateFlow()

    private val _showCreateStatusDialog = MutableStateFlow(false)
    val showCreateStatusDialog: StateFlow<Boolean> = _showCreateStatusDialog.asStateFlow()

    private val _showDialpad = MutableStateFlow(false)
    val showDialpad: StateFlow<Boolean> = _showDialpad.asStateFlow()

    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Data streams
    val contacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLogEntity>> = repository.allCallLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statuses: StateFlow<List<StatusEntity>> = repository.allStatuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeChatMessages: StateFlow<List<MessageEntity>> = _activeChatMessages.asStateFlow()

    private val _activeContact = MutableStateFlow<ContactEntity?>(null)
    val activeContact: StateFlow<ContactEntity?> = _activeContact.asStateFlow()

    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies: StateFlow<List<String>> = _smartReplies.asStateFlow()

    val isAiTyping: StateFlow<Boolean> = repository.isAiTyping.asStateFlow()

    // AI Tools State
    private val _aiToolState = MutableStateFlow(AiToolResult())
    val aiToolState: StateFlow<AiToolResult> = _aiToolState.asStateFlow()

    private var callTimerJob: Job? = null
    private var chatMessagesJob: Job? = null

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openChat(contactId: Long) {
        _activeChatContactId.value = contactId
        chatMessagesJob?.cancel()
        chatMessagesJob = viewModelScope.launch {
            repository.observeContact(contactId).collectLatest { contact ->
                _activeContact.value = contact
            }
        }
        viewModelScope.launch {
            repository.getMessages(contactId).collectLatest { msgs ->
                _activeChatMessages.value = msgs
                val lastMsg = msgs.lastOrNull()
                if (lastMsg != null && lastMsg.senderType != "USER") {
                    _smartReplies.value = repository.generateSmartReplies(lastMsg.content)
                } else if (msgs.isEmpty()) {
                    _smartReplies.value = listOf("Hey there! 👋", "How's your day going?", "Can we talk?")
                } else {
                    _smartReplies.value = emptyList()
                }
            }
        }
    }

    fun closeChat() {
        _activeChatContactId.value = null
        _activeContact.value = null
        _activeChatMessages.value = emptyList()
        _smartReplies.value = emptyList()
        chatMessagesJob?.cancel()
    }

    fun sendMessage(text: String, messageType: String = "TEXT", voiceDurationSec: Int = 0) {
        val contactId = _activeChatContactId.value ?: return
        if (text.isBlank() && messageType == "TEXT") return

        viewModelScope.launch {
            repository.sendMessage(
                contactId = contactId,
                text = text,
                senderType = "USER",
                messageType = messageType,
                voiceDurationSec = voiceDurationSec
            )
            _smartReplies.value = emptyList()
        }
    }

    fun reactToMessage(messageId: Long, reaction: String) {
        viewModelScope.launch {
            repository.addReaction(messageId, reaction)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    // Calling Logic
    fun startVoiceCall(contactId: Long) {
        viewModelScope.launch {
            val contact = repository.getContact(contactId) ?: return@launch
            _activeCall.value = ActiveCallState(
                contactId = contact.id,
                contactName = contact.name,
                contactPhone = contact.phone,
                isVideo = false,
                isConnected = false,
                durationSec = 0
            )
            startCallTimer(contact.id, isVideo = false)
        }
    }

    fun startVideoCall(contactId: Long) {
        viewModelScope.launch {
            val contact = repository.getContact(contactId) ?: return@launch
            _activeCall.value = ActiveCallState(
                contactId = contact.id,
                contactName = contact.name,
                contactPhone = contact.phone,
                isVideo = true,
                isConnected = false,
                durationSec = 0
            )
            startCallTimer(contact.id, isVideo = true)
        }
    }

    private fun startCallTimer(contactId: Long, isVideo: Boolean) {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            delay(1800) // Ringing simulation
            _activeCall.value = _activeCall.value?.copy(isConnected = true)
            while (true) {
                delay(1000)
                val current = _activeCall.value ?: break
                if (current.isConnected) {
                    _activeCall.value = current.copy(durationSec = current.durationSec + 1)
                }
            }
        }
    }

    fun endCall() {
        val current = _activeCall.value ?: return
        val duration = current.durationSec
        val contactId = current.contactId
        val isVideo = current.isVideo

        callTimerJob?.cancel()
        _activeCall.value = null

        viewModelScope.launch {
            repository.logCall(
                contactId = contactId,
                callType = if (isVideo) "VIDEO" else "VOICE",
                direction = "OUTGOING",
                durationSec = duration
            )
        }
    }

    fun toggleMuteCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleSpeakerCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isSpeakerOn = !it.isSpeakerOn)
        }
    }

    fun toggleCameraCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isCameraOn = !it.isCameraOn)
        }
    }

    fun toggleFlipCameraCall() {
        _activeCall.value = _activeCall.value?.let {
            it.copy(isFrontCamera = !it.isFrontCamera)
        }
    }

    // Status Features
    fun openStatusViewer(status: StatusEntity) {
        _activeStatusView.value = status
        viewModelScope.launch {
            repository.viewStatus(status.id)
        }
    }

    fun closeStatusViewer() {
        _activeStatusView.value = null
    }

    fun openCreateStatusDialog() {
        _showCreateStatusDialog.value = true
    }

    fun closeCreateStatusDialog() {
        _showCreateStatusDialog.value = false
    }

    fun postStatus(caption: String, bgHex: String) {
        if (caption.isBlank()) return
        viewModelScope.launch {
            repository.postStatus(caption, bgHex, isMine = true)
            _showCreateStatusDialog.value = false
        }
    }

    // Dialpad & New Contacts
    fun openDialpad() {
        _showDialpad.value = true
    }

    fun closeDialpad() {
        _showDialpad.value = false
    }

    fun openNewChatDialog() {
        _showNewChatDialog.value = true
    }

    fun closeNewChatDialog() {
        _showNewChatDialog.value = false
    }

    fun createAndOpenChat(name: String, phone: String) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            val id = repository.createContact(name, phone)
            _showNewChatDialog.value = false
            openChat(id)
        }
    }

    // AI Tools Hub Actions
    fun runAiRephrase(text: String, tone: String) {
        if (text.isBlank()) return
        _aiToolState.value = AiToolResult(toolType = "Rephrase ($tone)", inputText = text, isLoading = true)
        viewModelScope.launch {
            val result = repository.rephraseText(text, tone)
            _aiToolState.value = AiToolResult(toolType = "Rephrase ($tone)", inputText = text, outputText = result, isLoading = false)
        }
    }

    fun runAiTranslate(text: String, targetLang: String) {
        if (text.isBlank()) return
        _aiToolState.value = AiToolResult(toolType = "Translate to $targetLang", inputText = text, isLoading = true)
        viewModelScope.launch {
            val result = repository.translateText(text, targetLang)
            _aiToolState.value = AiToolResult(toolType = "Translate to $targetLang", inputText = text, outputText = result, isLoading = false)
        }
    }

    fun runAiSummarize(text: String) {
        if (text.isBlank()) return
        _aiToolState.value = AiToolResult(toolType = "Summarizer", inputText = text, isLoading = true)
        viewModelScope.launch {
            val result = repository.summarizeChatText(text)
            _aiToolState.value = AiToolResult(toolType = "Summarizer", inputText = text, outputText = result, isLoading = false)
        }
    }

    fun runAiGrammar(text: String) {
        if (text.isBlank()) return
        _aiToolState.value = AiToolResult(toolType = "Grammar & Polish", inputText = text, isLoading = true)
        viewModelScope.launch {
            val result = repository.polishGrammar(text)
            _aiToolState.value = AiToolResult(toolType = "Grammar & Polish", inputText = text, outputText = result, isLoading = false)
        }
    }

    fun runAiStatusIdea(mood: String) {
        _aiToolState.value = AiToolResult(toolType = "Status Creator ($mood)", inputText = mood, isLoading = true)
        viewModelScope.launch {
            val result = repository.generateStatusIdea(mood)
            _aiToolState.value = AiToolResult(toolType = "Status Creator ($mood)", inputText = mood, outputText = result, isLoading = false)
        }
    }

    fun clearAiToolResult() {
        _aiToolState.value = AiToolResult()
    }
}
