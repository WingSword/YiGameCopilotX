package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.AiLv
import com.yi.yigamecopilot.android.theme.Chi
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.awalong.GOOD_PERSON
import org.walks.gamecopilot.ui.components.common.IdentityCardColors
import org.walks.gamecopilot.ui.components.common.SwipeableIdentityCardShell
import org.walks.gamecopilot.ui.components.common.rememberIdentityCardColors

@Composable
fun AwalongIdentityCard(
    playerNumber: Int,
    role: AwalongRole,
    nickname: String,
    allRoles: List<AwalongRole>,
    allNicknames: List<String>,
    onClose: () -> Unit = {}
) {
    val roleColor = if (role.roleType == GOOD_PERSON) AiLv else Chi
    val colors = rememberIdentityCardColors().copy(accent = roleColor)

    SwipeableIdentityCardShell(
        resetKey = "awalong-$playerNumber-${role.title}-$nickname",
        cardWidth = 300.dp,
        cardHeight = 470.dp,
        onClose = onClose,
        colors = colors,
        hiddenContent = {
            AwalongHiddenFace(
                playerNumber = playerNumber,
                nickname = nickname,
                colors = colors
            )
        },
        visibleContent = {
            AwalongRevealedFace(
                role = role,
                roleColor = roleColor,
                allRoles = allRoles,
                allNicknames = allNicknames
            )
        }
    )
}

@Composable
private fun AwalongHiddenFace(
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
            letterSpacing = 0.sp
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
            tint = colors.accent,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "身份已隐藏",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun AwalongRevealedFace(
    role: AwalongRole,
    roleColor: androidx.compose.ui.graphics.Color,
    allRoles: List<AwalongRole>,
    allNicknames: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = role.title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontSize = 36.sp,
            color = roleColor
        )
        Surface(
            color = roleColor.copy(alpha = 0.14f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
        ) {
            Text(
                text = if (role.roleType == GOOD_PERSON) "好人阵营" else "坏人阵营",
                color = roleColor,
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
                .background(roleColor)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = role.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        val checkList = role.checkSkills(allRoles)
        if (checkList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "你能看到的其他玩家：",
                textAlign = TextAlign.Start,
                color = roleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            LazyColumn(modifier = Modifier.height(150.dp)) {
                items(checkList.keys.toList()) { playerIndex ->
                    val targetNickname = allNicknames.getOrElse(playerIndex) { "" }
                    val visibleRole = checkList[playerIndex]
                    val roleText = if (role == AwalongRole.PAIXIWEIWEIER) {
                        "可能是梅林"
                    } else {
                        visibleRole?.title.orEmpty()
                    }
                    Text(
                        text = "[${playerIndex + 1}${if (targetNickname.isNotBlank()) " ($targetNickname)" else ""} 的身份 $roleText]",
                        modifier = Modifier
                            .padding(vertical = 3.dp)
                            .fillMaxWidth(),
                        color = if (role == AwalongRole.PAIXIWEIWEIER || visibleRole?.roleType == GOOD_PERSON) AiLv else Chi,
                        textAlign = TextAlign.Start,
                        fontSize = 13.sp
                    )
                }
            }
        }

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
