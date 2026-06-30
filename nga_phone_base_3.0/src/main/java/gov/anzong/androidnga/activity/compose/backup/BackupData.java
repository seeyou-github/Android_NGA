package gov.anzong.androidnga.activity.compose.backup;

public class BackupData {

    public int schemaVersion;

    public long exportTime;

    public String appVersionName;

    public int appVersionCode;

    public java.util.Map<String, Object> settings;

    public java.util.List<sp.phone.common.User> filterUsers;

    public java.util.List<gov.anzong.androidnga.activity.compose.filter.FilterKeyword> filterKeywords;

    public java.util.List<sp.phone.common.User> users;

    public int activeUserIndex;

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public BackupData() {
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
        this.exportTime = System.currentTimeMillis();
        this.settings = new java.util.LinkedHashMap<>();
        this.filterUsers = new java.util.ArrayList<>();
        this.filterKeywords = new java.util.ArrayList<>();
        this.users = new java.util.ArrayList<>();
        this.activeUserIndex = 0;
        this.appVersionName = null;
        this.appVersionCode = 0;
    }
}
