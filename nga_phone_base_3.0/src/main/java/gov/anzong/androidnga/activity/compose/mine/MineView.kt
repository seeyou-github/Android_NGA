package gov.anzong.androidnga.activity.compose.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MineView(
    hasLogin: Boolean,
    onLoginClick: () -> Unit,
    onOpenAccountManager: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (hasLogin) "我的" else "未登录",
            style = MaterialTheme.typography.h6,
        )

        if (!hasLogin) {
            ActionRow(title = "登录账号", subtitle = "点击登录") { onLoginClick() }
        } else {
            ActionRow(title = "账号管理", subtitle = "切换/新增/删除") { onOpenAccountManager() }
        }

        Spacer(Modifier.padding(top = 8.dp))
        Text(text = "功能迁移中：抽屉菜单将逐步搬到这里", color = Color.Gray)
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.body1)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.body2, color = Color.Gray)
            }
        }
        Text(text = ">", color = Color.Gray)
    }
}
