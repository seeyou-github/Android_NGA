package gov.anzong.androidnga.activity.compose.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme
import gov.anzong.androidnga.R

data class BottomTabItem(
    val label: String,
    val iconResId: Int,
)

object BottomTabs {
    const val HOME_INDEX = 0
    const val PROFILE_INDEX = 1

    val ITEMS = listOf(
        BottomTabItem(label = "主页", iconResId = R.drawable.ic_tab_home),
        BottomTabItem(label = "我的", iconResId = R.drawable.ic_tab_profile),
    )
}

private val BottomBarHeight = 44.dp

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    NavigationBar(
        modifier = Modifier.height(BottomBarHeight),
        containerColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.primary,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars,
    ) {
        BottomTabs.ITEMS.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                alwaysShowLabel = false,
                icon = {
                    Box(modifier = Modifier.size(BottomBarHeight), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.label,
                            modifier = Modifier.fillMaxSize(0.8f),
                        )
                    }
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colors.primary,
                    selectedTextColor = MaterialTheme.colors.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colors.background,
                ),
            )
        }
    }
}
