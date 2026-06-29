package gov.anzong.androidnga.activity.compose.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.justwent.androidnga.bu.UserManager
import gov.anzong.androidnga.R
import sp.phone.common.User

data class ProfileMenuItem(
    val label: String,
    val iconResId: Int,
    val extra: String? = null,
    val onClick: () -> Unit,
)

@Composable
fun ProfileTab(
    buildMenuItems: () -> List<ProfileMenuItem>,
    onAvatarClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        UserHeaderView(onAvatarClick = onAvatarClick)
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val menuItems = buildMenuItems()
            items(menuItems) { item ->
                ProfileMenuRow(item)
            }
            item {
                val paddingValues = WindowInsets.navigationBars.asPaddingValues()
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = item.iconResId),
            contentDescription = item.label,
            tint = Color.DarkGray,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.size(16.dp))
        androidx.compose.material.Text(
            text = item.label,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        if (!item.extra.isNullOrEmpty()) {
            androidx.compose.material.Text(
                text = item.extra,
                color = Color.Red,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun UserAvatarView(user: User? = null, userCount: Int = 0) {
    val avatarPainter: Painter
    val avatarColorFilter: ColorFilter?
    if (user?.avatarUrl?.isNotEmpty() == true) {
        avatarPainter = rememberAsyncImagePainter(
            model = user.avatarUrl,
            placeholder = painterResource(id = R.drawable.drawerdefaulticon),
        )
        avatarColorFilter = null
    } else {
        avatarPainter = painterResource(id = R.drawable.drawerdefaulticon)
        avatarColorFilter = ColorFilter.tint(Color.White)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            modifier = Modifier
                .size(width = 64.dp, height = 64.dp)
                .clip(shape = RoundedCornerShape(32.dp)),
            painter = avatarPainter,
            colorFilter = avatarColorFilter,
            contentDescription = ""
        )
        val msg: String
        val subMsg: String
        if (userCount > 1) {
            msg = "已登录${userCount}个账户，点击切换"
            subMsg = "当前：${user?.nickName}(${user?.userId})"
        } else if (userCount == 1) {
            msg = "已登录${userCount}个账户"
            subMsg = "当前：${user?.nickName}(${user?.userId})"
        } else {
            msg = "未登录"
            subMsg = "点击登录账号登录"
        }
        androidx.compose.material.Text(
            text = msg,
            modifier = Modifier.padding(top = 8.dp),
            color = Color.White,
            fontSize = 14.sp
        )
        androidx.compose.material.Text(
            text = subMsg,
            maxLines = 2,
            modifier = Modifier.padding(top = 6.dp),
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun UserHeaderView(onAvatarClick: () -> Unit = {}) {
    var activeIndex by remember { mutableIntStateOf(0) }
    val userList = UserManager.getUserListLiveData().observeAsState(emptyList())
    val userCount = userList.value.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(MaterialTheme.colors.primary)
            .clickable { onAvatarClick() }
    ) {
        AnimatedContent(
            targetState = activeIndex,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) { index ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (userCount > 0) {
                        val user = userList.value[index % userCount]
                        UserAvatarView(user, userCount)
                    } else {
                        UserAvatarView()
                    }
                }
            }
        }

        if (userCount > 1) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                IconButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = {
                        activeIndex = UserManager.toggleUser(true)
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_action_next_item),
                        tint = Color.White,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}
