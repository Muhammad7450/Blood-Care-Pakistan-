package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.GeminiService
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.SoftRedContainer
import com.example.ui.util.LanguageState
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun AiChatbotScreen() {
    val coroutineScope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                text = LanguageState.getText(
                    "Hello! I am your AI Blood Care Assistant. Ask me anything about blood donor eligibility, blood group compatibility (e.g. who can O- donate to?), or emergency sourcing in Pakistan.",
                    "السلام علیکم! میں آپ کا اے آئی بلڈ کیئر اسسٹنٹ ہوں۔ خون کے عطیہ، بلڈ گروپس کی مطابقت اور اہلیت کے بارے میں سوال پوچھیں۔"
                ),
                isUser = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SoftRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = CrimsonRedPrimary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = LanguageState.getText("AI Donor Recommendation & Advisor", "اے آئی طبی معاون"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = LanguageState.getText("Powered by Gemini 3.5 Flash", "Gemini 3.5 Flash سے لیس"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat messages log
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { msg ->
                val bgClr = if (msg.isUser) CrimsonRedPrimary else MaterialTheme.colorScheme.surface
                val txtClr = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                val align = if (msg.isUser) Alignment.End else Alignment.Start

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgClr),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(
                            text = msg.text,
                            color = txtClr,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrimsonRedPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LanguageState.getText("Thinking...", "سوچ رہا ہے..."), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text(LanguageState.getText("Ask AI about blood compatibility...", "بلڈ کے متعلق سوال پوچھیں...")) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank() && !isLoading) {
                        val query = inputQuery
                        inputQuery = ""
                        chatMessages.add(ChatMessage(query, isUser = true))
                        isLoading = true

                        coroutineScope.launch {
                            val reply = GeminiService.generateBloodGuidance(query, LanguageState.currentLanguage)
                            chatMessages.add(ChatMessage(reply, isUser = false))
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CrimsonRedPrimary)
                    .testTag("send_ai_query_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Query", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
