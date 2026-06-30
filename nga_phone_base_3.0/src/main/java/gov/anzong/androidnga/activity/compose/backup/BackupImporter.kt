package gov.anzong.androidnga.activity.compose.backup

import android.content.Context
import android.content.SharedPreferences
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.justwent.androidnga.bu.UserManager
import gov.anzong.androidnga.activity.compose.board.ForumBoardViewModel
import gov.anzong.androidnga.activity.compose.filter.FilterKeyword
import gov.anzong.androidnga.activity.compose.filter.FilterManager
import gov.anzong.androidnga.base.util.PreferenceUtils
import gov.anzong.androidnga.base.utils.Files
import gov.anzong.androidnga.common.util.LogUtils
import sp.phone.common.User
import java.io.File
import java.io.InputStream

object BackupImporter {

    private const val TAG = "BackupImporter"

    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L

    fun importFromStream(
        context: Context,
        inputStream: InputStream,
        overwrite: Boolean = true,
    ): BackupImportResult {
        LogUtils.i(TAG, "==== 开始导入配置 (overwrite=$overwrite) ====")
        return try {
            val rawBytes = readBoundedStream(inputStream)
            if (rawBytes == null) {
                val errMsg = "文件读取失败或超过大小限制 ($MAX_FILE_SIZE_BYTES 字节)"
                LogUtils.e(TAG, errMsg)
                return BackupImportResult.Failure(IllegalStateException(errMsg))
            }
            LogUtils.i(TAG, "读取到 ${rawBytes.size} 字节，开始解析 JSON")

            val payload = parse(rawBytes) ?: run {
                LogUtils.e(TAG, "JSON 解析失败，返回 null")
                return BackupImportResult.Failure(IllegalStateException("JSON 解析失败"))
            }

            LogUtils.i(TAG, "解析成功: schemaVersion=${payload.schemaVersion}, exportTime=${payload.exportTime}")
            LogUtils.i(TAG, "    settings=${payload.settings.size}, filterUsers=${payload.filterUsers.size}, " +
                    "filterKeywords=${payload.filterKeywords.size}, users=${payload.users.size}, " +
                    "activeIndex=${payload.activeUserIndex}")

            applyToLocal(context, payload, overwrite)
        } catch (t: Throwable) {
            val msg = "导入异常: ${t.javaClass.simpleName}: ${t.message}"
            LogUtils.e(TAG, msg, t)
            BackupImportResult.Failure(t)
        }
    }

    private fun readBoundedStream(inputStream: InputStream): ByteArray? {
        return try {
            val buf = ByteArray(8 * 1024)
            val builder = java.io.ByteArrayOutputStream()
            var total = 0
            while (true) {
                val read = inputStream.read(buf)
                if (read <= 0) break
                total += read
                if (total > MAX_FILE_SIZE_BYTES) {
                    LogUtils.e(TAG, "文件过大：已读取 $total 字节，超过限制 $MAX_FILE_SIZE_BYTES")
                    return null
                }
                builder.write(buf, 0, read)
            }
            builder.toByteArray()
        } catch (t: Throwable) {
            LogUtils.e(TAG, "读取输入流出错: ${t.message}", t)
            null
        } finally {
            runCatching { inputStream.close() }
        }
    }

    private fun parse(bytes: ByteArray): BackupData? {
        return try {
            val jsonStr = String(bytes, Charsets.UTF_8)
            val parsed = JSON.parseObject(jsonStr, BackupData::class.java)
            LogUtils.i(TAG, "JSON.parseObject 完成: ${parsed != null}")
            if (parsed != null) {
                val userCount = parsed.users?.size ?: -1
                val filterUserCount = parsed.filterUsers?.size ?: -1
                val filterKeywordCount = parsed.filterKeywords?.size ?: -1
                val settingsCount = parsed.settings?.size ?: -1
                LogUtils.i(TAG, "    反序列化后字段内容: settings=$settingsCount, filterUsers=$filterUserCount, filterKeywords=$filterKeywordCount, users=$userCount, activeIndex=${parsed.activeUserIndex}, boardBookmarks=${parsed.boardBookmarks?.let { "存在(${it.length}字)" } ?: "null"}")
            }
            parsed
        } catch (e: JSONException) {
            LogUtils.e(TAG, "JSON 解析失败(JSONException): ${e.message}", e)
            null
        } catch (t: Throwable) {
            LogUtils.e(TAG, "JSON 解析失败(${t.javaClass.simpleName}): ${t.message}", t)
            null
        }
    }

