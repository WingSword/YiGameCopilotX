package org.walks.gamecopilot.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.WordImportEngine

/**
 * 词库导入对话框
 * 支持：粘贴导入、模板下载（复制到剪贴板）、校验错误提示
 *
 * @param title 对话框标题
 * @param isSpyMode true=谁是卧底（词对模式），false=你画我猜（单词汇模式）
 * @param onDismiss 关闭回调
 * @param onImportSuccess 导入成功回调，参数为原始文本
 */
@Composable
fun WordImportDialog(
    title: String,
    isSpyMode: Boolean,
    onDismiss: () -> Unit,
    onImportSuccess: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lineErrors by remember { mutableStateOf<List<WordImportEngine.LineError>>(emptyList()) }
    var warnings by remember { mutableStateOf<List<String>>(emptyList()) }
    var isImported by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 成功状态
                if (isImported) {
                    ImportSuccessView(
                        warnings = warnings,
                        onDismiss = onDismiss
                    )
                    return@Column
                }

                // 操作按钮行：复制模板 + 粘贴
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 复制模板按钮
                    OutlinedButton(
                        onClick = {
                            val template = if (isSpyMode) {
                                WordImportEngine.spyTemplate()
                            } else {
                                WordImportEngine.drawTemplate()
                            }
                            clipboardManager.setText(AnnotatedString(template))
                            PlatformHelper.getInstance().vibrateMethod()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制模板", fontSize = 13.sp)
                    }

                    // 粘贴按钮
                    OutlinedButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text ?: ""
                            if (clipText.isNotBlank()) {
                                inputText = clipText
                                PlatformHelper.getInstance().vibrateMethod()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("从剪贴板粘贴", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 格式提示
                Text(
                    text = if (isSpyMode) {
                        "格式：每行一对词汇，用 - 分隔（如：牛奶 - 豆浆）"
                    } else {
                        "格式：每行一个词汇（如：苹果）"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 输入框
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        errorMessage = null
                        lineErrors = emptyList()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = {
                        Text(
                            if (isSpyMode) "在此粘贴或输入词库内容...\n牛奶 - 豆浆\n橙子 - 橘子"
                            else "在此粘贴或输入词库内容...\n苹果\n香蕉\n长颈鹿",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    isError = errorMessage != null,
                    shape = RoundedCornerShape(8.dp)
                )

                // 错误提示
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ErrorView(message = errorMessage!!, lineErrors = lineErrors)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 导入按钮
                Button(
                    onClick = {
                        val result = if (isSpyMode) {
                            WordImportEngine.parseSpyPairs(inputText)
                        } else {
                            WordImportEngine.parseDrawWords(inputText)
                        }

                        when (result) {
                            is WordImportEngine.ImportResult.SpyPairs -> {
                                PlatformHelper.getInstance().vibrateLongMethod()
                                warnings = result.warnings
                                isImported = true
                                onImportSuccess(inputText)
                            }
                            is WordImportEngine.ImportResult.DrawWords -> {
                                PlatformHelper.getInstance().vibrateLongMethod()
                                warnings = result.warnings
                                isImported = true
                                onImportSuccess(inputText)
                            }
                            is WordImportEngine.ImportResult.Error -> {
                                errorMessage = result.message
                                lineErrors = result.lineErrors
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("导入词库", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * 错误信息展示
 */
@Composable
private fun ErrorView(
    message: String,
    lineErrors: List<WordImportEngine.LineError>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
            .padding(12.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        if (lineErrors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            // 最多显示前 5 个错误
            lineErrors.take(5).forEach { error ->
                Text(
                    text = "第${error.lineNumber}行: ${error.reason}",
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
            if (lineErrors.size > 5) {
                Text(
                    text = "... 还有 ${lineErrors.size - 5} 处错误",
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 导入成功展示
 */
@Composable
private fun ImportSuccessView(
    warnings: List<String>,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "导入成功",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (warnings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                warnings.forEach { warning ->
                    Text(
                        text = warning,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("完成", fontWeight = FontWeight.SemiBold)
        }
    }
}
