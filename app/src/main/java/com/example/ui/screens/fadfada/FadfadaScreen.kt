package com.example.ui.screens.fadfada

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioFeedbackManager
import com.example.data.model.ChatConversationEntity
import com.example.data.model.Mascot
import com.example.data.model.MascotId
import com.example.data.model.Mascots
import com.example.ui.components.MascotView
import kotlinx.coroutines.launch

@Composable
fun FadfadaScreen(
    userMascotId: String,
    chatMessages: List<ChatConversationEntity>,
    isMascotTyping: Boolean = false,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchOpen by remember { mutableStateOf(false) }
    var pinnedMessageIds by remember { mutableStateOf(setOf<Long>()) }

    val mascot = Mascots.getById(MascotId.valueOf(userMascotId))
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Auto scroll when new message or typing starts
    LaunchedEffect(chatMessages.size, isMascotTyping) {
        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }

    // Quick Prompt Shortcuts
    val quickPrompts = listOf(
        "تعبان من المذاكرة 😔",
        "عندي امتحان قريب 🎯",
        "مش عارف أبدأ منين 🤔",
        "زهقان ومشتت ⚡",
        "عاوز نصيحة سريعة 💡",
        "مستوايا اتغير إزاي؟ 📈"
    )

    val filteredMessages = if (searchQuery.isBlank()) {
        chatMessages
    } else {
        chatMessages.filter { it.messageText.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- 1. Mascot Banner Header ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(68.dp)) {
                        Image(
                            painter = painterResource(id = mascot.imageResId),
                            contentDescription = mascot.nameAr,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "فضفضلي مع ${mascot.nameAr}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isMascotTyping) Color(0xFFF59E0B) else Color(0xFF10B981))
                            )
                        }

                        Text(
                            text = if (isMascotTyping) "... يكتب الآن 💭" else mascot.introQuoteEg,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    // Search Button Toggle
                    IconButton(
                        onClick = { isSearchOpen = !isSearchOpen },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search Chat",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Expandable Search Bar
                AnimatedVisibility(visible = isSearchOpen) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث في الرسائل...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.9f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. Quick Emotion Prompt Chips ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.clickable {
                        AudioFeedbackManager.performLightTap(context)
                        onSendMessage(prompt)
                    }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 3. Chat Messages History ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated Typing Bubble
            if (isMascotTyping) {
                item {
                    MascotTypingIndicator(mascot = mascot)
                }
            }

            items(filteredMessages.reversed(), key = { it.id }) { chat ->
                val isUser = chat.sender == "USER"
                val isPinned = pinnedMessageIds.contains(chat.id)

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isUser) {
                            // Avatar Icon
                            Image(
                                painter = painterResource(id = mascot.imageResId),
                                contentDescription = mascot.nameAr,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (isUser) 20.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 20.dp
                            ),
                            color = if (isUser) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shadowElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (isPinned) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Pinned",
                                            tint = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "رسالة مثبّتة",
                                            fontSize = 10.sp,
                                            color = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = chat.messageText,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Copy Action
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = if (isUser) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(chat.messageText))
                                                AudioFeedbackManager.performLightTap(context)
                                            }
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Pin Action
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pin",
                                        tint = if (isPinned) MaterialTheme.colorScheme.primary else (if (isUser) Color.White.copy(alpha = 0.6f) else Color.Gray),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                pinnedMessageIds = if (isPinned) {
                                                    pinnedMessageIds - chat.id
                                                } else {
                                                    pinnedMessageIds + chat.id
                                                }
                                                AudioFeedbackManager.performLightTap(context)
                                            }
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Read Status Double Checkmark for User Messages
                                    if (isUser) {
                                        Text(
                                            text = "✓✓",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF60A5FA)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. Input Text Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("فضفضلي يا بطل... أنا اسمعك 💭", fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        AudioFeedbackManager.playMascotPop()
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MascotsTypingDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "DotAnim")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OffsetY"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun MascotTypingIndicator(mascot: Mascot) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = mascot.imageResId),
            contentDescription = mascot.nameAr,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(28.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "${mascot.nameAr} بيكتب ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MascotsTypingDot(0)
                MascotsTypingDot(150)
                MascotsTypingDot(300)
            }
        }
    }
}
