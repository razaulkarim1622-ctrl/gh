package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.DeliveryStatusIcon
import com.example.ui.theme.JihadGreenBubble
import com.example.ui.theme.JihadGreenBubbleDark
import com.example.ui.theme.JihadGreenDark
import com.example.ui.theme.JihadGreenPrimary
import com.example.ui.theme.JihadOnlineGreen
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.AppLanguage
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    title: String,
    isGroup: Boolean = false,
    isNearby: Boolean = false,
    viewModel: ChatViewModel,
    lang: AppLanguage
) {
    BackHandler {
        viewModel.popBackStack()
    }

    val context = LocalContext.current
    val messagesFlow = remember(conversationId) { viewModel.getMessagesForConversation(conversationId) }
    val messages by messagesFlow.collectAsState(initial = emptyList())
    val isOnline by viewModel.isOnline.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedMessageForAction by remember { mutableStateOf<ChatMessageEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(
                            initials = title.take(2),
                            size = 38.dp,
                            backgroundColor = JihadGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isNearby) Strings.get("nearby_connected", lang)
                                else if (isGroup) "Group • ${messages.distinctBy { it.senderName }.size + 1} members"
                                else if (isOnline) Strings.get("online", lang)
                                else Strings.get("offline", lang),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(Strings.get("block", lang)) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.blockUser(conversationId)
                                },
                                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.get("report", lang)) },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.showFeedback(
                                        if (lang == AppLanguage.BENGALI) "রিপোর্ট গ্রহণ করা হয়েছে" else "Report submitted"
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.get("delete_chat", lang), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirmDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JihadGreenPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onLongClick = { selectedMessageForAction = message },
                        isDark = viewModel.isDarkTheme.value
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Message Composer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            Strings.get("type_message", lang),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = false,
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendMessage(
                                conversationId = conversationId,
                                text = textInput,
                                isGroup = isGroup,
                                isNearbyP2P = isNearby
                            )
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(JihadGreenPrimary)
                        .testTag("send_msg_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Message Options Dialog (Copy / Delete)
    selectedMessageForAction?.let { msg ->
        AlertDialog(
            onDismissRequest = { selectedMessageForAction = null },
            title = { Text(if (lang == AppLanguage.BENGALI) "মেসেজ অপশন" else "Message Options") },
            text = { Text(msg.text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Jihad Chat", msg.text)
                        clipboard.setPrimaryClip(clip)
                        viewModel.showFeedback(Strings.get("copy_message", lang))
                        selectedMessageForAction = null
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("copy_message", lang))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMessage(msg.id)
                        selectedMessageForAction = null
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("delete", lang), color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // Confirm Delete Entire Conversation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(Strings.get("delete_chat", lang)) },
            text = { Text(Strings.get("confirm_delete_data", lang)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteConversation(conversationId)
                    }
                ) {
                    Text(Strings.get("delete", lang), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(Strings.get("cancel", lang))
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessageEntity,
    onLongClick: () -> Unit,
    isDark: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = timeFormat.format(Date(message.timestamp))

    val bubbleColor = if (message.isMine) {
        if (isDark) JihadGreenBubbleDark else JihadGreenBubble
    } else {
        MaterialTheme.colorScheme.surface
    }

    val alignment = if (message.isMine) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isMine) 16.dp else 4.dp,
                bottomEnd = if (message.isMine) 4.dp else 16.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isMine) 16.dp else 4.dp,
                        bottomEnd = if (message.isMine) 4.dp else 16.dp
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // If group message and incoming, show sender name
                if (message.isGroup && !message.isMine) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        color = JihadGreenDark,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (message.isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        DeliveryStatusIcon(
                            status = message.deliveryStatus,
                            isMine = true
                        )
                    }
                }
            }
        }
    }
}
