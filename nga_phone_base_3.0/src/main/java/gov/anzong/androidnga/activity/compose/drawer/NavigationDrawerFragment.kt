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
        topAppBarData.optionMenuData = getOptionMenuData()
        return topAppBarData
    }

    private fun getProfileTopAppBarData(): TopAppBarData {
        val topAppBarData = TopAppBarData(title = "我的")
        topAppBarData.navigationIconAction = null
        return topAppBarData
    }

    private fun getOptionMenuData(): List<OptionMenuData> {
        return arrayListOf(
            OptionMenuData(
                title = "搜索用户",
                action = { viewModel.startSearchActivity(requireActivity()) },
                type = OptionMenuData.OPTION_MENU_TYPE_ALWAYS_SHOW,
                icon = R.drawable.btn_ic_search,
            ),
            OptionMenuData(
                title = "我的主题",
                action = { viewModel.startPostPage(requireContext(), false) },
            ),
            OptionMenuData(
                title = "我的回复",
                action = { viewModel.startPostPage(requireContext(), true) },
            ),
            OptionMenuData(
                title = "我的缓存",
                action = { viewModel.startCacheTopicPage(requireContext()) },
            ),
            OptionMenuData(
                title = "短消息",
                action = { viewModel.startMessagePage(requireContext()) },
            ),
            OptionMenuData(
                title = "收藏夹",
                action = { viewModel.startFavoriteTopicPage(requireContext()) },
            ),
            OptionMenuData(
                title = "设置",
                action = { viewModel.startSettingsPage(requireActivity()) },
            ),
        )
    }

    private fun buildProfileMenuItems(): List<ProfileMenuItem> {
        val replyCount = viewModel.replyCount.value
        val extra: String? = if (replyCount != null && replyCount > 0) replyCount.toString() else null

        return arrayListOf(
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
            ProfileMenuItem(
                label = "关于",
                iconResId = R.drawable.ic_action_about,
                onClick = { viewModel.startAboutNgaClient(requireActivity()) },
            ),
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
                        buildMenuItems = { buildProfileMenuItems() },
                        onAvatarClick = { handleAvatarClick() },
                    )
                }
            }
        }
    }

}
