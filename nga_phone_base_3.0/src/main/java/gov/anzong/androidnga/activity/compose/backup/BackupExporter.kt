package gov.anzong.androidnga.activity.compose.backup

import android.content.Context
import android.content.SharedPreferences
import com.alibaba.fastjson.JSON
import com.justwent.androidnga.bu.UserManager
import gov.anzong.androidnga.activity.compose.filter.FilterKeyword
import gov.anzong.androidnga.activity.compose.filter.FilterManager
import gov.anzong.androidnga.base.util.PreferenceUtils
import gov.anzong.androidnga.base.utils.Files
import gov.anzong.androidnga.common.util.LogUtils
import sp.phone.common.User
import java.io.File
import java.io.OutputStream

object BackupExporter {

    private const val TAG = "BackupExporter"

    fun exportToStream(context: Context, outputStream: OutputStream): BackupExportResult {
        return try {
            LogUtils.i(TAG, "==== 开始导出配置 ====")
            val payload = collect(context)
            val json = JSON.toJSONString(payload, true)
            outputStream.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
                os.flush()
            }
            LogUtils.i(TAG, "==== 导出完成: ${json.length} 字节 ====")
            BackupExportResult.Success(
                settingsCount = payload.settings.size,
                filterUserCount = payload.filterUsers.size,
                filterKeywordCount = payload.filterKeywords.size,
                userCount = payload.users.size,
                activeIndex = payload.activeUserIndex,
            )
        } catch (t: Throwable) {
            LogUtils.e(TAG, "导出失败: ${t.javaClass.simpleName}: ${t.message}", t)
            BackupExportResult.Failure(t)
        }
    }

    private fun collect(context: Context): BackupData {
        LogUtils.i(TAG, "[1/4] 收集全局设置 (默认 SP)")
        val settingsMap = collectWhitelistedSettings().also {
            LogUtils.i(TAG, "    -> settings count=${it.size}")
        }

        LogUtils.i(TAG, "[2/4] 收集屏蔽规则 (filter SP)")
        val filterUsers = FilterManager.userFilterList.toList().also {
            LogUtils.i(TAG, "    -> filter users count=${it.size}")
        }
        val filterKeywords = FilterManager.wordFilterList.toList().also {
            LogUtils.i(TAG, "    -> filter keywords count=${it.size}")
        }

        LogUtils.i(TAG, "[3/5] 收集用户账号 (Room)")
        val users = UserManager.getUserList().toList().also {
            LogUtils.i(TAG, "    -> users count=${it.size}")
        }

        LogUtils.i(TAG, "[4/5] 收藏版面 (board_bookmark.json)")
        val boardBookmarksJson = readBoardBookmarksFile(context)
        if (boardBookmarksJson != null) {
            LogUtils.i(TAG, "    -> board_bookmark.json 内容长度=${boardBookmarksJson.length}")
        } else {
            LogUtils.i(TAG, "    -> board_bookmark.json 不存在或读取失败")
        }

        LogUtils.i(TAG, "[5/5] 当前激活账号索引")
        val activeIndex = try {
            UserManager.getActiveIndex()
        } catch (t: Throwable) {
            LogUtils.e(TAG, "    -> 获取 activeIndex 失败，使用 0: ${t.message}", t)
            0
        }

        val pkgInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (t: Throwable) {
            null
        }

        return BackupData().apply {
            this.settings = settingsMap
            this.filterUsers = filterUsers
            this.filterKeywords = filterKeywords
            this.users = users
            this.boardBookmarks = boardBookmarksJson
            this.activeUserIndex = activeIndex
            this.appVersionName = pkgInfo?.versionName
            this.appVersionCode = pkgInfo?.versionCode ?: 0
        }
    }

    private fun readBoardBookmarksFile(context: Context): String? {
        val file = File(context.filesDir, "board_bookmark.json")
        return if (file.exists()) {
            runCatching { Files.readFile(file) }.getOrNull()
        } else null
    }

    private fun collectWhitelistedSettings(): Map<String, Any?> {
        val sp = PreferenceUtils.getDefaultPreferences()
        val all: Map<String, *> = sp.all
        val result = LinkedHashMap<String, Any?>()
        for (key in BackupWhitelistKeys.GLOBAL_SETTINGS_KEYS) {
            if (all.containsKey(key)) {
                result[key] = all[key]
            }
        }
        return result
    }
}

sealed class BackupExportResult {
    data class Success(
        val settingsCount: Int,
        val filterUserCount: Int,
        val filterKeywordCount: Int,
        val userCount: Int,
        val activeIndex: Int,
    ) : BackupExportResult()

    data class Failure(val throwable: Throwable) : BackupExportResult()
}