    private fun applyToLocal(context: Context, payload: BackupData, overwrite: Boolean): BackupImportResult {
        val errors = mutableListOf<String>()

        runSafely(TAG, "settings") {
            applySettings(payload.settings, overwrite)
        }?.let { errors.add("settings: $it") }

        runSafely(TAG, "filterUsers") {
            applyFilterUsers(payload.filterUsers, overwrite)
        }?.let { errors.add("filterUsers: $it") }

        runSafely(TAG, "filterKeywords") {
            applyFilterKeywords(payload.filterKeywords, overwrite)
        }?.let { errors.add("filterKeywords: $it") }

        runSafely(TAG, "users") {
            applyUsers(payload.users, payload.activeUserIndex)
        }?.let { errors.add("users: $it") }

        runSafely(TAG, "boardBookmarks") {
            applyBoardBookmarks(context, payload.boardBookmarks, overwrite)
        }?.let { errors.add("boardBookmarks: $it") }

        if (errors.isNotEmpty()) {
            val msg = "部分子模块导入失败：${errors.joinToString("; ")}"
            LogUtils.e(TAG, msg)
            return BackupImportResult.Partial(
                success = true,
                errors = errors,
                payload = payload,
            )
        }

        LogUtils.i(TAG, "==== 导入完成，全部成功 ====")
        return BackupImportResult.Success(payload)
    }

    private inline fun runSafely(tag: String, label: String, block: () -> Unit): String? {
        LogUtils.i(TAG, "    [$label] 开始")
        return try {
            block()
            LogUtils.i(TAG, "    [$label] 成功")
            null
        } catch (t: Throwable) {
            LogUtils.e(TAG, "    [$label] 失败: ${t.javaClass.simpleName}: ${t.message}", t)
            "${t.javaClass.simpleName}: ${t.message ?: "unknown"}"
        }
    }

    private fun applySettings(settings: Map<String, Any?>, overwrite: Boolean) {
        if (settings.isEmpty()) {
            LogUtils.i(TAG, "        无 settings 待写入")
            return
        }
        val sp = PreferenceUtils.getDefaultPreferences()
        val editor = sp.edit()
        var count = 0
        for ((key, value) in settings) {
            try {
                if (key.isBlank()) continue
                if (!overwrite && sp.contains(key)) {
                    LogUtils.i(TAG, "        跳过(已存在): $key")
                    continue
                }
                val ok = putTyped(editor, key, value)
                if (ok) count++ else LogUtils.e(TAG, "        跳过(类型未知): $key = ${value?.javaClass?.simpleName}")
            } catch (t: Throwable) {
                LogUtils.e(TAG, "        写入 key=$key 出错: ${t.message}", t)
            }
        }
        editor.apply()
        LogUtils.i(TAG, "        写入 settings 共 $count 项")
    }

    private fun putTyped(editor: SharedPreferences.Editor, key: String, value: Any?): Boolean {
        return when (value) {
            null -> { editor.remove(key); true }
            is String -> { editor.putString(key, value); true }
            is Boolean -> { editor.putBoolean(key, value); true }
            is Int -> { editor.putInt(key, value); true }
            is Long -> { editor.putLong(key, value); true }
            is Float -> { editor.putFloat(key, value); true }
            is Double -> { editor.putLong(key, value.toLong()); true }
            is Set<*> -> {
                @Suppress("UNCHECKED_CAST")
                val stringSet = value as? Set<String>
                if (stringSet != null) {
                    editor.putStringSet(key, stringSet)
                    true
                } else false
            }
            else -> false
        }
    }

