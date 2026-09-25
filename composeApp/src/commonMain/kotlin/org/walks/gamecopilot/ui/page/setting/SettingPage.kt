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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.AlertDialog
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
import org.walks.gamecopilot.distribution.AppDistribution
import org.walks.gamecopilot.privacy.FullPrivacyPolicy
import org.walks.gamecopilot.privacy.PrivacyPolicyDialog
import org.walks.gamecopilot.getPlatform
import androidx.compose.ui.platform.LocalUriHandler
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
    val uriHandler = LocalUriHandler.current
    val records by org.walks.gamecopilot.data.GameStatsManager.recordsFlow.collectAsState()
    val currentTheme by viewmodel.themeMode.collectAsState()
    val aiConfig by viewmodel.aiConfig.collectAsState()
    var aiExpanded by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showAiConsent by remember { mutableStateOf(false) }
    if (showPrivacy) PrivacyPolicyDialog { showPrivacy = false }
    if (showAiConsent) {
        AlertDialog(
            onDismissRequest = { showAiConsent = false },
            title = { Text("启用 DeepSeek 提示") },
            text = {
                Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("请求提示时，会将游戏类型、所需对局内容（可能包含玩家代号、身份、词语及投票）和你的 API Key 通过 HTTPS 直接发送给 DeepSeek，用于生成建议。密钥保存在本机。")
                    Text("DeepSeek 由杭州深度求索人工智能基础技术研究有限公司及其关联公司提供。你可以继续使用本地预设，或随时在这里撤回授权并清除密钥。")
                    TextButton(onClick = { uriHandler.openUri(FullPrivacyPolicy.AI_POLICY_URL) }) { Text("DeepSeek 隐私政策") }
                }
            },
            confirmButton = { TextButton(onClick = {
                viewmodel.handleAiIntent(AiIntent.UpdateConfig(aiConfig.copy(
                    provider = AiProvider.DEEP_SEEK, baseUrl = AiProvider.DEEP_SEEK.defaultBaseUrl, onlineConsent = true)))
                showAiConsent = false
            }) { Text("同意并选择 DeepSeek") } },
            dismissButton = { TextButton(onClick = { showAiConsent = false }) { Text("暂不启用") } }
        )
    }
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
                        Text("启用对局提示", color = MaterialTheme.colorScheme.onSurface)
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
                if (AppDistribution.onlineAiEnabled) {
                    AppSegmentedControl(
                        options = AiProvider.entries.map { it.displayName },
                        selectedIndex = AiProvider.entries.indexOf(aiConfig.provider),
                        onSelected = { index ->
                            val provider = AiProvider.entries[index]
                            if (provider == AiProvider.DEEP_SEEK && !aiConfig.onlineConsent) showAiConsent = true
                            else viewmodel.handleAiIntent(AiIntent.UpdateProvider(provider))
                        }
                    )
                } else {
                    Text("本地预设 · 无需联网", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(design.spacing.lg))

                // API Key 输入框（仅 DeepSeek 显示）
                if (AppDistribution.onlineAiEnabled && aiConfig.provider == AiProvider.DEEP_SEEK) {
                    Text(if (aiConfig.onlineConsent) "请求提示时，对局内容会发送至 DeepSeek。" else "尚未确认联网说明，当前使用本地预设。",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = {
                        if (!aiConfig.onlineConsent) showAiConsent = true
                        else viewmodel.handleAiIntent(AiIntent.UpdateConfig(aiConfig.copy(
                            provider = AiProvider.FALLBACK, apiKey = "", onlineConsent = false)))
                    }) { Text(if (aiConfig.onlineConsent) "撤回授权并清除密钥" else "查看并确认联网说明") }
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
                        !AppDistribution.onlineAiEnabled -> "当前使用本地预设模式"
                        aiConfig.provider == AiProvider.FALLBACK -> "当前使用本地预设模式"
                        !aiConfig.onlineConsent -> "尚未确认联网说明，使用本地预设"
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
                Text(AppDistribution.channel.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { showPrivacy = true }) { Text("隐私政策") }
                if (AppDistribution.externalUpdatesEnabled && getPlatform().name.startsWith("Android")) {
                    TextButton(onClick = { uriHandler.openUri("https://github.com/WingSword/YiGameCopilotX/releases/latest") }) {
                        Text("获取新版")
                    }
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
