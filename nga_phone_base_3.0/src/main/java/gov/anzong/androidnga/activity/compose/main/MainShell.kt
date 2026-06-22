package gov.anzong.androidnga.activity.compose.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import gov.anzong.androidnga.activity.compose.board.ForumBoardViewModel
import gov.anzong.androidnga.activity.compose.board.ForumBoardView as BoardTabsView

enum class MainTab { Home, Nodes, Mine }

@Composable
fun MainShell(
    forumBoardViewModel: ForumBoardViewModel,
    onSearchClick: () -> Unit,
    onEditBoardsClick: (() -> Unit)? = null,
    nodesContent: @Composable () -> Unit,
    mineContent: @Composable () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val titles = listOf("主页", "板块", "我的")

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                title = { Text(titles[selectedTab], color = Color.White) },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Outlined.Search, contentDescription = "搜索", tint = Color.White)
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
                BottomNavigationItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "主页") },
                    alwaysShowLabel = false,
                    label = { Text("主页") }
                )
                BottomNavigationItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Outlined.GridView, contentDescription = "板块") },
                    alwaysShowLabel = false,
                    label = { Text("板块") }
                )
                BottomNavigationItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "我的") },
                    alwaysShowLabel = false,
                    label = { Text("我的") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> BoardTabsView(
                    forumBoardViewModel = forumBoardViewModel,
                    onAddBoardClick = onEditBoardsClick,
                )
                1 -> nodesContent()
                2 -> mineContent()
            }
        }
    }
}
