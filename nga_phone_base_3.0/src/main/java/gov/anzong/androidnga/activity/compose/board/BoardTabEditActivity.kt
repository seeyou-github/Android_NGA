package gov.anzong.androidnga.activity.compose.board

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.justwen.androidnga.ui.compose.BaseComposeActivity
import com.justwen.androidnga.ui.compose.widget.TopAppBarData
import gov.anzong.androidnga.core.board.data.BoardEntity

class BoardTabEditActivity : BaseComposeActivity() {

    private val vm: BoardTabEditViewModel by lazy {
        ViewModelProvider(this)[BoardTabEditViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        title = "板块管理"
        super.onCreate(savedInstanceState)
    }

    override fun getTopAppBarData(): TopAppBarData {
        return TopAppBarData(title = title.toString()).also {
            it.navigationIconAction = { finish() }
        }
    }

    @Composable
    override fun ContentView() {
        val roots = vm.roots
        val hidden = vm.hidden

        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    vm.save()
                    finish()
                }) {
                    Text("保存")
                }
                Spacer(Modifier.padding(8.dp))
                Text("可排序 / 显示隐藏", color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
            ) {
                itemsIndexed(roots) { index, item ->
                    BoardRow(
                        index = index,
                        item = item,
                        hidden = hidden.contains(item.id),
                        onToggleHidden = { vm.toggleHidden(item.id) },
                        onMoveUp = { vm.move(index, index - 1) },
                        onMoveDown = { vm.move(index, index + 1) },
                    )
                }
            }
        }
    }

    @Composable
    private fun BoardRow(
        index: Int,
        item: BoardEntity,
        hidden: Boolean,
        onToggleHidden: () -> Unit,
        onMoveUp: () -> Unit,
        onMoveDown: () -> Unit,
    ) {
        val canMoveUp = index > 0
        val canMoveDown = index < vm.roots.size - 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .clickable { onToggleHidden() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = !hidden, onCheckedChange = { onToggleHidden() })
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                color = if (hidden) Color.Gray else MaterialTheme.colors.onBackground,
            )
            Text(
                text = "↑",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable(enabled = canMoveUp) { onMoveUp() },
                color = if (canMoveUp) Color(0xFF666666) else Color(0xFFBBBBBB)
            )
            Text(
                text = "↓",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clickable(enabled = canMoveDown) { onMoveDown() },
                color = if (canMoveDown) Color(0xFF666666) else Color(0xFFBBBBBB)
            )
        }
    }
}
