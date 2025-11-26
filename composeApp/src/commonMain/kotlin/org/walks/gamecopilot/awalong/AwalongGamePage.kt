package org.walks.gamecopilot.awalong


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.yi.yigamecopilot.android.theme.CangSe
import com.yi.yigamecopilot.android.theme.Chi
import com.yi.yigamecopilot.android.theme.GoldanColorList
import com.yi.yigamecopilot.android.theme.KuHuang
import com.yi.yigamecopilot.android.theme.MoSe
import com.yi.yigamecopilot.android.theme.MorandiBlue
import com.yi.yigamecopilot.android.theme.QiHei
import com.yi.yigamecopilot.android.theme.WuJin
import com.yi.yigamecopilot.android.theme.YinBai
import com.yi.yigamecopilot.android.theme.ZhuQing
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_captain
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 *  Created by Wing at 17:39 on 2025/5/20
 *  阿瓦隆游戏界面
 */


// 优化后的页面组件
@Composable
private fun PageContent(
    text: String,
    gameRule: String,
    bgColor: Color,
    content: @Composable () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                CangSe,
                shape = RoundedCornerShape(
                    topEnd = if (showHelp) 16.dp else 32.dp,
                    topStart = if (showHelp) 16.dp else 32.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
            ),

        ) {
        AnimatedVisibility(visible = showHelp) {
            Column(
                modifier = Modifier.fillMaxHeight(0.5f).scrollable(
                    rememberScrollState(), orientation = Orientation.Vertical
                )
            ) {
                Text(
                    text = "游戏规则:\n${gameRule}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight(1f).fillMaxWidth()
                .background(
                    bgColor,
                    shape = RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                content()
            }

            Row {
                Icon(
                    painter = painterResource(Res.drawable.icon_info),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.clickable {
                        showHelp = !showHelp
                    }
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.LightGray.copy(0.5f),
                    textAlign = TextAlign.End,

                    )
            }
        }


    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AwalongGamePage(viewmodel: MainViewmodel) {
    val gameConfig = viewmodel.awalongConfigState.value

    val nameChange = { newName: String, no: Int ->
        viewmodel.handleAwalongGameIntent(AwalongIntent.ChangeNickName(newName, no))
    }
    val pages = mutableListOf<@Composable () -> Unit>(
        {
            PageContent(
                "第零日",
                bgColor = Color(0xff161823),
                gameRule = gameConfig.description,
                content = {
                    PageDayZero(
                        viewmodel.awalongGameState.value.roleList,
                        viewmodel.awalongGameState.value.nickNameList,
                        nameChange
                    )
                })
        },
    )
    gameConfig.process.forEachIndexed { index, taskNum ->
        pages.add({
            PageContent(
                "",
                gameRule = gameConfig.description,
                bgColor = GoldanColorList[index % 5],
                content = {
                    Box(contentAlignment = Alignment.BottomCenter) {
                        PageDayTask(
                            viewmodel.awalongGameState.value.roleList,
                            viewmodel.awalongGameState.value.nickNameList,
                            gameConfig.process[index],
                            viewmodel.awalongGameState.value.dayList.getOrNull(index),
                            onCheck = { map, result, cap ->
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.CheckTask(
                                        AwalongGameDayEntity(
                                            day = index,
                                            mainTask = map,
                                            taskResult = result,
                                            murderTask = -1,
                                            captain = cap
                                        )
                                    )
                                )

                            }
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                            item {
                                Text("第${index+1}日任务进度：", color = MaterialTheme.colorScheme.secondary)
                            }
                            items(viewmodel.awalongGameState.value.dayList.size) {
                                val day = viewmodel.awalongGameState.value.dayList.getOrNull(it)
                                Box(
                                    modifier = Modifier.size(18.dp).border(
                                        2.dp,
                                        if (index == it) KuHuang else Color.White,
                                        CircleShape
                                    )
                                        .background(
                                            color = if (index == it) KuHuang else Color.Transparent,
                                            CircleShape
                                        )
                                        ,
                                ){
                                    when (day?.taskResult) {
                                        1 -> Icon(
                                            imageVector = Icons.Sharp.Check,
                                            modifier = Modifier.size(18.dp),
                                            contentDescription = "",
                                            tint = ZhuQing
                                        )

                                        -1 -> Icon(
                                            imageVector = Icons.Sharp.Close,
                                            modifier = Modifier.size(18.dp),
                                            contentDescription = "",
                                            tint = Chi
                                        )
                                    }
                                }
                            }
                        }
                    }

                })
        })
    }
    val pageState = rememberPagerState(initialPage = 0, pageCount = { pages.size })

    HorizontalPager(
        state = pageState
    ) { page ->
        pages[page]()
    }
}


// 独立定义每个页面组件
@Composable
fun StoryPage(title: String, content: String) {
    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PageDayTask(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskNum: Int,
    dayEntity: AwalongGameDayEntity?,
    onCheck: (Map<Int, Int>, Int, Int) -> Unit
) {
    var process by remember { mutableStateOf(0) }
    var result by remember { mutableStateOf(0) }
    var taskPlayer = remember { mutableStateListOf<Int>() }

    val taskCaptain = remember { mutableStateOf(dayEntity?.captain) }
    if (dayEntity != null) {
        taskPlayer.addAll(dayEntity.mainTask.keys)
        if (dayEntity.mainTask.values.size == taskNum && !dayEntity.mainTask.values.contains(0)) {
            process = 2
        }
        result = dayEntity.taskResult
    }

    val taskMap = remember {
        mutableMapOf<Int, Int>()
    }

    var showDialog by remember { mutableStateOf(-1) }
    Column {
        Text("请选择 $taskNum 位要执行任务的玩家：")
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(roleList.size) {
                Box(
                    contentAlignment = Alignment.TopEnd,

                    ) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp, end = 8.dp).background(
                            color = if (taskPlayer.contains(it)) MoSe else YinBai,
                            RoundedCornerShape(8.dp)
                        )
                            .border(2.dp, MoSe, RoundedCornerShape(8.dp)).padding(8.dp).clickable {
                                if (process == 0) {
                                    if (!taskPlayer.contains(it) && taskPlayer.size < taskNum) {
                                        taskPlayer.add(it)
                                    } else {
                                        taskPlayer.remove(it)
                                    }
                                } else if (process == 1) {
                                    showDialog = it
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = nicknameList[it],
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(100.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Row {
                            Text(text = "${it + 1}号", color = MaterialTheme.colorScheme.secondary)
                            AnimatedVisibility(taskMap[it] == 1 || taskMap[it] == -1) {
                                Icon(imageVector = Icons.Sharp.Check, contentDescription = null)
                            }
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(taskCaptain.value == it) {
                        Icon(
                            painter = painterResource(Res.drawable.icon_captain),
                            "",
                            modifier = Modifier.size(24.dp).rotate(45f),
                            tint = Color.Unspecified,
                        )
                    }

                }

            }
            item(span = { GridItemSpan(3) }) {
                if (process == 0) {
                    TextButton(
                        onClick = {
                            if (taskPlayer.size < taskNum) {

                            } else {
                                process = 1
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = WuJin)
                    ) {
                        Text("锁定玩家")
                    }
                } else if (process == 1) {
                    TextButton(
                        onClick = {
                            if (!taskMap.values.contains(0) && taskMap.values.size == taskNum) {
                                result = if (taskMap.values.contains(-1)) -1 else 1
                                process = 2
                                onCheck(taskMap, result, roleList.indices.random())
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = QiHei)
                    ) {
                        Text("开始验证")
                    }
                } else {
                    TextButton(
                        onClick = { },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (result == 1) ZhuQing else Chi)
                    ) {
                        Text(if (result == 1) "任务完成" else "任务失败")
                    }
                }

            }
        }
    }
    if (showDialog >= 0) {
        Dialog(onDismissRequest = { showDialog = -1 }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.75f).padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = MorandiBlue)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    TextButton(
                        onClick = {
                            taskMap[showDialog] = -1
                            showDialog = -1
                        },
                        enabled = roleList[showDialog].roleType == BAD_PERSON,
                        colors = ButtonDefaults.buttonColors(containerColor = Chi)
                    ) {
                        Text("阻止任务")
                    }
                    TextButton(onClick = {
                        taskMap[showDialog] = 1
                        showDialog = -1
                    }) {
                        Text("执行任务")
                    }
                }
            }
        }
    }


}

@Composable
private fun PageDayZero(
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
    val nickList = remember { mutableStateListOf(nicknameList) }
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
                            LazyColumn() {
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
    Row(
        modifier = Modifier.background(
            shape = RoundedCornerShape(16.dp),
            color = YinBai
        ).padding(16.dp).clickable {
            onItemClick()
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${sn + 1}号", fontWeight = FontWeight.W900, color = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f)) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                enabled = isEditing,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.W900),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.secondary.copy(0.66f),
                    disabledTextColor = MaterialTheme.colorScheme.secondary.copy(0.66f),
                ),
                leadingIcon = {
                    Icon(
                        imageVector = if (isEditing) Icons.Sharp.CheckCircle else Icons.Sharp.Edit,
                        contentDescription = null,
                        tint = Color.Gray,
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
        AnimatedVisibility(
            isSelected,
            modifier = Modifier.border(4.dp, Color.DarkGray, RoundedCornerShape(10.dp)).background(
                color = Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ).padding(4.dp)
        ) {
            actionButtonIcon?.invoke()
        }

    }
}

@Composable
private fun PageDayOne() {
    Column(Modifier.padding(16.dp).fillMaxSize().background(color = Color.Cyan)) {
        Text("第一天：任务阶段", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("• 选择执行任务队员\n• 进行第一次投票", style = MaterialTheme.typography.bodyLarge)
    }
}


