package org.walks.gamecopilot.ui.page.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun SettingPage(viewmodel: MainViewmodel) {
    val design = LocalAppDesign.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = design.spacing.xl)
            .verticalScroll(scrollState)
            .padding(top = design.spacing.xl, bottom = design.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(design.spacing.lg)
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = design.spacing.sm,
                vertical = design.spacing.md
            )
        ) {
            Text(
                text = "设置中心",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }

        SettingCard(title = "AI 助手", design = design) {
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

        SettingCard(title = "版本信息", design = design) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("当前版本", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = PlatformHelper.getInstance().getAppVersionName(),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(design.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("构建版本", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = PlatformHelper.getInstance().getAppVersionCode().toString(),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        SettingCard(title = "外观主题", design = design) {
            val currentTheme by viewmodel.themeMode.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl)
            ) {
                ThemeOptionRow(
                    icon = Icons.Rounded.SettingsBrightness,
                    label = "跟随系统",
                    desc = "根据系统设置自动切换",
                    selected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { viewmodel.setThemeMode(ThemeMode.SYSTEM) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ThemeOptionRow(
                    icon = Icons.Rounded.LightMode,
                    label = "浅色模式",
                    desc = "明亮清新的界面风格",
                    selected = currentTheme == ThemeMode.LIGHT,
                    onClick = { viewmodel.setThemeMode(ThemeMode.LIGHT) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ThemeOptionRow(
                    icon = Icons.Rounded.DarkMode,
                    label = "深色模式",
                    desc = "护眼沉浸的暗色风格",
                    selected = currentTheme == ThemeMode.DARK,
                    onClick = { viewmodel.setThemeMode(ThemeMode.DARK) }
                )
            }
        }

        SettingCard(title = "应用介绍", design = design) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl)
            ) {
                Text(
                    text = "桌游助手是一款专为桌游爱好者设计的智能助手应用。" +
                            "集成多款热门派对桌游，提供便捷的工具和辅助功能，让游戏体验更加流畅。",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(design.spacing.lg))

                SectionTitle(text = "支持游戏")

                Text(
                    text = "• 谁是卧底 — 发言推理，找出卧底\n" +
                            "• 阿瓦隆 — 阵营博弈，任务对抗\n" +
                            "• 你画我猜 — 创意表达，趣味猜词\n" +
                            "• 猎巫镇 — 身份对抗，白天讨论夜晚行动\n" +
                            "• 一夜终极狼人 — 快速推理，一夜定胜负",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = design.spacing.md)
                )

                Spacer(modifier = Modifier.height(design.spacing.lg))

                SectionTitle(text = "随机工具")

                Text(
                    text = "• 骰子、硬币、转盘、指转盘、卡牌\n" +
                            "• 支持自定义配置和编辑\n" +
                            "• 适合各类桌游辅助决策",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = design.spacing.md)
                )
            }
        }

        SettingCard(title = "联系反馈", design = design) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl)
            ) {
                Text(
                    text = "如果您在使用过程中遇到任何问题或有任何建议，欢迎通过以下方式联系我们。" +
                            "我们非常重视您的反馈，将尽快为您提供帮助。",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(design.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("反馈邮箱", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "YvesSword@outlook.com",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(design.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("反馈类型", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "问题反馈、功能建议、合作咨询",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(design.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("响应时间", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "1-3个工作日内",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
        elevation = CardDefaults.cardElevation(defaultElevation = design.elevation.card),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
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
