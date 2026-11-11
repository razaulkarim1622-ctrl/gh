package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.nearby.NearbyConnectionState
import com.example.data.nearby.NearbyDevice
import com.example.ui.components.AvatarView
import com.example.ui.theme.JihadDivider
import com.example.ui.theme.JihadGreenBubble
import com.example.ui.theme.JihadGreenDark
import com.example.ui.theme.JihadGreenMint
import com.example.ui.theme.JihadGreenPrimary
import com.example.ui.theme.JihadOnlineGreen
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.AppLanguage
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyChatScreen(
    viewModel: ChatViewModel,
    lang: AppLanguage
) {
    BackHandler {
        viewModel.popBackStack()
    }

    val nearbyState by viewModel.nearbyState.collectAsState()
    val discoveredDevices by viewModel.nearbyDevices.collectAsState()
    val p2pMessagesFlow = remember { viewModel.getMessagesForConversation("nearby_p2p_session") }
    val p2pMessages by p2pMessagesFlow.collectAsState(initial = emptyList())

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isConnected = nearbyState is NearbyConnectionState.Connected
    val activeDevice = (nearbyState as? NearbyConnectionState.Connected)?.device

    LaunchedEffect(p2pMessages.size) {
        if (p2pMessages.isNotEmpty()) {
            listState.animateScrollToItem(p2pMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Strings.get("nearby_offline", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isConnected) "Connected: ${activeDevice?.name}" else "Bluetooth / Wi-Fi Direct Mesh",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (nearbyState is NearbyConnectionState.Scanning) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(22.dp)
                                .padding(end = 12.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JihadGreenPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Status and Scan Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.WifiTethering,
                                contentDescription = null,
                                tint = if (isConnected) JihadOnlineGreen else JihadGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConnected) Strings.get("p2p_active", lang)
                                else if (nearbyState is NearbyConnectionState.Scanning) Strings.get("scanning", lang)
                                else Strings.get("available_devices", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (isConnected) {
                            OutlinedButton(
                                onClick = { viewModel.disconnectNearby() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(Strings.get("disconnect", lang), fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (nearbyState is NearbyConnectionState.Scanning) {
                                        viewModel.stopNearbyDiscovery()
                                    } else {
                                        viewModel.startNearbyDiscovery()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JihadGreenPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (nearbyState is NearbyConnectionState.Scanning) Strings.get("stop_scan", lang) else Strings.get("scan_nearby", lang),
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (!isConnected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = Strings.get("nearby_desc", lang),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Discovered Peers Horizontal/Vertical Picker
            if (!isConnected) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    items(discoveredDevices) { device ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(JihadGreenMint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = JihadGreenPrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(device.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "${device.protocol} • ${device.rssi} dBm",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.connectNearbyDevice(device) },
                                colors = ButtonDefaults.buttonColors(containerColor = JihadGreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("connect_peer_btn_${device.id}")
                            ) {
                                Text(Strings.get("connect", lang), fontSize = 12.sp, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // Connected P2P Off-Grid Message Stream
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    if (p2pMessages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (lang == AppLanguage.BENGALI) "অফলাইন P2P কানেকশন সফল হয়েছে!" else "Offline P2P Session Active!",
                                        fontWeight = FontWeight.Bold,
                                        color = JihadGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == AppLanguage.BENGALI) "ইন্টারনেট ছাড়াই সরাসরি মেসেজ পাঠান।" else "Exchange messages without internet or data.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(p2pMessages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onLongClick = {},
                            isDark = viewModel.isDarkTheme.value
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // P2P Composer
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
                        placeholder = { Text(if (lang == AppLanguage.BENGALI) "অফলাইন P2P মেসেজ লিখুন..." else "Type offline P2P message...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nearby_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(
                                    conversationId = "nearby_p2p_session",
                                    text = textInput,
                                    isGroup = false,
                                    isNearbyP2P = true
                                )
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(JihadGreenPrimary)
                            .testTag("send_nearby_btn")
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
    }
}
