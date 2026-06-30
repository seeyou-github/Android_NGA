package gov.anzong.androidnga.activity.compose.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import com.justwen.androidnga.ui.compose.BaseComposeFragment
import com.justwen.androidnga.ui.compose.theme.AppTheme
import com.justwen.androidnga.ui.compose.widget.OptionMenuData
import com.justwen.androidnga.ui.compose.widget.ScaffoldApp
import com.justwen.androidnga.ui.compose.widget.TopAppBarData
import gov.anzong.androidnga.R
import gov.anzong.androidnga.activity.compose.bottomnav.BottomNavBar
import gov.anzong.androidnga.activity.compose.bottomnav.BottomTabs
import gov.anzong.androidnga.activity.compose.board.ForumBoardViewModel
import gov.anzong.androidnga.activity.compose.home.HomeTab
import gov.anzong.androidnga.activity.compose.profile.ProfileMenuItem
import gov.anzong.androidnga.activity.compose.profile.ProfileMenuSection
import gov.anzong.androidnga.activity.compose.profile.ProfileTab
import com.justwent.androidnga.bu.UserManager
import sp.phone.common.User

class NavigationDrawerFragment : BaseComposeFragment() {

    private val viewModel: NavigationDrawerViewModel by lazy {
        ViewModelProvider(this).get(NavigationDrawerViewModel::class.java)
    }

    private val forumBoardViewModel: ForumBoardViewModel by lazy {
        ViewModelProvider(this).get(ForumBoardViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    NavigationRoot()
                }
            }
        }
    }

    private fun getHomeTopAppBarData(): TopAppBarData {
        val topAppBarData = TopAppBarData(title = getString(R.string.start_title))
        topAppBarData.navigationIconAction = null
        topAppBarData.optionMenuData = getHomeOptionMenuData()
        return topAppBarData
    }

    private fun getProfileTopAppBarData(): TopAppBarData {
        val topAppBarData = TopAppBarData(title = "我的")
        topAppBarData.navigationIconAction = null
        topAppBarData.optionMenuData = emptyList()
        return topAppBarData
    }

    private fun getHomeOptionMenuData(): List<OptionMenuData> {
        return arrayListOf(
            OptionMenuData(
                title = "搜索用户",
                action = { viewModel.startSearchActivity(requireActivity()) },
                type = OptionMenuData.OPTION_MENU_TYPE_ALWAYS_SHOW,
                icon = R.drawable.btn_ic_search,
            ),
        )
    }

    private fun buildProfileMenuSections(): List<ProfileMenuSection> {
        val replyCount = viewModel.replyCount.value
        val extra: String? = if (replyCount != null && replyCount > 0) replyCount.toString() else null

        val firstSection = arrayListOf(
            ProfileMenuItem(
                label = "登录账号",
                iconResId = R.drawable.ic_login,
                onClick = { viewModel.startLoginPage(requireActivity()) },
            ),
            ProfileMenuItem(
                label = "添加版面ID",
                iconResId = R.drawable.ic_action_add_to_queue,
                onClick = { viewModel.showAddBoardDialog(this@NavigationDrawerFragment) },
            ),
            ProfileMenuItem(
                label = "由URL读取",
                iconResId = R.drawable.ic_action_forward,
                onClick = { viewModel.forwardWithUrl(this@NavigationDrawerFragment) },
            ),
            ProfileMenuItem(
                label = "清空我的收藏",
                iconResId = R.drawable.ic_action_warning,
                onClick = { viewModel.showClearFavoriteBoards(requireContext()) },
            ),
            ProfileMenuItem(
                label = "最近被喷",
                iconResId = R.drawable.ic_action_gun,
                extra = extra,
                onClick = { viewModel.startNotificationActivity(requireActivity()) },
            ),
        )

        val secondSection = arrayListOf(
            ProfileMenuItem(
                label = "我的主题",
                iconResId = R.drawable.ic_action_gun,
                onClick = { viewModel.startPostPage(requireContext(), false) },
            ),
            ProfileMenuItem(
                label = "我的回复",
                iconResId = R.drawable.ic_action_gun,
                onClick = { viewModel.startPostPage(requireContext(), true) },
            ),
            ProfileMenuItem(
                label = "我的缓存",
                iconResId = R.drawable.ic_action_gun,
                onClick = { viewModel.startCacheTopicPage(requireContext()) },
            ),
            ProfileMenuItem(
                label = "短消息",
                iconResId = R.drawable.ic_action_gun,
                onClick = { viewModel.startMessagePage(requireContext()) },
            ),
            ProfileMenuItem(
                label = "收藏夹",
                iconResId = R.drawable.ic_action_gun,
                onClick = { viewModel.startFavoriteTopicPage(requireContext()) },
            ),
        )

        val thirdSection = arrayListOf(
            ProfileMenuItem(
                label = "设置",
                iconResId = R.drawable.ic_action_about,
                onClick = { viewModel.startSettingsPage(requireActivity()) },
            ),
            ProfileMenuItem(
                label = "数据导入/导出",
                iconResId = R.drawable.ic_file_download,
                onClick = { viewModel.startBackupPage(requireActivity()) },
            ),
            ProfileMenuItem(
                label = "关于",
                iconResId = R.drawable.ic_action_about,
                onClick = { viewModel.startAboutNgaClient(requireActivity()) },
            ),
        )

        return arrayListOf(
            ProfileMenuSection(items = firstSection),
            ProfileMenuSection(items = secondSection),
            ProfileMenuSection(items = thirdSection),
        )
    }

    private fun handleAvatarClick() {
        val userList: List<User> = UserManager.getUserList()
        if (userList.isNotEmpty()) {
            val activeUser: User? = UserManager.getActiveUser()
            if (activeUser != null) {
                viewModel.startProfilePage(requireActivity(), activeUser)
                return
            }
        }
        viewModel.startLoginPage(requireActivity())
    }

    @Composable
    fun NavigationRoot() {
        var selectedIndex by remember { mutableIntStateOf(BottomTabs.HOME_INDEX) }

        ScaffoldApp(
            topAppBarData = if (selectedIndex == BottomTabs.HOME_INDEX) getHomeTopAppBarData() else getProfileTopAppBarData(),
            bottomBar = {
                BottomNavBar(
                    selectedIndex = selectedIndex,
                    onTabSelected = { selectedIndex = it }
                )
            }
        ) {
            when (selectedIndex) {
                BottomTabs.HOME_INDEX -> {
                    HomeTab(forumBoardViewModel = forumBoardViewModel)
                }
                BottomTabs.PROFILE_INDEX -> {
                    ProfileTab(
                        buildSections = { buildProfileMenuSections() },
                        onAvatarClick = { handleAvatarClick() },
                    )
                }
            }
        }
    }

}
