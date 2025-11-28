package org.walks.gamecopilot.ui.page.game.localspy.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.MorandiColorList
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.ui.widget.FlipCard
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_asterisk
import yigamecopilotx.composeapp.generated.resources.icon_star

/**
 * 身份卡片组件，用于显示玩家身份信息及操作提示
 * 
 * 功能说明：
 * - 支持点击翻转卡片查看身份
 * - 卡片背面显示玩家编号
 * - 卡片正面显示具体身份词汇
 * - 随机边框颜色增加视觉效果
 *
 * @param gameState 当前游戏状态实体，包含玩家身份数据及操作逻辑
 * @param currentSelectPlayer 当前选择的玩家编号（默认1号玩家）
 */
@Composable
fun LocalSpyIdentityCard(
    gameState: LocalSpyEntity,
    currentSelectPlayer: Int = 1,
) {
    val flipState = remember { mutableStateOf(false) }
    
    // 主容器：包含卡片布局和交互效果
    FlipCard(
        modifier = Modifier.height(200.dp).width(140.dp).clip(RoundedCornerShape(12.dp))
            .clickable { flipState.value = !flipState.value },
        backContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {

                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(Res.drawable.icon_asterisk),
                        alpha = 0.33f,
                        modifier = Modifier.size(100.dp),
                        contentDescription = ""
                    )
                    Text(
                        text = currentSelectPlayer.toString(),
                        fontSize = 90.sp,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                        textAlign = TextAlign.Right
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (gameState.isSpy(currentSelectPlayer)) "" else "",
                    textAlign = TextAlign.Center,
                    color = if (gameState.isSpy(currentSelectPlayer)) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraLight,
                    modifier = Modifier.fillMaxWidth()
                )
                // 操作提示文本：根据显示状态切换提示语
                Text(
                    "点击卡片查看身份词",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        frontContent = {
            Box(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface
                ).border(
                    BorderStroke(
                        width = 4.dp,
                        color = MorandiColorList[(0..7).random()] // 随机生成边框颜色
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(Res.drawable.icon_star),
                        alpha = 0.33f,
                        modifier = Modifier.size(120.dp).rotate(-30f),
                        contentDescription = ""
                    )

                    Text(
                        text = currentSelectPlayer.toString(),
                        modifier = Modifier.rotate(-30f),
                        fontSize = 90.sp,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                        textAlign = TextAlign.Right
                    )
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        gameState.optIdentity(currentSelectPlayer),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.weight(1f))

                }
            }

        },
        isFlipped = !flipState.value,
        onFlipComplete = {

        }
    )
}