package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageStatus
import com.example.ui.theme.JihadGreenBubble
import com.example.ui.theme.JihadGreenPrimary
import com.example.ui.theme.JihadOfflineAmber
import com.example.ui.theme.JihadOnlineGreen
import com.example.util.AppLanguage
import com.example.util.Strings

@Composable
fun AvatarView(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = JihadGreenPrimary,
    isOnline: Boolean = false,
    showOnlineBadge: Boolean = false
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }

        if (showOnlineBadge) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(if (isOnline) JihadOnlineGreen else Color.LightGray)
                )
            }
        }
    }
}

@Composable
fun DeliveryStatusIcon(
    status: MessageStatus,
    modifier: Modifier = Modifier,
    isMine: Boolean = true
) {
    if (!isMine) return

    when (status) {
        MessageStatus.QUEUED_OFFLINE -> {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = "Queued Offline",
                tint = JihadOfflineAmber,
                modifier = modifier.size(13.dp)
            )
        }
        MessageStatus.SENDING -> {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sending",
                tint = Color.Gray,
                modifier = modifier.size(13.dp)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = Color.Gray,
                modifier = modifier.size(14.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = Color.Gray,
                modifier = modifier.size(15.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = JihadGreenPrimary,
                modifier = modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun OfflineQueueBanner(
    isOnline: Boolean,
    queuedCount: Int,
    lang: AppLanguage,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = !isOnline || queuedCount > 0) {
        Surface(
            color = if (!isOnline) Color(0xFFFFF3CD) else JihadGreenBubble,
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (!isOnline) Icons.Default.WifiOff else Icons.Default.Sync,
                    contentDescription = null,
                    tint = if (!isOnline) JihadOfflineAmber else JihadGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (!isOnline) {
                        "${Strings.get("offline_banner_title", lang)} • ${queuedCount} ${Strings.get("queued", lang)}"
                    } else {
                        "${queuedCount} ${Strings.get("queued", lang)} • ${Strings.get("sending", lang)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (!isOnline) Color(0xFF856404) else JihadGreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                if (isOnline && queuedCount > 0) {
                    Text(
                        text = "Sync Now",
                        color = JihadGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSyncClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("sync_queue_button")
                    )
                }
            }
        }
    }
}
