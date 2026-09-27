package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CallLogEntity
import com.example.data.local.ContactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.StatusEntity
import com.example.data.remote.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SmartMsgRepository(
    private val database: AppDatabase,
    private val geminiService: GeminiService = GeminiService()
) {
    private val contactDao = database.contactDao()
    private val messageDao = database.messageDao()
    private val callLogDao = database.callLogDao()
    private val statusDao = database.statusDao()

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val allCallLogs: Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()
    val allStatuses: Flow<List<StatusEntity>> = statusDao.getAllStatuses()
    val isAiTyping = MutableStateFlow(false)

    fun getMessages(contactId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesForContact(contactId)

    fun observeContact(contactId: Long): Flow<ContactEntity?> =
        contactDao.observeContactById(contactId)

    suspend fun getContact(contactId: Long): ContactEntity? =
        contactDao.getContactById(contactId)

    suspend fun initializeSeedDataIfNeeded() {
        if (contactDao.getCount() == 0) {
            val aiContact = ContactEntity(
                id = 1L,
                name = "Aura AI Assistant",
                phone = "AI-101",
                avatarUrl = "",
                statusMessage = "Always active • Your personal Smart AI companion ✨",
                isOnline = true,
                isAiAssistant = true,
                lastSeen = "Online"
            )

            val contacts = listOf(
                aiContact,
                ContactEntity(
                    id = 2L,
                    name = "Arif Alam",
                    phone = "+91 98765 43210",
                    statusMessage = "Developing smart Android apps 🚀",
                    isOnline = true,
                    isAiAssistant = false,
                    lastSeen = "Online"
                ),
                ContactEntity(
                    id = 3L,
                    name = "Priya Sharma",
                    phone = "+91 91234 56789",
                    statusMessage = "In design sprint, text or video call 🎨",
                    isOnline = true,
                    isAiAssistant = false,
                    lastSeen = "Online"
                ),
                ContactEntity(
                    id = 4L,
                    name = "Rahul Verma",
                    phone = "+91 99887 76655",
                    statusMessage = "Available for audio calls",
                    isOnline = false,
                    isAiAssistant = false,
                    lastSeen = "10 mins ago"
                ),
                ContactEntity(
                    id = 5L,
                    name = "Neha Kapoor",
                    phone = "+91 98112 23344",
                    statusMessage = "Exploring mountain trails 🏔️",
                    isOnline = true,
                    isAiAssistant = false,
                    lastSeen = "Online"
                ),
                ContactEntity(
                    id = 6L,
                    name = "Amit Patel",
                    phone = "+91 97654 32190",
                    statusMessage = "Coding late nights ☕",
                    isOnline = false,
                    isAiAssistant = false,
                    lastSeen = "Yesterday"
                )
            )

            contactDao.insertContacts(contacts)

            // Seed initial messages
            val now = System.currentTimeMillis()
            val initialMessages = listOf(
                MessageEntity(
                    contactId = 1L,
                    senderType = "AI",
                    content = "Namaste & Welcome! 🌟 I'm Aura, your built-in Smart AI. I can rephrase messages, translate languages, write statuses, or chat anytime. How can I assist you?",
                    timestamp = now - 3600000 * 3
                ),
                MessageEntity(
                    contactId = 2L,
                    senderType = "CONTACT",
                    content = "Hey! Did you check out the new video call and AI features in SmartMsg?",
                    timestamp = now - 1800000,
                    reaction = "🔥"
                ),
                MessageEntity(
                    contactId = 2L,
                    senderType = "USER",
                    content = "Yes! The UI looks super clean and the smart replies work like magic.",
                    timestamp = now - 1500000,
                    reaction = "❤️"
                ),
                MessageEntity(
                    contactId = 3L,
                    senderType = "CONTACT",
                    content = "Can we jump on a quick video call at 4 PM to discuss the new design?",
                    timestamp = now - 7200000
                ),
                MessageEntity(
                    contactId = 4L,
                    senderType = "CONTACT",
                    content = "Sent you the project update document. Let me know when you review it.",
                    timestamp = now - 14400000
                ),
                MessageEntity(
                    contactId = 5L,
                    senderType = "CONTACT",
                    content = "Check out my new status! The sunset in Himachal is breathtaking.",
                    timestamp = now - 28800000
                )
            )
            messageDao.insertMessages(initialMessages)

            // Seed Call Logs
            val initialCalls = listOf(
                CallLogEntity(
                    contactId = 2L,
                    callType = "VIDEO",
                    direction = "OUTGOING",
                    timestamp = now - 3600000,
                    durationSec = 245
                ),
                CallLogEntity(
                    contactId = 3L,
                    callType = "VOICE",
                    direction = "INCOMING",
                    timestamp = now - 10800000,
                    durationSec = 412
                ),
                CallLogEntity(
                    contactId = 4L,
                    callType = "VOICE",
                    direction = "MISSED",
                    timestamp = now - 86400000,
                    durationSec = 0
                ),
                CallLogEntity(
                    contactId = 5L,
                    callType = "VIDEO",
                    direction = "INCOMING",
                    timestamp = now - 172800000,
                    durationSec = 180
                )
            )
            callLogDao.insertCallLogs(initialCalls)

            // Seed Statuses
            val initialStatuses = listOf(
                StatusEntity(
                    contactId = 0L,
                    authorName = "My Status",
                    caption = "Trying out the new SmartMsg app! 🚀 Loving the AI features.",
                    backgroundHex = "#1E40AF",
                    timestamp = now - 1800000,
                    isMine = true,
                    viewsCount = 14
                ),
                StatusEntity(
                    contactId = 2L,
                    authorName = "Arif Alam",
                    caption = "“The best way to predict the future is to invent it.” ✨ Working on cool Android apps.",
                    backgroundHex = "#047857",
                    timestamp = now - 3600000,
                    isMine = false,
                    viewsCount = 28
                ),
                StatusEntity(
                    contactId = 3L,
                    authorName = "Priya Sharma",
                    caption = "🎨 Design sprint underway! Brainstorming intuitive communication experiences.",
                    backgroundHex = "#7C3AED",
                    timestamp = now - 7200000,
                    isMine = false,
                    viewsCount = 35
                ),
                StatusEntity(
                    contactId = 5L,
                    authorName = "Neha Kapoor",
                    caption = "🏔️ Breathing the fresh mountain air in Himachal. Nature is healing!",
                    backgroundHex = "#B91C1C",
                    timestamp = now - 14400000,
                    isMine = false,
                    viewsCount = 42
                )
            )
            statusDao.insertStatuses(initialStatuses)
        }
    }

    suspend fun sendMessage(
        contactId: Long,
        text: String,
        senderType: String = "USER",
        messageType: String = "TEXT",
        voiceDurationSec: Int = 0
    ) {
        val userMsg = MessageEntity(
            contactId = contactId,
            senderType = senderType,
            content = text,
            timestamp = System.currentTimeMillis(),
            messageType = messageType,
            voiceDurationSec = voiceDurationSec
        )
        messageDao.insertMessage(userMsg)

        val contact = contactDao.getContactById(contactId)
        if (contact != null && contact.isAiAssistant) {
            isAiTyping.value = true
            // Trigger AI response asynchronously with zero lag
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val systemPrompt = "You are Aura AI, an ultra-fast, world-class conversational AI with comprehensive world knowledge like ChatGPT. Answer directly, intelligently, and helpfully."
                    val aiReplyText = geminiService.generateText(text, systemPrompt)
                    val aiMsg = MessageEntity(
                        contactId = contactId,
                        senderType = "AI",
                        content = aiReplyText,
                        timestamp = System.currentTimeMillis(),
                        messageType = "TEXT"
                    )
                    messageDao.insertMessage(aiMsg)
                } finally {
                    isAiTyping.value = false
                }
            }
        }
    }

    suspend fun addReaction(messageId: Long, reaction: String) {
        messageDao.updateReaction(messageId, reaction)
    }

    suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteMessage(messageId)
    }

    suspend fun logCall(contactId: Long, callType: String, direction: String, durationSec: Int) {
        val call = CallLogEntity(
            contactId = contactId,
            callType = callType,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            durationSec = durationSec
        )
        callLogDao.insertCallLog(call)
    }

    suspend fun postStatus(caption: String, bgHex: String, mediaUrl: String = "", isMine: Boolean = true) {
        val status = StatusEntity(
            contactId = 0L,
            authorName = if (isMine) "My Status" else "Contact",
            caption = caption,
            backgroundHex = bgHex,
            mediaUrl = mediaUrl,
            timestamp = System.currentTimeMillis(),
            isMine = isMine,
            viewsCount = 0
        )
        statusDao.insertStatus(status)
    }

    suspend fun viewStatus(statusId: Long) {
        statusDao.incrementViews(statusId)
    }

    suspend fun createContact(name: String, phone: String, status: String = "Available"): Long {
        val contact = ContactEntity(
            name = name,
            phone = phone,
            statusMessage = status,
            isOnline = true,
            isAiAssistant = false,
            lastSeen = "Just now"
        )
        return contactDao.insertContact(contact)
    }

    // AI Tools Functions
    suspend fun rephraseText(text: String, tone: String): String {
        val prompt = "Rephrase this message in a $tone tone for messaging:\n\"$text\""
        val system = "You are an expert messaging coach and text rephraser. Provide the rephrased message directly without introductory banter."
        return geminiService.generateText(prompt, system)
    }

    suspend fun translateText(text: String, targetLanguage: String): String {
        val prompt = "Translate the following message into $targetLanguage accurately and naturally for a chat conversation:\n\"$text\""
        val system = "You are a professional multi-language chat translator. Return the exact translation."
        return geminiService.generateText(prompt, system)
    }

    suspend fun summarizeChatText(text: String): String {
        val prompt = "Summarize this message or conversation into concise, clean bullet points:\n\"$text\""
        val system = "You are an efficient text summarizer. Provide a crisp summary with bullet points."
        return geminiService.generateText(prompt, system)
    }

    suspend fun polishGrammar(text: String): String {
        val prompt = "Fix grammar, spelling, punctuation, and wording for this chat message:\n\"$text\""
        val system = "You are a grammar and spelling polish assistant. Return the polished message directly."
        return geminiService.generateText(prompt, system)
    }

    suspend fun generateStatusIdea(mood: String): String {
        val prompt = "Generate a short, catchy, inspirational, or stylish status update (1-2 sentences with emojis) for the mood: $mood"
        val system = "You are a creative status and social media caption generator."
        return geminiService.generateText(prompt, system)
    }

    suspend fun generateSmartReplies(lastMessage: String): List<String> {
        val prompt = "Suggest 3 short, natural, appropriate quick replies (each under 6 words) to this message: \"$lastMessage\". Output only the 3 options separated by a newline."
        val response = geminiService.generateText(prompt)
        val lines = response.lines()
            .map { it.trim().removePrefix("1.").removePrefix("2.").removePrefix("3.").removePrefix("-").removePrefix("•").trim().trim('"', '\'') }
            .filter { it.isNotBlank() }
            .take(3)

        return if (lines.isNotEmpty()) lines else listOf("Sounds great! 👍", "I'll get back to you soon.", "Sure, let's do it! ✨")
    }
}
