package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarView
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.JihadDivider
import com.example.ui.theme.JihadGreenDark
import com.example.ui.theme.JihadGreenMint
import com.example.ui.theme.JihadGreenPrimary
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.AppLanguage
import com.example.util.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChatViewModel,
    lang: AppLanguage
) {
    BackHandler {
        viewModel.popBackStack()
    }

    val name by viewModel.userDisplayName.collectAsState()
    val phone by viewModel.userPhone.collectAsState()
    val bio by viewModel.userBio.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(name) }
    var editBio by remember { mutableStateOf(bio) }
    var showQrModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get("profile", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showQrModal = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color.White)
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                AvatarView(
                    initials = name.take(2),
                    size = 96.dp,
                    backgroundColor = JihadGreenPrimary
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(JihadGreenPrimary)
                        .clickable { isEditing = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ProfileInfoRow(
                            label = Strings.get("full_name", lang),
                            value = name,
                            icon = Icons.Default.Person
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = JihadDivider)
                        ProfileInfoRow(
                            label = Strings.get("phone_number", lang),
                            value = phone,
                            icon = Icons.Default.Security
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = JihadDivider)
                        ProfileInfoRow(
                            label = Strings.get("bio", lang),
                            value = bio,
                            icon = Icons.Default.Edit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        editName = name
                        editBio = bio
                        isEditing = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JihadGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (lang == AppLanguage.BENGALI) "প্রোফাইল সম্পাদনা করুন" else "Edit Profile", color = Color.White)
                }
            } else {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(Strings.get("full_name", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = editBio,
                    onValueChange = { editBio = it },
                    label = { Text(Strings.get("bio", lang)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, editBio)
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JihadGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Strings.get("save", lang), color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    TextButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Strings.get("cancel", lang))
                    }
                }
            }
        }
    }

    if (showQrModal) {
        AlertDialog(
            onDismissRequest = { showQrModal = false },
            title = { Text(if (lang == AppLanguage.BENGALI) "আপনার চ্যাট QR কোড" else "Your Chat QR Code") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(JihadGreenMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = JihadGreenPrimary,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrModal = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = JihadGreenPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    lang: AppLanguage
) {
    BackHandler {
        viewModel.popBackStack()
    }

    val isDark by viewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get("settings", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(16.dp)
        ) {
            // Language Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = JihadGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(Strings.get("language", lang), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Row {
                            Text(
                                text = "বাংলা",
                                color = if (lang == AppLanguage.BENGALI) Color.White else JihadGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (lang == AppLanguage.BENGALI) JihadGreenPrimary else JihadGreenMint)
                                    .clickable { viewModel.setLanguage(AppLanguage.BENGALI) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "English",
                                color = if (lang == AppLanguage.ENGLISH) Color.White else JihadGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (lang == AppLanguage.ENGLISH) JihadGreenPrimary else JihadGreenMint)
                                    .clickable { viewModel.setLanguage(AppLanguage.ENGLISH) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = JihadDivider)

                    // Dark Theme Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = JihadGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(Strings.get("theme", lang), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = JihadGreenPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Security link
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.Privacy) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = JihadGreenPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(Strings.get("privacy_security", lang), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Text("›", fontSize = 20.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Download APK for Mobile Phone
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDownloadDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JihadGreenMint)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(JihadGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (lang == AppLanguage.BENGALI) "ফোনে APK ডাউনলোড করুন" else "Download APK for Mobile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = JihadGreenDark
                            )
                            Text(
                                text = if (lang == AppLanguage.BENGALI) "সরাসরি মোবাইলে ইনস্টল করার ফাইল ও লিংক" else "Direct file & link to install on mobile",
                                fontSize = 12.sp,
                                color = JihadGreenDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = JihadGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Clear All Local Data
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearDataConfirm = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        Strings.get("delete_all_data", lang),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showDownloadDialog) {
        val apkUrl = "https://ais-pre-bu5ur5ucnniarqwywa5gdg-569567363979.asia-east1.run.app/Jihad_Chat.apk"
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = JihadGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (lang == AppLanguage.BENGALI) "ফোনে অ্যাপ ডাউনলোড করুন" else "Download App to Phone",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (lang == AppLanguage.BENGALI)
                            "আপনার Realme বা অ্যান্ড্রয়েড ফোনে 'Jihad Chat' ইনস্টল করার জন্য নিচের অপশনগুলো ব্যবহার করুন:"
                        else
                            "Use the options below to download and install 'Jihad Chat' on your Android device:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = JihadGreenMint),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (lang == AppLanguage.BENGALI) "ইনস্টল করার সহজ নিয়ম:" else "Installation Steps:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = JihadGreenDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lang == AppLanguage.BENGALI)
                                    "১. 'ব্রাউজারে ডাউনলোড' চাপলে APK ডাউনলোড শুরু হবে।\n২. ডাউনলোড শেষে নোটিফিকেশন থেকে ফাইলে ট্যাপ করুন।\n৩. Settings থেকে 'Install unknown apps' অনুমতি দিন।\n৪. 'Install' চাপুন এবং সরাসরি ব্যবহার করুন।"
                                else
                                    "1. Tap 'Download in Browser' to get the APK.\n2. Tap the downloaded file in notification/files.\n3. Allow 'Install unknown apps' in Android settings.\n4. Tap 'Install' to start chatting!",
                                fontSize = 12.sp,
                                color = JihadGreenDark,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    viewModel.showFeedback("ব্রাউজার খুলতে সমস্যা হয়েছে")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JihadGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (lang == AppLanguage.BENGALI) "ব্রাউজারে ডাউনলোড" else "Browser Download",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Jihad Chat APK", apkUrl)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showFeedback(
                                    if (lang == AppLanguage.BENGALI) "ডাউনলোড লিংক কপি করা হয়েছে!" else "Download link copied!"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (lang == AppLanguage.BENGALI) "লিংক কপি" else "Copy Link",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text(if (lang == AppLanguage.BENGALI) "ঠিক আছে" else "Close")
                }
            }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text(Strings.get("delete_all_data", lang)) },
            text = { Text(Strings.get("confirm_delete_data", lang)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataConfirm = false
                        viewModel.clearAllData()
                    }
                ) {
                    Text(Strings.get("delete", lang), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) {
                    Text(Strings.get("cancel", lang))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    viewModel: ChatViewModel,
    lang: AppLanguage
) {
    BackHandler {
        viewModel.popBackStack()
    }

    val readReceipts by viewModel.preferences.readReceipts.collectAsState()
    val lastSeen by viewModel.preferences.lastSeen.collectAsState()
    val securityLock by viewModel.preferences.securityLock.collectAsState()
    val blockedContacts by viewModel.blockedContacts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get("privacy_security", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = JihadGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(Strings.get("read_receipts", lang), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Switch(
                            checked = readReceipts,
                            onCheckedChange = { viewModel.setReadReceipts(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = JihadGreenPrimary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = JihadDivider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = JihadGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(Strings.get("last_seen", lang), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Switch(
                            checked = lastSeen,
                            onCheckedChange = { viewModel.setLastSeen(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = JihadGreenPrimary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = JihadDivider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = JihadGreenPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(Strings.get("security_lock", lang), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Switch(
                            checked = securityLock,
                            onCheckedChange = { viewModel.setSecurityLock(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = JihadGreenPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = Strings.get("blocked_users", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (blockedContacts.isEmpty()) {
                Text(
                    text = if (lang == AppLanguage.BENGALI) "কোনো ব্যবহারকারীকে ব্লক করা হয়নি।" else "No blocked users.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                LazyColumn {
                    items(blockedContacts) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(contact.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            TextButton(onClick = { viewModel.unblockUser(contact.id) }) {
                                Text(Strings.get("unblock", lang), color = JihadGreenPrimary)
                            }
                        }
                        HorizontalDivider(color = JihadDivider.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
