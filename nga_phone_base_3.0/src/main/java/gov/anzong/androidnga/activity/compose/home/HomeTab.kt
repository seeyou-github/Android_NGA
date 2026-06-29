package gov.anzong.androidnga.activity.compose.home

import androidx.compose.runtime.Composable
import gov.anzong.androidnga.activity.compose.board.ForumBoardView
import gov.anzong.androidnga.activity.compose.board.ForumBoardViewModel

@Composable
fun HomeTab(
    forumBoardViewModel: ForumBoardViewModel,
) {
    ForumBoardView(forumBoardViewModel)
}
