package org.walks.gamecopilot.awalong


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yi.yigamecopilot.android.theme.AiLv
import com.yi.yigamecopilot.android.theme.AnSe
import com.yi.yigamecopilot.android.theme.Chi
import com.yi.yigamecopilot.android.theme.WuJin
import com.yi.yigamecopilot.android.theme.YinBai
import org.walks.gamecopilot.MainViewmodel

/**
 *  Created by Wing at 17:39 on 2025/5/20
 *  阿瓦隆游戏界面
 */


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AwalongGamePage(viewmodel: MainViewmodel) {
    // 使用优化后的游戏页面
    AwalongGamePageOptimized(viewmodel)
}

@Composable
fun PageDayZero(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    onNameChange: (String, Int) -> Unit
) {
    val selectList = remember {
        mutableStateListOf<Int>()
    }

    var nickNameList by remember {
        mutableStateOf(nicknameList)
    }
    var showGameRole by remember { mutableStateOf<AwalongRole?>(null) }
    var roles = remember {
        mutableStateListOf<AwalongRole>().apply {
            this.addAll(roleList)
        }
    }
    remember { mutableStateListOf(nicknameList) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        items(roles.size) { index ->
            RoleItem(
                nickName = nickNameList[index],
                sn = index, isSelected = selectList.contains(index), onItemClick = {
                    if (selectList.contains(index)) {
                        selectList.remove(index)
                    } else {
                        selectList.add(index)
                    }
                }, actionButtonIcon = {
                    Text(
                        text = "查看",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable {
                            showGameRole = roles[index]
                        }
                    )
                },
                nickNameChange = {
                    nickNameList = nickNameList.toMutableList().apply {
                        set(index, it)
                    }
                    onNameChange(it, index)
                })

        }

    }
    if (showGameRole != null) {
        Dialog(
            onDismissRequest = {
                showGameRole = null
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false // 关键属性
            )
        ) {
            showGameRole?.let { role ->
                Card(
                    modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.5f).padding(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WuJin
                    )
                ) {
                    Text(
                        text = role.title,
                        fontWeight = FontWeight.W600,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 44.sp,
                        color = if (role.roleType == GOOD_PERSON) AiLv.copy(0.75f) else Chi.copy(
                            0.75f
                        )
                    )
                    val checkList = role.checkSkills(roles)
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(
                            "[${if (role.roleType == GOOD_PERSON) "好人" else "坏人"}阵营]\n",
                            color = if (role.roleType == GOOD_PERSON) AiLv.copy() else Chi.copy(

                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Text(
                            role.description,
                            color = AnSe,
                        )
                        if (checkList.isNotEmpty()) {
                            Text(
                                "\n你所能看到的其他玩家的角色：\n",
                                textAlign = TextAlign.Center,
                                color = AnSe,
                            )
                            LazyColumn {
                                items(checkList.keys.toList()) {
                                    val showText =
                                        "[${it + 1} (${nickNameList[it]}) 的身份 ${if (role != AwalongRole.PAIXIWEIWEIER) checkList[it]?.title else "可能是梅林"}]"
                                    Text(
                                        text = showText,
                                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                                        color = if (role == AwalongRole.PAIXIWEIWEIER || checkList[it]?.roleType == GOOD_PERSON) AiLv else Chi,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleItem(
    sn: Int,
    isSelected: Boolean,
    nickName: String,
    onItemClick: () -> Unit,
    actionButtonIcon: (@Composable () -> Unit)? = null,
    nickNameChange: ((String) -> Unit)? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(nickName) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else YinBai
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 号码显示区域 - 与PlayerCard保持一致
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${sn + 1}号",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    enabled = isEditing,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(0.66f),
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = if (isEditing) Icons.Sharp.CheckCircle else Icons.Sharp.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                if (isEditing) {
                                    nickNameChange?.invoke(name)
                                }
                                isEditing = !isEditing
                            }
                        )
                    }
                )
            }
            
            if (isSelected) {
                actionButtonIcon?.invoke()
            }
        }
    }
}


