package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.AiLv
import com.yi.yigamecopilot.android.theme.AnSe
import com.yi.yigamecopilot.android.theme.Chi
import com.yi.yigamecopilot.android.theme.WuJin
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.ui.widget.FlipCard

/**
 * 阿瓦隆身份卡片组件
 * 显示角色详细信息，包括技能和其他玩家信息
 */
@Composable
fun AwalongIdentityCard(
    playerNumber: Int,
    role: AwalongRole,
    nickname: String,
    allRoles: List<AwalongRole>,
    allNicknames: List<String>
) {
    val flipState = remember { androidx.compose.runtime.mutableStateOf(false) }

    FlipCard(
        modifier = Modifier.height(320.dp).width(200.dp)
            .clickable { flipState.value = !flipState.value },
        backContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$playerNumber",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.W900,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (nickname.isNotEmpty()) nickname else "玩家$playerNumber",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "点击卡片查看身份",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        frontContent = {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = WuJin)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // 角色标题
                    Text(
                        text = role.title,
                        fontWeight = FontWeight.W600,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 32.sp,
                        color = if (role.roleType == GOOD_PERSON) AiLv.copy(0.75f) else Chi.copy(
                            0.75f
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 阵营信息
                    Text(
                        "[${if (role.roleType == GOOD_PERSON) "好人" else "坏人"}阵营]",
                        color = if (role.roleType == GOOD_PERSON) AiLv else Chi,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 角色描述
                    Text(
                        role.description,
                        color = AnSe,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // 技能信息
                    val checkList = role.checkSkills(allRoles)
                    if (checkList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "\n你能看到的其他玩家：\n",
                            textAlign = TextAlign.Center,
                            color = AnSe,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LazyColumn(modifier = Modifier.height(120.dp)) {
                            items(checkList.keys.toList()) { playerIndex ->
                                val targetNickname =
                                    if (playerIndex < allNicknames.size) allNicknames[playerIndex] else ""
                                val showText =
                                    "[${playerIndex + 1}${if (targetNickname.isNotEmpty()) " ($targetNickname)" else ""} 的身份 ${if (role != AwalongRole.PAIXIWEIWEIER) checkList[playerIndex]?.title else "可能是梅林"}]"
                                Text(
                                    text = showText,
                                    modifier = Modifier.padding(vertical = 2.dp).fillMaxWidth(),
                                    color = if (role == AwalongRole.PAIXIWEIWEIER || checkList[playerIndex]?.roleType == GOOD_PERSON) AiLv else Chi,
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        isFlipped = !flipState.value
    )
}

// 阵营常量 - 与AwalongConfig.kt保持一致
private const val GOOD_PERSON = 1
private const val BAD_PERSON = -1
private const val NEUTRAL_PERSON = 0