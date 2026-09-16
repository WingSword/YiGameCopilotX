package org.walks.gamecopilot.ui.page.multiplayer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.walks.gamecopilot.online.CloudInvitation
import org.walks.gamecopilot.online.CloudInvitations
import org.walks.gamecopilot.online.CloudRoomClient as Cloud
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppDialogActions
import org.walks.gamecopilot.ui.components.AppPrimaryAction

@Composable
fun CloudInvitationDialog(onDismiss: () -> Unit) {
    var invitation by remember { mutableStateOf<CloudInvitation?>(null) }
    var webUrl by remember { mutableStateOf(CloudInvitations.webAddress) }
    var copied by remember { mutableStateOf(false) }
    val busy by Cloud.busy.collectAsState()
    val error by Cloud.error.collectAsState()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { invitation = Cloud.invitation(webUrl) }
    AppDialog(
        title = "扫码加入网页版",
        subtitle = "手机展示二维码，朋友用浏览器加入",
        onDismiss = onDismiss,
        modifier = Modifier.widthIn(max = 480.dp),
        actions = {
            if(invitation != null) AppDialogActions(
                confirmText = if(copied) "已复制邀请链接" else "复制邀请链接",
                onConfirm = { invitation?.let { clipboard.setText(AnnotatedString(it.url)); copied = true } },
                dismissText = "关闭二维码",
                onDismiss = onDismiss
            ) else OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("关闭二维码") }
        }
    ) {
        val current = invitation
        if(current != null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                InvitationQr(current.modules, Modifier.widthIn(max = 320.dp).fillMaxWidth().aspectRatio(1f))
            }
            Text("用手机相机或浏览器扫码，填写昵称即可加入。", style = MaterialTheme.typography.bodyMedium)
            Text("邀请仅用于入房，房间解散后失效。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            if(busy) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                Text("正在生成邀请…", style = MaterialTheme.typography.bodyMedium)
            }
            if(error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            OutlinedTextField(webUrl, { webUrl = it }, label = { Text("网页版地址") }, supportingText = { Text("服务已配置时可留空") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            AppPrimaryAction("生成邀请二维码", onClick = { scope.launch { invitation = Cloud.invitation(webUrl) } }, enabled = !busy)
        }
    }
}

@Composable
private fun InvitationQr(rows: List<String>, modifier: Modifier) {
    val valid = rows.size in 21..177 && rows.all { it.length == rows.size && it.all { bit -> bit == '0' || bit == '1' } }
    if(!valid) { Text("二维码数据无效，请重新生成"); return }
    Canvas(modifier.background(Color.White).semantics { contentDescription = "房间邀请二维码" }) {
        val cells = rows.size + 8
        val unit = kotlin.math.floor(minOf(size.width, size.height) / cells)
        val left = (size.width - cells * unit) / 2; val top = (size.height - cells * unit) / 2
        rows.forEachIndexed { y, row -> row.forEachIndexed { x, bit ->
            if(bit == '1') drawRect(Color.Black, Offset(left + (x+4)*unit, top + (y+4)*unit), Size(unit, unit))
        } }
    }
}
