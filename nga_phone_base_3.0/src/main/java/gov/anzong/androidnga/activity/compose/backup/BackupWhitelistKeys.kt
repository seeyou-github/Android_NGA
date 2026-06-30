package gov.anzong.androidnga.activity.compose.backup

object BackupWhitelistKeys {

    val GLOBAL_SETTINGS_KEYS: Set<String> = setOf(
        "download_img_quality_without_wifi",
        "enableNotification",
        "notificationSound",
        "nightmode",
        "refresh_after_post_setting_mode",
        "showSignature",
        "showStatic",
        "showColortxt",
        "showiconmode",
        "adjust_size",
        "material_theme",
        "bottom_tab",
        "left_hand",
        "sort_by_post",
        "pref_user",
        "filter_sub_board",
        "nga_domain",
        "topic_title_size",
        "topic_content_size",
        "avatar_size",
        "emoticon_size",
        "use_solid_color_bg",
        "key_night_mode_follow_system",
        "key_webview_zoom_size",
        "key_preload_board_version",
        "pref_load_avatar_strategy",
        "pref_load_pic_strategy",
        "preference_key_ua",
        "version_major_code",
        "version_mirror_code",
        "version_code",
        "previous_version_code",
        "webview_data_index",
        "check_in_last_time",
    )

    val GLOBAL_SETTINGS_KEYS_EXCLUDED: Set<String> = setOf(
        "search_history_topic",
        "search_history_board",
        "search_history_user",
        "topic_history",
        "reply_count",
        "user_active_index",
        "bookmark_board",
        "key_check_upgrade_state",
        "key_check_upgrade_time",
        "key_clear_cache",
    )
}
