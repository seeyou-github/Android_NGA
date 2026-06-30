package sp.phone.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.base.util.ContextUtils;
import gov.anzong.androidnga.base.util.ThreadUtils;
import gov.anzong.androidnga.common.PreferenceKey;

public class ThemeManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int[] mAppThemes = {
            R.style.AppThemeDayNightBrown_NoActionBar,
            R.style.AppThemeDayNightGreen_NoActionBar,
            R.style.AppThemeDayNightBlack_NoActionBar,
    };

    private int[] mAppThemesActionBar = {
            R.style.AppThemeDayNightBrown,
            R.style.AppThemeDayNightGreen,
            R.style.AppThemeDayNightBlack,
    };

    private int mThemeIndex;

    private boolean mNightMode;

    private Context mContext;

    private WebViewTheme mWebViewTheme;

    private TypedValue mTypedValue = new TypedValue();

    private boolean mNightModeFollowSystem;

    private boolean mThemeDirty;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(PreferenceKey.NIGHT_MODE)) {
            mNightMode = sp.getBoolean(key, false);
            applyDayNight();
        } else if (key.equals(PreferenceKey.MATERIAL_THEME)) {
            mThemeIndex = Integer.parseInt(sp.getString(key, "0"));
        } else if (key.equals(PreferenceKey.KEY_NIGHT_MODE_FOLLOW_SYSTEM)) {
            mNightModeFollowSystem = sp.getBoolean(key, false);
            applyDayNight();
        }
        mWebViewTheme = null;
    }

    @ColorInt
    public int getCustomTextColor(Context context) {
        SharedPreferences prefs = ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE);
        String hex = prefs.getString("theme_text_color", null);
        if (hex != null) {
            try {
                return android.graphics.Color.parseColor(hex);
            } catch (Exception ignored) { }
        }
        return getPrimaryColor(context);
    }

    @ColorInt
    public int getCustomTopBarColor(Context context) {
        SharedPreferences prefs = ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE);
        String hex = prefs.getString("theme_top_bar_color", null);
        if (hex != null) {
            try {
                return android.graphics.Color.parseColor(hex);
            } catch (Exception ignored) { }
        }
        return getPrimaryColor(context);
    }

    @ColorInt
    public int getCustomBackgroundColor(Context context) {
        SharedPreferences prefs = ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE);
        String hex = prefs.getString("theme_bg_color", null);
        if (hex != null) {
            try {
                return android.graphics.Color.parseColor(hex);
            } catch (Exception ignored) { }
        }
        return ContextCompat.getColor(context, R.color.background_color);
    }

    @ColorInt
    public int getCustomAccentColor(Context context) {
        SharedPreferences prefs = ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE);
        String hex = prefs.getString("theme_accent_color", null);
        if (hex != null) {
            try {
                return android.graphics.Color.parseColor(hex);
            } catch (Exception ignored) { }
        }
        return getAccentColor(context);
    }

    public boolean hasCustomTheme() {
        SharedPreferences prefs = ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE);
        return prefs.getString("theme_text_color", null) != null;
    }

    public void markThemeDirty() {
        mThemeDirty = true;
    }

    public boolean consumeThemeDirty() {
        boolean dirty = mThemeDirty;
        mThemeDirty = false;
        return dirty;
    }

    private static class ThemeManagerHolder {

        private static ThemeManager sInstance = new ThemeManager();
    }

    private ThemeManager() {
        mContext = ContextUtils.getContext();
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceKey.PERFERENCE, Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);
        mNightMode = sp.getBoolean(PreferenceKey.NIGHT_MODE, true);
        mThemeIndex = Integer.parseInt(sp.getString(PreferenceKey.MATERIAL_THEME, "1"));
        mNightModeFollowSystem = sp.getBoolean(PreferenceKey.KEY_NIGHT_MODE_FOLLOW_SYSTEM, false);
        applyDayNightDelay(0);
    }

    public static ThemeManager getInstance() {
        return ThemeManagerHolder.sInstance;
    }

    private void applyDayNightDelay(long delay) {
        ThreadUtils.postOnMainThreadDelay(() -> {
            if (mNightModeFollowSystem) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if (mNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }, delay);
    }

    private void applyDayNight() {
        applyDayNightDelay(200);
    }

    public boolean isNightModeFollowSystem() {
        return mNightModeFollowSystem;
    }

    public void initializeWebTheme(Context context) {
        if (mWebViewTheme == null) {
            mWebViewTheme = new WebViewTheme(context);
        }
    }

    public int getForegroundColor() {
        return R.color.foreground_color;
    }

    public int getBackgroundColor() {
        return getBackgroundColor(0);
    }

    public int getBackgroundColor(int position) {
        return position % 2 == 1 ? R.color.background_color2 : R.color.background_color;
    }

    public boolean isNightMode() {
        return mNightModeFollowSystem ? ContextUtils.getResources().getBoolean(R.bool.night_mode) : mNightMode;
    }

    public void setNightMode(boolean isNightMode) {
        SharedPreferences sp = ContextUtils.getSharedPreferences(PreferenceKey.PERFERENCE);
        sp.edit().putBoolean(PreferenceKey.NIGHT_MODE, isNightMode).apply();
    }

    @ColorInt
    public int getPrimaryColor(Context context) {
        if (hasCustomTheme()) {
            return getCustomTopBarColor(context);
        }
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, mTypedValue, true);
        return ContextCompat.getColor(context, mTypedValue.resourceId);
    }

    @ColorInt
    public int getAccentColor(Context context) {
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, mTypedValue, true);
        return ContextCompat.getColor(context, mTypedValue.resourceId);
    }


    @ColorInt
    public int getWebTextColor() {
        return mWebViewTheme.getWebTextColor();
    }

    @ColorInt
    public int getWebQuoteBackgroundColor() {
        return mWebViewTheme.getQuoteBackgroundColor();
    }

    @StyleRes
    public int getTheme(boolean toolbarEnabled) {
        int index = isNightMode() ? 0 : mThemeIndex;
        return toolbarEnabled ? mAppThemes[index] : mAppThemesActionBar[index];
    }

    public int getThemeIndex() {
        return mThemeIndex;
    }

    public void applyAboutTheme(AppCompatActivity activity) {
        activity.setTheme(ThemeConstants.THEME_ACTIVITY_ABOUT[isNightMode() ? 0 : mThemeIndex]);
        activity.getDelegate().setLocalNightMode(isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void applyTheme(Activity activity) {

    }
}
