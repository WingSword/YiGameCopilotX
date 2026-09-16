package org.walks.gamecopilot.ui.page.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.intent.AiIntent
import org.walks.gamecopilot.service.ai.AiProvider
import org.walks.gamecopilot.service.ai.AiStyle
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.theme.ThemeMode
import org.walks.gamecopilot.ui.components.AppSegmentedControl


/**
 * 设置页面
 * 包含APP版本信息和APP介绍
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(viewmodel: MainViewmodel, onOpenMonopolyLedger: () -> Unit = {}, onOpenStats: () -> Unit = {}) {
    val design = LocalAppDesign.current
    val records by org.walks.gamecopilot.data.GameStatsManager.recordsFlow.collectAsState()
    val currentTheme by viewmodel.themeMode.collectAsState()
    val aiConfig by viewmodel.aiConfig.collectAsState()
    var aiExpanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
    Column(Modifier.widthIn(max = 680.dp).fillMaxSize()
        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("我的", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        androidx.compose.material3.Surface(shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("我的桌游", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("✦", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    Column { Text("${records.size}", fontSize = 32.sp, fontWeight = FontWeight.Bold); Text("累计对局", fontSize = 12.sp) }
                    Column { Text("${records.sumOf { it.playerCount }}", fontSize = 32.sp, fontWeight = FontWeight.Bold); Text("参与人次", fontSize = 12.sp) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PersonalToolTile("桌游记账", Icons.Rounded.AccountBalanceWallet, Modifier.weight(1f), onOpenMonopolyLedger)
            PersonalToolTile("对局统计", Icons.Rounded.BarChart, Modifier.weight(1f), onOpenStats)
        }
        SettingCard("偏好设置", design) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("外观", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AppSegmentedControl(options = listOf("跟随系统", "浅色", "深色"),
                    selectedIndex = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).indexOf(currentTheme),
                    onSelected = { viewmodel.setThemeMode(listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)[it]) })
                Row(Modifier.fillMaxWidth().clickable { aiExpanded = !aiExpanded }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                    Text("对局提示", Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.Medium)
                    Text(if (aiConfig.isEnabled) "已开启" else "已关闭", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (aiExpanded) "  −" else "  ›", fontSize = 22.sp)
                }
            }
        }
        if (aiExpanded) {
        SettingCard(title = "对局提示", design = design) {
            val aiConfig by viewmodel.aiConfig.collectAsState()
            var showApiKey by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl)
            ) {
                // AI 开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("启用 AI 助手", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Switch(
                        checked = aiConfig.isEnabled,
                        onCheckedChange = { viewmodel.handleAiIntent(AiIntent.ToggleAi(it)) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(design.spacing.lg))

                // Provider 选择
                Text(
                    text = "AI 服务",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                AppSegmentedControl(
                    options = AiProvider.entries.map { it.displayName },
                    selectedIndex = AiProvider.entries.indexOf(aiConfig.provider),
                    onSelected = { index ->
                        viewmodel.handleAiIntent(
                            AiIntent.UpdateProvider(AiProvider.entries[index])
                        )
                    }
                )

                Spacer(modifier = Modifier.height(design.spacing.lg))

                // API Key 输入框（仅 DeepSeek 显示）
                if (aiConfig.provider == AiProvider.DEEP_SEEK) {
                    Text(
                        text = "API Key",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = aiConfig.apiKey,
                        onValueChange = { viewmodel.handleAiIntent(AiIntent.UpdateApiKey(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "请输入 DeepSeek API Key",
                                fontSize = 14.sp
                            )
                        },
                        visualTransformation = if (showApiKey) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    if (showApiKey) Icons.Rounded.VisibilityOff
                                    else Icons.Rounded.Visibility,
                                    contentDescription = if (showApiKey) "隐藏" else "显示",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(design.spacing.lg))
                }

                // AI 风格选择
                Text(
                    text = "回复风格",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                AppSegmentedControl(
                    options = AiStyle.entries.map { it.displayName },
                    selectedIndex = AiStyle.entries.indexOf(aiConfig.aiStyle),
                    onSelected = { index ->
                        viewmodel.handleAiIntent(
                            AiIntent.UpdateStyle(AiStyle.entries[index])
                        )
                    }
                )

                // 当前状态提示
                if (aiConfig.isEnabled) {
                    Spacer(modifier = Modifier.height(design.spacing.md))
                    val statusText = when {
                        aiConfig.provider == AiProvider.FALLBACK -> "当前使用本地预设模式"
                        aiConfig.apiKey.isBlank() -> "⚠️ API Key 未设置，将使用本地预设"
                        else -> "✅ AI 助手已就绪"
                    }
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }


        }
        SettingCard("关于", design) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Text("桌游助手", Modifier.weight(1f))
                    Text("v${PlatformHelper.getInstance().getAppVersionName()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("联系与反馈", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.foundation.text.selection.SelectionContainer { Text("YvesSword@outlook.com", color = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

}

@Composable
private fun PersonalToolTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    androidx.compose.material3.Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    design: org.walks.gamecopilot.theme.AppDesignSystem,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
        shape = RoundedCornerShape(design.cornerRadius.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = design.spacing.xl,
                    end = design.spacing.xl,
                    top = design.spacing.lg,
                    bottom = design.spacing.sm
                )
            )
            content()
        }
    }
}

@Composable
private fun ThemeOptionRow(
    icon: ImageVector,
    label: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
