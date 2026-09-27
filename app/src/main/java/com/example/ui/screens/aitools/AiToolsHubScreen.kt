package com.example.ui.screens.aitools

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AiToolResult
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiToolsHubScreen(
    aiToolState: AiToolResult,
    onRephrase: (String, String) -> Unit,
    onTranslate: (String, String) -> Unit,
    onSummarize: (String) -> Unit,
    onPolishGrammar: (String) -> Unit,
    onGenerateStatus: (String) -> Unit,
    onClearResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTool by remember { mutableStateOf("Rephrase") }
    var inputText by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("Professional") }
    var selectedLanguage by remember { mutableStateOf("Hindi") }
    var selectedMood by remember { mutableStateOf("Inspiring") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_tools_hub_screen")
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ai_tools_hero_1790503269769),
                    contentDescription = "AI Suite Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF0F172A).copy(alpha = 0.85f),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Smart AI Tools Suite",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Powered by Gemini 3.5 • Write, rephrase, translate & create",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Tool Selector Tabs
        item {
            val tools = listOf("Rephrase", "Translate", "Summarize", "Grammar", "Status Idea")
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tools.forEach { tool ->
                    val isSelected = selectedTool == tool
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTool = tool
                            onClearResult()
                        },
                        label = { Text(tool, fontSize = 13.sp) },
                        leadingIcon = {
                            val icon = when (tool) {
                                "Rephrase" -> Icons.Default.Tune
                                "Translate" -> Icons.Default.Language
                                "Summarize" -> Icons.Default.Summarize
                                "Grammar" -> Icons.Default.Spellcheck
                                else -> Icons.Default.Psychology
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }

        // Active Tool Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (selectedTool) {
                            "Rephrase" -> "Message Rephraser & Tone Transformer"
                            "Translate" -> "Smart Multi-Language Chat Translator"
                            "Summarize" -> "Chat & Paragraph Summarizer"
                            "Grammar" -> "Grammar, Punctuation & Style Polish"
                            else -> "Smart Status & Caption Generator"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Text Field
                    if (selectedTool != "Status Idea") {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    when (selectedTool) {
                                        "Rephrase" -> "Enter message you want to rewrite..."
                                        "Translate" -> "Type message to translate..."
                                        "Summarize" -> "Paste a long message or discussion..."
                                        else -> "Type message to fix grammar..."
                                    },
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("ai_tool_input_text"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Tool specific parameters
                    when (selectedTool) {
                        "Rephrase" -> {
                            Text("Select Tone:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val tones = listOf("Professional", "Friendly", "Casual", "Funny", "Concise", "Romantic")
                                tones.forEach { tone ->
                                    val sel = selectedTone == tone
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (sel) PrimaryBlue else MaterialTheme.colorScheme.surface)
                                            .clickable { selectedTone = tone }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = tone,
                                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        "Translate" -> {
                            Text("Translate to:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val langs = listOf("Hindi", "Spanish", "French", "German", "Arabic", "Japanese", "Urdu", "English")
                                langs.forEach { lang ->
                                    val sel = selectedLanguage == lang
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (sel) AccentPurple else MaterialTheme.colorScheme.surface)
                                            .clickable { selectedLanguage = lang }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = lang,
                                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        "Status Idea" -> {
                            Text("Choose Mood / Vibe:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val moods = listOf("Inspiring", "Coding / Tech", "Chill / Relax", "Funny", "Romantic", "Deep Thoughts")
                                moods.forEach { mood ->
                                    val sel = selectedMood == mood
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (sel) AccentCyan else MaterialTheme.colorScheme.surface)
                                            .clickable { selectedMood = mood }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = mood,
                                            color = if (sel) Color.Black else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Execute Button
                    Button(
                        onClick = {
                            when (selectedTool) {
                                "Rephrase" -> onRephrase(inputText, selectedTone)
                                "Translate" -> onTranslate(inputText, selectedLanguage)
                                "Summarize" -> onSummarize(inputText)
                                "Grammar" -> onPolishGrammar(inputText)
                                "Status Idea" -> onGenerateStatus(selectedMood)
                            }
                        },
                        enabled = !aiToolState.isLoading && (inputText.isNotBlank() || selectedTool == "Status Idea"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_tool_generate_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (aiToolState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating with AI...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedTool) {
                                    "Rephrase" -> "Rephrase Now"
                                    "Translate" -> "Translate"
                                    "Summarize" -> "Summarize"
                                    "Grammar" -> "Fix & Polish"
                                    else -> "Generate Status Idea"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Result Display Card
        item {
            AnimatedVisibility(visible = aiToolState.outputText.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("ai_tool_result_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(PrimaryBlue, AccentPurple))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = aiToolState.toolType,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryBlue
                                )
                            }

                            // Copy Button
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(aiToolState.outputText))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy result",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = aiToolState.outputText,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