    private fun applyFilterUsers(users: List<User>, overwrite: Boolean) {
        LogUtils.i(TAG, "        收到 ${users.size} 条 filter users")
        if (users.isEmpty()) return
        val existing = FilterManager.userFilterList
        if (overwrite) {
            val snapshot = existing.toList()
            for (u in snapshot) {
                runCatching { FilterManager.removeFilterUser(u) }
                    .onFailure { LogUtils.e(TAG, "        清旧 filter user 失败: ${u.userId}: ${it.message}", it) }
            }
        }
        for (u in users) {
            if (u.userId.isNullOrBlank()) {
                LogUtils.e(TAG, "        跳过 uid 为空的 filter user")
                continue
            }
            runCatching { FilterManager.addFilterUser(u) }
                .onFailure { LogUtils.e(TAG, "        添加 filter user 失败: ${u.userId}: ${it.message}", it) }
        }
    }

    private fun applyFilterKeywords(keywords: List<FilterKeyword>, overwrite: Boolean) {
        LogUtils.i(TAG, "        收到 ${keywords.size} 条 filter keywords")
        if (keywords.isEmpty()) return
        val existing = FilterManager.wordFilterList
        if (overwrite) {
            val snapshot = existing.toList()
            for (k in snapshot) {
                runCatching { FilterManager.removeFilterWord(k.keyword ?: "") }
                    .onFailure { LogUtils.e(TAG, "        清旧 filter keyword 失败: ${k.keyword}: ${it.message}", it) }
            }
        }
        for (k in keywords) {
            val kw = k.keyword.trim()
            if (kw.isEmpty()) continue
            runCatching { FilterManager.addFilterWord(kw) }
                .onFailure { LogUtils.e(TAG, "        添加 filter keyword 失败: $kw: ${it.message}", it) }
        }
    }

    private fun applyBoardBookmarks(context: Context, boardBookmarksJson: String?, overwrite: Boolean) {
        if (boardBookmarksJson.isNullOrBlank()) {
            LogUtils.i(TAG, "        备份中无收藏版面数据，跳过")
            return
        }
        val file = File(context.filesDir, "board_bookmark.json")
        if (!overwrite && file.exists()) {
            LogUtils.i(TAG, "        跳过（本地已有收藏且 overwrite=false）")
            return
        }
        runCatching {
            Files.writeFile(file, boardBookmarksJson)
            LogUtils.i(TAG, "        成功写入 board_bookmark.json (${boardBookmarksJson.length} 字节)")
            ForumBoardViewModel.reloadBookmarkBoard()
            LogUtils.i(TAG, "        已刷新内存中收藏版面数据")
        }.onFailure { LogUtils.e(TAG, "        写入 board_bookmark.json 失败: ${it.message}", it) }
    }

    private fun applyUsers(users: List<User>, activeIndex: Int) {
        LogUtils.i(TAG, "        收到 ${users.size} 个 user, activeIndex=$activeIndex")
        val validUsers = users.filter { u ->
            !u.userId.isNullOrBlank() && !u.cid.isNullOrBlank()
        }
        if (validUsers.isEmpty()) {
            LogUtils.i(TAG, "        无有效的 user（缺少 uid/cid），跳过")
            return
        }
        val skipped = users.size - validUsers.size
        if (skipped > 0) {
            LogUtils.i(TAG, "        跳过 $skipped 个缺少 uid/cid 的 user")
        }

        runCatching {
            UserManager.replaceAllUsers(validUsers, activeIndex)
            LogUtils.i(TAG, "        成功替换 user 列表: ${validUsers.size} 个, activeIndex=$activeIndex")
        }.onFailure { LogUtils.e(TAG, "        替换 user 失败: ${it.message}", it) }
    }
}

sealed class BackupImportResult {
    data class Success(val payload: BackupData) : BackupImportResult()
    data class Partial(
        val success: Boolean,
        val errors: List<String>,
        val payload: BackupData,
    ) : BackupImportResult()
    data class Failure(val throwable: Throwable) : BackupImportResult()
}
