package gov.anzong.androidnga.activity.compose.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gov.anzong.androidnga.common.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupViewModel : ViewModel() {

    private val _state = MutableLiveData<BackupUiState>(BackupUiState.Idle)
    val state: LiveData<BackupUiState> = _state

    fun resetToIdle() {
        _state.value = BackupUiState.Idle
    }

    fun exportTo(context: Context, uri: Uri) {
        LogUtils.i(TAG, "exportTo invoked, uri=$uri")
        _state.value = BackupUiState.Working(isExport = true)
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val os = context.contentResolver.openOutputStream(uri, "wt")
                    ?: throw IllegalStateException("无法打开目标文件输出流: $uri")
                os.use { stream ->
                    BackupExporter.exportToStream(context, stream)
                }
            }.getOrElse { BackupExportResult.Failure(it) }

            withContext(Dispatchers.Main) {
                _state.value = when (result) {
                    is BackupExportResult.Success -> BackupUiState.ExportDone(result)
                    is BackupExportResult.Failure -> BackupUiState.Error(
                        stage = BackupUiState.Stage.EXPORT,
                        error = result.throwable,
                        message = friendlyMessage(BackupUiState.Stage.EXPORT, result.throwable),
                        stackTrace = LogUtils.getStackTraceString(result.throwable),
                    )
                }
            }
        }
    }

    fun importFrom(context: Context, uri: Uri) {
        LogUtils.i(TAG, "importFrom invoked, uri=$uri")
        _state.value = BackupUiState.Working(isExport = false)
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("无法打开文件输入流: $uri")
                input.use { stream ->
                    BackupImporter.importFromStream(context, stream)
                }
            }.getOrElse { BackupImportResult.Failure(it) }

            withContext(Dispatchers.Main) {
                _state.value = when (result) {
                    is BackupImportResult.Success -> BackupUiState.ImportDone(count = result.payload)
                    is BackupImportResult.Partial -> {
                        LogUtils.e(TAG, "部分导入失败: ${result.errors}")
                        BackupUiState.Error(
                            stage = BackupUiState.Stage.IMPORT_PARTIAL,
                            error = RuntimeException(result.errors.joinToString("\n")),
                            message = "部分子项导入失败",
                            stackTrace = result.errors.joinToString("\n"),
                        )
                    }
                    is BackupImportResult.Failure -> BackupUiState.Error(
                        stage = BackupUiState.Stage.IMPORT,
                        error = result.throwable,
                        message = friendlyMessage(BackupUiState.Stage.IMPORT, result.throwable),
                        stackTrace = LogUtils.getStackTraceString(result.throwable),
                    )
                }
            }
        }
    }

    private fun friendlyMessage(stage: BackupUiState.Stage, t: Throwable): String {
        val prefix = when (stage) {
            BackupUiState.Stage.EXPORT -> "导出失败"
            BackupUiState.Stage.IMPORT -> "导入失败"
            BackupUiState.Stage.IMPORT_PARTIAL -> "部分导入失败"
        }
        val type = t.javaClass.simpleName
        val msg = t.message
        return buildString {
            append(prefix).append('\n')
            append("异常类型: ").append(type).append('\n')
            if (!msg.isNullOrBlank()) append("异常信息: ").append(msg).append('\n')
            append("\n完整堆栈已写入 logcat 标签:NGAClient / BackupImporter,可过滤查看")
        }
    }

    companion object {
        private const val TAG = "BackupViewModel"
    }
}

sealed class BackupUiState {
    object Idle : BackupUiState()
    data class Working(val isExport: Boolean) : BackupUiState()
    data class ExportDone(val result: BackupExportResult.Success) : BackupUiState()
    data class ImportDone(val count: BackupData) : BackupUiState()
    data class Error(
        val stage: Stage,
        val error: Throwable,
        val message: String,
        val stackTrace: String,
    ) : BackupUiState()

    enum class Stage { EXPORT, IMPORT, IMPORT_PARTIAL }
}
