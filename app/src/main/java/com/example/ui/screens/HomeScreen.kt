package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.OfflineQueueBanner
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.JihadDivider
import com.example.ui.theme.JihadGreenDark
import com.example.ui.theme.JihadGreenMint
import com.example.ui.theme.JihadGreenPrimary
import com.example.ui.theme.JihadOfflineAmber
import com.example.ui.theme.JihadOnlineGreen
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.AppLanguage
import com.example.util.Strings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    lang: AppLanguage
) {
    val conversations by viewModel.conversations.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val queuedCount by viewModel.queuedCount.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = conversations.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.lastMessageText.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JihadGreenPrimary)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = Strings.get("app_name", lang),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) JihadOnlineGreen else JihadOfflineAmber)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) Strings.get("online", lang) else Strings.get("offline", lang),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Network Simulation Toggle
                        IconButton(
                            onClick = { viewModel.toggleSimulateOffline() },
                            modifier = Modifier.testTag("toggle_offline_btn")
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "Toggle Network Status",
                                tint = if (isOnline) Color.White else JihadOfflineAmber
                            )
                        }

                        // Search Toggle
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }

                        // Profile / Settings
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.Settings) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = JihadGreenPrimary
                    )
                )

                // Search Bar Expandable
                AnimatedVisibility(visible = isSearchActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(Strings.get("search_chats", lang), color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                // Tab Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = JihadGreenPrimary,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.White,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Strings.get("chats", lang),
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                                val unreadTotal = conversations.filter { !it.isGroup }.sumOf { it.unreadCount }
                                if (unreadTotal > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = Color.White, contentColor = JihadGreenPrimary) {
                                        Text("$unreadTotal", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = Strings.get("groups", lang),
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Strings.get("nearby_offline", lang),
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 1) {
                        viewModel.navigateTo(AppScreen.NewChat)
                    } else if (selectedTab == 2) {
                        viewModel.navigateTo(AppScreen.NearbyChat)
                    } else {
                        viewModel.navigateTo(AppScreen.NewChat)
                    }
                },
                containerColor = JihadGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("home_fab")
            ) {
                Icon(
                    imageVector = if (selectedTab == 1) Icons.Default.GroupAdd else if (selectedTab == 2) Icons.Default.NearMe else Icons.Default.Chat,
                    contentDescription = "Action"
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Chat, contentDescription = Strings.get("chats", lang)) },
                    label = { Text(Strings.get("chats", lang)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JihadGreenPrimary,
                        selectedTextColor = JihadGreenPrimary,
                        indicatorColor = JihadGreenMint
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Group, contentDescription = Strings.get("groups", lang)) },
                    label = { Text(Strings.get("groups", lang)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JihadGreenPrimary,
                        selectedTextColor = JihadGreenPrimary,
                        indicatorColor = JihadGreenMint
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.NearMe, contentDescription = Strings.get("nearby_offline", lang)) },
                    label = { Text(Strings.get("nearby_offline", lang)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JihadGreenPrimary,
                        selectedTextColor = JihadGreenPrimary,
                        indicatorColor = JihadGreenMint
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(AppScreen.Contacts) },
                    icon = { Icon(Icons.Default.People, contentDescription = Strings.get("contacts", lang)) },
                    label = { Text(Strings.get("contacts", lang)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JihadGreenPrimary,
                        selectedTextColor = JihadGreenPrimary,
                        indicatorColor = JihadGreenMint
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(AppScreen.Profile) },
                    icon = { Icon(Icons.Default.Person, contentDescription = Strings.get("profile", lang)) },
                    label = { Text(Strings.get("profile", lang)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JihadGreenPrimary,
                        selectedTextColor = JihadGreenPrimary,
                        indicatorColor = JihadGreenMint
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Offline Message Queue Banner
            OfflineQueueBanner(
                isOnline = isOnline,
                queuedCount = queuedCount,
                lang = lang,
                onSyncClick = { viewModel.flushOfflineQueueNow() }
            )

            when (selectedTab) {
                0 -> {
                    val directChats = filteredChats.filter { !it.isGroup }
                    if (directChats.isEmpty()) {
                        EmptyChatState(lang) { viewModel.navigateTo(AppScreen.NewChat) }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(directChats, key = { it.id }) { chat ->
                                ChatListItem(
                                    chat = chat,
                                    onClick = {
                                        viewModel.navigateTo(
                                            AppScreen.ChatDetail(chat.id, chat.title, isGroup = false)
                                        )
                                    }
                                )
                                HorizontalDivider(color = JihadDivider.copy(alpha = 0.5f), thickness = 0.8.dp)
                            }
                        }
                    }
                }
                1 -> {
                    val groupChats = filteredChats.filter { it.isGroup }
                    if (groupChats.isEmpty()) {
                        EmptyGroupState(lang) { viewModel.navigateTo(AppScreen.NewChat) }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(groupChats, key = { it.id }) { group ->
                                ChatListItem(
                                    chat = group,
                                    onClick = {
                                        viewModel.navigateTo(
                                            AppScreen.GroupChat(group.id, group.title)
                                        )
                                    }
                                )
                                HorizontalDivider(color = JihadDivider.copy(alpha = 0.5f), thickness = 0.8.dp)
                            }
                        }
                    }
                }
                2 -> {
                    // Nearby Offline P2P Tab Overview
                    NearbyTabOverview(viewModel, lang)
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: ConversationEntity,
    onClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = timeFormat.format(Date(chat.lastMessageTimestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            initials = chat.avatarInitials.ifBlank { chat.title.take(1) },
            backgroundColor = Color(chat.avatarColor),
            isOnline = chat.isOnline,
            showOnlineBadge = !chat.isGroup
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = if (chat.unreadCount > 0) JihadGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessageText.ifBlank { "চ্যাট শুরু করুন" },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(JihadGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chat.unreadCount}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(lang: AppLanguage, onNewChatClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(JihadGreenMint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = JihadGreenPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Strings.get("no_chats", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = Strings.get("no_chats_desc", lang),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun EmptyGroupState(lang: AppLanguage, onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(JihadGreenMint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = JihadGreenPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (lang == AppLanguage.BENGALI) "কোনো গ্রুপ চ্যাট নেই" else "No Groups Yet",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (lang == AppLanguage.BENGALI) "বন্ধুবান্ধব বা পরিবারের সাথে চ্যাট করতে গ্রুপ তৈরি করুন।" else "Create a group to chat with friends, colleagues, or family.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NearbyTabOverview(viewModel: ChatViewModel, lang: AppLanguage) {
    val nearbyState by viewModel.nearbyState.collectAsState()
    val nearbyDevices by viewModel.nearbyDevices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        androidx.compose.material3.Card(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = JihadGreenMint
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = JihadGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.get("nearby_offline", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = JihadGreenDark
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Strings.get("nearby_desc", lang),
                    fontSize = 12.sp,
                    color = JihadGreenDark.copy(alpha = 0.85f),
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.Button(
                    onClick = { viewModel.navigateTo(AppScreen.NearbyChat) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = JihadGreenPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Strings.get("scan_nearby", lang),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = Strings.get("available_devices", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(nearbyDevices) { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(AppScreen.NearbyChat) }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(
                            initials = device.name.take(1),
                            size = 40.dp,
                            backgroundColor = JihadGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = device.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${device.protocol} • ${device.rssi} dBm",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = Strings.get("connect", lang),
                        color = JihadGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                HorizontalDivider(color = JihadDivider.copy(alpha = 0.5f))
            }
        }
    }
}
