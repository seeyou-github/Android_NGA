package gov.anzong.androidnga.activity.compose.backup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.justwen.androidnga.ui.compose.BaseComposeFragment
import gov.anzong.androidnga.R
import gov.anzong.androidnga.base.util.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BackupFragment : BaseComposeFragment() {

    private val viewModel: BackupViewModel by lazy {
        ViewModelProvider(this)[BackupViewModel::class.java]
    }

    private var createDocLauncher: ActivityResultLauncher<String>? = null
    private var openDocLauncher: ActivityResultLauncher<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "数据导入/导出"
        createDocLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri: Uri? ->
            if (uri != null) {
                LogD("SAF 返回导出 Uri: $uri")
                lifecycleScope.launch(Dispatchers.IO) {
                    val ok = try {
                        requireContext().contentResolver
                            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    } catch (t: Throwable) {
                        false
                    }
                    LogD("takePersistable WRITE 返回 persistent=$ok")
                }
                viewModel.exportTo(requireContext(), uri)
            } else {
                LogD("用户取消选择导出目标")
            }
        }

        openDocLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                LogD("SAF 返回导入 Uri: $uri")
                lifecycleScope.launch(Dispatchers.IO) {
                    val ok = try {
                        requireContext().contentResolver
                            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (t: Throwable) {
                        false
                    }
                    LogD("takePersistable READ 返回 persistent=$ok")
                }
                viewModel.importFrom(requireContext(), uri)
            } else {
                LogD("用户取消选择导入文件")
            }
        }
    }

    @Composable
    override fun ContentView() {
        val state by viewModel.state.observeAsState(BackupUiState.Idle)

        LaunchedEffect(state) {
            val current = state
            if (current is BackupUiState.Error) {
                showErrorDialog(current)
                viewModel.resetToIdle()
            } else if (current is BackupUiState.ExportDone) {
                ToastUtils.success("导出完成")
                viewModel.resetToIdle()
            } else if (current is BackupUiState.ImportDone) {
                ToastUtils.success("导入完成")
                viewModel.resetToIdle()
            }
        }

        BackupContent(
            state = state,
            onExportClick = {
                ensureSafAndThen {
                    val fileName = "nga_backup_${System.currentTimeMillis()}.json"
                    LogD("触发 SAF CreateDocument，请求文件名: $fileName")
                    createDocLauncher?.launch(fileName)
                }
            },
            onImportClick = {
                ensureSafAndThen {
                    LogD("触发 SAF OpenDocument")
                    openDocLauncher?.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            },
        )
    }

    private fun ensureSafAndThen(block: () -> Unit) {
        block()
    }

    private fun showErrorDialog(error: BackupUiState.Error) {
        val ctx = context ?: return
        val dialog = AlertDialog.Builder(ctx)
            .setTitle(
                when (error.stage) {
                    BackupUiState.Stage.EXPORT -> "导出失败"
                    BackupUiState.Stage.IMPORT -> "导入失败"
                    BackupUiState.Stage.IMPORT_PARTIAL -> "部分导入失败"
                }
            )
            .setMessage(error.message)
            .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
            .setNeutralButton("查看完整堆栈") { d, _ ->
                d.dismiss()
                val stackDialog = AlertDialog.Builder(ctx)
                    .setTitle("完整堆栈")
                    .setMessage(
                        runCatching {
                            error.error.javaClass.name + ": " + (error.error.message ?: "") + "\n\n" + error.stackTrace
                        }.getOrDefault(error.stackTrace)
                    )
                    .setPositiveButton(android.R.string.ok) { d2, _ -> d2.dismiss() }
                    .create()
                stackDialog.show()
            }
            .create()
        dialog.show()
    }

    private fun LogD(msg: String) {
        gov.anzong.androidnga.common.util.LogUtils.d("BackupFragment", msg)
    }
}

@Composable
private fun BackupContent(
    state: BackupUiState,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "数据导入/导出",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "导出内容:全局设置(白名单项)、屏蔽规则、用户账号/凭证。\n" +
                    "不包含:浏览/搜索历史、板块/帖子缓存、WebView 会话。",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BackupActionRow(
            iconResId = R.drawable.ic_file_download,
            label = "导出数据",
            desc = "选择一个目录或文件路径，将本地配置导出为 JSON 文件",
            enabled = state !is BackupUiState.Working,
            onClick = onExportClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        BackupActionRow(
            iconResId = R.drawable.ic_action_forward,
            label = "导入数据",
            desc = "选择一个之前导出的 JSON 文件，覆盖本地配置",
            enabled = state !is BackupUiState.Working,
            onClick = onImportClick,
        )

        Spacer(modifier = Modifier.height(24.dp))

        StatusView(state = state)
    }
}

@Composable
private fun BackupActionRow(
    iconResId: Int,
    label: String,
    desc: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            tint = if (enabled) Color.DarkGray else Color.LightGray,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color.DarkGray else Color.LightGray,
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
    }
}

@Composable
private fun StatusView(state: BackupUiState) {
    val message = when (state) {
        is BackupUiState.Idle -> null
        is BackupUiState.Working -> if (state.isExport) "正在导出..." else "正在导入..."
        is BackupUiState.ExportDone -> "导出完成:设置 ${state.result.settingsCount} 项，屏蔽用户 ${state.result.filterUserCount}，屏蔽关键词 ${state.result.filterKeywordCount}，账号 ${state.result.userCount}"
        is BackupUiState.ImportDone -> "导入完成:设置 ${state.count.settings.size}，屏蔽用户 ${state.count.filterUsers.size}，屏蔽关键词 ${state.count.filterKeywords.size}，账号 ${state.count.users.size}"
        is BackupUiState.Error -> state.message
    }
    if (message != null) {
        val color = when (state) {
            is BackupUiState.Error -> Color(0xFFB00020)
            else -> Color.DarkGray
        }
        Text(
            text = message,
            color = color,
            fontSize = 13.sp,
        )
    }
}
