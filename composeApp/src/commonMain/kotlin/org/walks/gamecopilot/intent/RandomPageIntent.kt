package org.walks.gamecopilot.intent

import org.walks.gamecopilot.data.RandomListEntity

/**
 *  Created by Wing at 23:12 on 2025/4/27
 *
 */
sealed class RandomPageIntent {
    data object OnRefresh : RandomPageIntent()
    data class OnAddNewRandom(val randomListEntity:RandomListEntity) : RandomPageIntent()
    data object OnAddNewRandomDialogShow : RandomPageIntent()
    data object OnAddNewRandomDialogSave : RandomPageIntent()
    data class DeleteRandomConfig(val name:String) : RandomPageIntent()
    data object OnChangeNewRandomLabel : RandomPageIntent()
    data class OnSelectLabel(val label:String) : RandomPageIntent()
    data class OnCancelLabel(val label:String) : RandomPageIntent()
}