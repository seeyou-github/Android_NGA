package gov.anzong.androidnga.activity.compose.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alibaba.fastjson.JSON
import com.justwen.androidnga.base.network.retrofit.RetrofitHelper
import gov.anzong.androidnga.activity.compose.board.ForumBoardViewModel
import gov.anzong.androidnga.common.util.ForumUtils
import gov.anzong.androidnga.core.board.data.BoardEntity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import sp.phone.common.PhoneConfiguration
import sp.phone.param.ArticleListParam
import sp.phone.param.ParamKey

private data class TopicItem(
    val tid: Int,
    val fid: Int,
    val author: String,
    val authorId: Int,
    val lastPoster: String,
    val subject: String,
    val replies: Int,
    val postDate: Int,
    val isAnonymity: Boolean,
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookmarkTopicPager(forumBoardViewModel: ForumBoardViewModel) {
    val bookmarkSize by forumBoardViewModel.bookmarkSizeLiveData.observeAsState(0)
    val boardData by forumBoardViewModel.boardLiveData.observeAsState()
    val bookmarks = remember(bookmarkSize, boardData) { boardData?.getOrNull(0)?.children }

    if (bookmarks.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无收藏板块", color = MaterialTheme.colors.onBackground)
        }
        return
    }

    val tabs = bookmarks.map { it.name }

    com.justwen.androidnga.ui.compose.widget.TabLayoutWithPager(tabs = tabs, fixed = false) { index ->
        val board = bookmarks[index]
        BookmarkTopicListView(board)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BookmarkTopicListView(board: BoardEntity) {
    val topics = remember { mutableStateListOf<TopicItem>() }
    var currentPage by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var loadTrigger by remember { mutableIntStateOf(1) }
    val listState = rememberLazyListState()

    LaunchedEffect(board.fid, board.stid, loadTrigger) {
        if (isLoading) return@LaunchedEffect
        if (currentPage > 1 && !hasMore) return@LaunchedEffect
        isLoading = true
        if (currentPage == 1) isRefreshing = true
        loadTopics(board.fid, board.stid, currentPage, onResult = { items, hasNext ->
            if (currentPage == 1) topics.clear()
            topics.addAll(items)
            hasMore = hasNext
            if (PhoneConfiguration.getInstance().needSortByPostOrder() && topics.size > 1) {
                val sorted = topics.sortedByDescending { it.postDate }
                topics.clear()
                topics.addAll(sorted)
            }
            isLoading = false
            isRefreshing = false
        }, onError = {
            isLoading = false
            isRefreshing = false
        })
    }

    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            currentPage = 1
            loadTrigger++
        }
    )

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= topics.size - 3 && hasMore && !isLoading
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            currentPage++
        }
    }

    Box(modifier = Modifier.pullRefresh(refreshState)) {
        if (topics.isEmpty() && !isLoading && !isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无帖子", color = MaterialTheme.colors.onBackground)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(topics, key = { "${it.tid}_${it.postDate}" }) { topic ->
                    TopicItemView(topic)
                    Divider(color = MaterialTheme.colors.onBackground.copy(alpha = 0.12f))
                }
                if (isLoading && topics.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("加载中...", color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun TopicItemView(topic: TopicItem) {
    val context = LocalContext.current
    val titleSize = PhoneConfiguration.getInstance().topicTitleSize
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val param = ArticleListParam()
                param.tid = topic.tid
                param.page = 1
                param.title = topic.subject
                val intent = Intent(context, PhoneConfiguration.getInstance().articleActivityClass)
                val bundle = Bundle()
                bundle.putParcelable(ParamKey.KEY_PARAM, param)
                bundle.putBoolean("hide_fab", true)
                intent.putExtras(bundle)
                context.startActivity(intent)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = topic.subject,
            fontSize = titleSize.sp,
            color = MaterialTheme.colors.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = topic.author,
                fontSize = 13.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = topic.lastPoster,
                fontSize = 13.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${topic.replies}",
                fontSize = 13.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

private fun loadTopics(
    fid: Int,
    stid: Int,
    page: Int,
    onResult: (List<TopicItem>, Boolean) -> Unit,
    onError: () -> Unit,
) {
    val domain = ForumUtils.getAvailableDomain()
    val url = buildString {
        append("$domain/thread.php?fid=$fid&page=$page&lite=js&noprefix")
        if (stid != 0) append("&stid=$stid")
    }

    RetrofitHelper.getInstance().getService().get(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ json ->
            try {
                val items = parseTopicJson(json)
                onResult(items, items.isNotEmpty())
            } catch (e: Exception) {
                onResult(emptyList(), false)
            }
        }, {
            onError()
        })
}

private fun parseTopicJson(json: String): List<TopicItem> {
    val js = if (json.startsWith("window.script_muti_get_var_store=")) {
        json.substring("window.script_muti_get_var_store=".length)
    } else json

    val root = JSON.parseObject(js)
    val data = root.getJSONObject("data") ?: return emptyList()
    val topics = data.getJSONObject("__T") ?: return emptyList()
    val count = data.getIntValue("__T__ROWS")

    val items = mutableListOf<TopicItem>()
    for (i in 0 until count) {
        val t = topics.getJSONObject(i.toString()) ?: continue
        val author = t.getString("author") ?: ""
        val authorIdStr = t.getString("authorid") ?: ""
        val isAnonymity = author.startsWith("#anony_") || authorIdStr.startsWith("#anony_")
        val displayName = if (isAnonymity) getAnonymityName(if (author.startsWith("#anony_")) author else authorIdStr) else author
        val tid = t.getIntValue("tid")
        if (tid == 0) continue

        items.add(
            TopicItem(
                tid = tid,
                fid = t.getIntValue("fid"),
                author = displayName,
                authorId = authorIdStr.toIntOrNull() ?: 0,
                lastPoster = t.getString("lastposter") ?: "",
                subject = t.getString("subject") ?: "",
                replies = t.getIntValue("replies"),
                postDate = t.getIntValue("postdate"),
                isAnonymity = isAnonymity,
            )
        )
    }
    return items
}

private fun getAnonymityName(name: String): String {
    val t1 = "甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未申酉戌亥"
    val t2 = "王李张刘陈杨黄吴赵周徐孙马朱胡林郭何高罗郑梁谢宋唐许邓冯韩曹曾彭萧蔡潘田董袁于余叶蒋杜苏魏程吕丁沈任姚卢傅钟姜崔谭廖范汪陆金石戴贾韦夏邱方侯邹熊孟秦白江阎薛尹段雷黎史龙陶贺顾毛郝龚邵万钱严赖覃洪武莫孔汤向常温康施文牛樊葛邢安齐易乔伍庞颜倪庄聂章鲁岳翟殷詹申欧耿关兰焦俞左柳甘祝包宁尚符舒阮柯纪梅童凌毕单季裴霍涂成苗谷盛曲翁冉骆蓝路游辛靳管柴蒙鲍华喻祁蒲房滕屈饶解牟艾尤阳时穆农司卓古吉缪简车项连芦麦褚娄窦戚岑景党宫费卜冷晏席卫米柏宗瞿桂全佟应臧闵苟邬边卞姬师和仇栾隋商刁沙荣巫寇桑郎甄丛仲虞敖巩明佘池查麻苑迟邝"
    if (name.length == 39 && name.startsWith("#anony_")) {
        val sb = StringBuilder()
        var i = 6
        for (j in 0..5) {
            val pos: Int
            if (j == 0 || j == 3) {
                pos = name.substring(i + 1, i + 2).toInt(16)
                sb.append(t1[pos])
            } else {
                pos = name.substring(i, i + 2).toInt(16)
                sb.append(t2[pos])
            }
            i += 2
        }
        return sb.toString()
    }
    return name
}
