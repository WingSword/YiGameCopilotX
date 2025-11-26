package org.walks.gamecopilot.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.data.entity.WordGroup
import org.walks.gamecopilot.data.entity.WordGroupManager

/**
 * 词库选择器组件
 * 允许用户选择不同的词汇组
 * 
 * @param selectedGroupIds 当前选中的词组ID集合
 * @param onGroupsChanged 词组选择变化回调
 */
@Composable
fun WordGroupSelector(
    selectedGroupIds: Set<String>,
    onGroupsChanged: (Set<String>) -> Unit
) {
    val allGroups = WordGroupManager.getAllGroups()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 标题
        Text(
            text = "选择词库",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 词组选择区域
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allGroups.values.toList()) { group ->
                WordGroupItem(
                    group = group,
                    isSelected = selectedGroupIds.contains(group.id),
                    onToggle = { groupId ->
                        val newSelection = if (selectedGroupIds.contains(groupId)) {
                            selectedGroupIds - groupId
                        } else {
                            selectedGroupIds + groupId
                        }
                        onGroupsChanged(newSelection)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 词组项组件
 * 
 * @param group 词组数据
 * @param isSelected 是否选中
 * @param onToggle 切换选中状态回调
 */
@Composable
private fun WordGroupItem(
    group: WordGroup,
    isSelected: Boolean,
    onToggle: (String) -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onToggle(group.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.displayName,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        
        if (group.isBuiltIn) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "•",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}