package gov.anzong.androidnga.activity.compose.board

import com.alibaba.fastjson.annotation.JSONField

// Persisted config for home top-level board tabs.
class HomeBoardTabConfig {
    // Root board IDs that are visible, in display order.
    var visibleRootIds: MutableList<String> = mutableListOf()

    // Root board IDs hidden from the home tab.
    var hiddenRootIds: MutableList<String> = mutableListOf()

    @JSONField(serialize = false, deserialize = false)
    fun isVisible(id: String): Boolean = !hiddenRootIds.contains(id)
}
