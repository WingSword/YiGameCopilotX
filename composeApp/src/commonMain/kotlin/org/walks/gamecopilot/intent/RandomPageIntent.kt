package org.walks.gamecopilot.intent

import org.walks.gamecopilot.data.RandomListEntity

/**
 *  Created by Wing at 23:12 on 2025/4/27
 *
 */
sealed class RandomPageIntent {
    object OnRefresh : RandomPageIntent()
    data class OnAddNewRandom(val cardList:RandomListEntity) : RandomPageIntent()
    object OnAddNewRandomDialogDismiss : RandomPageIntent()
    object OnAddNewRandomDialogSave : RandomPageIntent()
    object OnAddNewRandomDialogDelete : RandomPageIntent()
    data object OnChangeNewRandomLabel : RandomPageIntent()
    data class OnSelectLabel(val label:String) : RandomPageIntent()
    data class OnCancelLabel(val label:String) : RandomPageIntent()
}