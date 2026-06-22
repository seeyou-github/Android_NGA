package gov.anzong.androidnga.activity.compose.board

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class BoardTabEditViewModel : ViewModel() {

    private val forumBoardModel = ForumBoardModel()

    // editable root list
    val roots = mutableStateListOf<gov.anzong.androidnga.core.board.data.BoardEntity>()

    // hidden root ids
    val hidden = mutableStateListOf<String>()

    init {
        // Start from current configured order/visibility
        roots.addAll(forumBoardModel.loadHomeRootBoards())
        // Keep hidden from persisted config so toggles reflect reality
        val cfgRaw = gov.anzong.androidnga.base.util.PreferenceUtils.getData(
            gov.anzong.androidnga.common.PreferenceKey.KEY_HOME_BOARD_TAB_CONFIG,
            ""
        )
        if (!cfgRaw.isNullOrEmpty()) {
            try {
                val cfg = com.alibaba.fastjson.JSON.parseObject(cfgRaw, HomeBoardTabConfig::class.java)
                cfg?.hiddenRootIds?.let { hidden.addAll(it) }
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    fun toggleHidden(id: String) {
        if (hidden.contains(id)) {
            hidden.remove(id)
        } else {
            hidden.add(id)
        }
    }

    fun move(from: Int, to: Int) {
        if (from == to) return
        if (from !in 0 until roots.size) return
        if (to !in 0 until roots.size) return
        val item = roots.removeAt(from)
        roots.add(to, item)
    }

    fun save() {
        val visibleIds = roots.map { it.id }
        val hiddenIds = hidden.toList()
        forumBoardModel.saveHomeRootBoards(visibleIds, hiddenIds)
        // Refresh singleton VM if it's in use elsewhere.
        try {
            ForumBoardViewModel.reloadHomeBoards()
        } catch (_: Throwable) {
            // ignore
        }
    }
}
