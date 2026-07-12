package org.walks.gamecopilot.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocalSpySwipeableIdentityCard(
    resetKey: Any,
    playerNumber: Int,
    nickname: String,
    identity: String,
    isSpy: Boolean,
    onClose: () -> Unit = {}
) {
    val accentColor = if (isSpy) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val colors = rememberIdentityCardColors().copy(accent = accentColor)

    SwipeableIdentityCardShell(
        resetKey = resetKey,
        cardWidth = 300.dp,
        cardHeight = 470.dp,
        onClose = onClose,
        colors = colors,
        hiddenContent = {
            LocalSpyHiddenFace(
                playerNumber = playerNumber,
                nickname = nickname,
                colors = colors
            )
        },
        visibleContent = {
            LocalSpyRevealedFace(
                identity = identity,
                roleLabel = if (isSpy) "卧底" else "平民",
                accentColor = accentColor
            )
        }
    )
}

@Composable
private fun LocalSpyHiddenFace(
    playerNumber: Int,
    nickname: String,
    colors: IdentityCardColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PLAYER $playerNumber",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = nickname.takeIf { it.isNotBlank() } ?: "玩家$playerNumber",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector = Icons.Rounded.Visibility,
            contentDescription = null,
            tint = colors.accent
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "身份词已隐藏",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun LocalSpyRevealedFace(
    identity: String,
    roleLabel: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = roleLabel,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontSize = 36.sp,
            color = accentColor
        )
        Surface(
            color = accentColor.copy(alpha = 0.14f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "谁是卧底",
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = identity,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 34.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "记住身份词，交还设备前滑回隐藏",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "左右滑动可隐藏身份",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
