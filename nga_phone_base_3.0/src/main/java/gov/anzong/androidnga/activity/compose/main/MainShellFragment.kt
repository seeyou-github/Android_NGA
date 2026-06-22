package gov.anzong.androidnga.activity.compose.main

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import com.justwen.androidnga.ui.compose.BaseComposeFragment
import com.justwen.androidnga.ui.compose.widget.TopAppBarData
import com.justwent.androidnga.bu.UserManager
import gov.anzong.androidnga.activity.compose.board.BoardTabEditActivity
import gov.anzong.androidnga.activity.compose.board.ForumBoardViewModel
import gov.anzong.androidnga.activity.compose.mine.MineView
import gov.anzong.androidnga.arouter.ARouterConstants
import sp.phone.util.ARouterUtils

class MainShellFragment : BaseComposeFragment() {

    private val forumBoardViewModel: ForumBoardViewModel by lazy {
        ViewModelProvider(this)[ForumBoardViewModel::class.java]
    }

    override fun getTopAppBarData(): TopAppBarData {
        // MainShell owns its own top bar.
        return TopAppBarData("").also {
            it.navigationIconAction = null
        }
    }

    @Composable
    override fun ContentView() {
        // keep Mine view reactive to account changes
        val userList = UserManager.getUserListLiveData().observeAsState(emptyList())

        MainShell(
            forumBoardViewModel = forumBoardViewModel,
            onSearchClick = {
                ARouterUtils.build(ARouterConstants.ACTIVITY_SEARCH).navigation(requireActivity())
            },
            onEditBoardsClick = {
                startActivity(Intent(requireContext(), BoardTabEditActivity::class.java))
            },
            nodesContent = {
                // Minimal: reuse same board view for now.
                gov.anzong.androidnga.activity.compose.board.ForumBoardView(
                    forumBoardViewModel = forumBoardViewModel,
                )
            },
            mineContent = {
                MineView(
                    hasLogin = userList.value.isNotEmpty(),
                    onLoginClick = {
                        ARouterUtils.build(ARouterConstants.ACTIVITY_LOGIN).navigation(requireActivity())
                    },
                    onOpenAccountManager = {
                        // Use existing account manager fragment hosted by TemplateComposeActivity
                        val intent = Intent(requireContext(), gov.anzong.androidnga.activity.compose.TemplateComposeActivity::class.java)
                        intent.putExtra("fragment", "com.justwent.androidnga.bu.user.UserManagerFragment")
                        startActivity(intent)
                    },
                )
            },
        )
    }
}
