package com.tianma.tweaks.miui.xp.hook.systemui.statusbar.def;

import static com.tianma.tweaks.miui.xp.wrapper.XposedWrapper.findAndHookMethod;
import static com.tianma.tweaks.miui.xp.wrapper.XposedWrapper.findClass;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Debug;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tianma.tweaks.miui.BuildConfig;
import com.tianma.tweaks.miui.data.sp.XPrefContainer;
import com.tianma.tweaks.miui.utils.XLogKt;
import com.tianma.tweaks.miui.utils.rom.MiuiVersion;
import com.tianma.tweaks.miui.xp.hook.BaseSubHook;
import com.tianma.tweaks.miui.xp.hook.systemui.helper.ResHelpers;
import com.tianma.tweaks.miui.xp.hook.systemui.weather.WeatherMonitor;
import com.tianma.tweaks.miui.xp.hook.systemui.weather.WeatherObserver;
import com.tianma.tweaks.miui.xp.wrapper.MethodHookWrapper;
import com.tianma.tweaks.miui.xp.wrapper.XposedWrapper;

/**
 * 下拉状态栏头部View Hook（下拉状态栏显示天气等）
 */
public class HeaderViewHook extends BaseSubHook implements WeatherObserver {

    private static final String CLASS_HEADER_VIEW = "com.android.systemui.statusbar.HeaderView";

    private Class<?> mHeaderViewClass;

    private Context mModContext;
    private Context mAppContext;

    private TextView mWeatherInfoTv;

    private boolean mWeatherEnabled;
    private int mWeatherTextColor;
    private float mWeatherTextSize;

    public HeaderViewHook(ClassLoader classLoader, MiuiVersion miuiVersion) {
        super(classLoader, null, miuiVersion);

        // mWeatherEnabled = XSPUtils.isDropdownStatusBarWeatherEnabled(xsp);
        mWeatherEnabled = XPrefContainer.isDropdownStatusBarWeatherEnabled();
        if (mWeatherEnabled) {
            // mWeatherTextColor = XSPUtils.getDropdownStatusBarWeatherTextColor(xsp);
            mWeatherTextColor = XPrefContainer.getDropdownStatusBarWeatherTextColor();
            // mWeatherTextSize = XSPUtils.getDropdownStatusBarWeatherTextSize(xsp);
            mWeatherTextSize = XPrefContainer.getDropdownStatusBarWeatherTextSize();
        }
    }

    @Override
    public void startHook() {
        XLogKt.logD("Hooking HeaderView...");
        if (mWeatherEnabled) {
            mHeaderViewClass = findClass(CLASS_HEADER_VIEW, getMClassLoader());
            hookConstructor();
            hookOnFinishInflate();
            hookOnAttachedToWindow();
            hookOnDetachedFromWindow();
        }
    }

    // HeaderView#constructor()
    private void hookConstructor() {
        XposedWrapper.hookAllConstructors(mHeaderViewClass,
                new MethodHookWrapper() {
                    @Override
                    protected void after(MethodHookParam param) {
                        if (mAppContext == null) {
                            View headerView = (View) param.thisObject;
                            mModContext = headerView.getContext().getApplicationContext();
                            try {
                                mAppContext = mModContext
                                        .createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
                            } catch (PackageManager.NameNotFoundException e) {
                                // ignore
                            }
                        }
                    }
                });
    }

    // HeaderView#onFinishInflate()
    private void hookOnFinishInflate() {
        findAndHookMethod(mHeaderViewClass,
                "onFinishInflate",
                new MethodHookWrapper() {
                    @Override
                    protected void after(MethodHookParam param) {
                        ViewGroup headerView = (ViewGroup) param.thisObject;
                        LinearLayout weatherContainer = (LinearLayout) headerView.getChildAt(headerView.getChildCount() - 1);
                        weatherContainer.setGravity(Gravity.CENTER_VERTICAL);

                        mWeatherInfoTv = new TextView(headerView.getContext());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        mWeatherInfoTv.setLayoutParams(lp);
                        mWeatherInfoTv.setTextColor(Color.GREEN);
                        mWeatherInfoTv.setTextSize(mWeatherTextSize);
                        mWeatherInfoTv.setOnClickListener(view -> openParaparia());

                        weatherContainer.addView(mWeatherInfoTv, 0);
                        // 右上角齿轮快捷按钮
                        View shortcutView = weatherContainer.findViewById(ResHelpers.getId(headerView.getResources(), "notification_shade_shortcut"));
                        if (shortcutView == null) {
                            shortcutView = weatherContainer.getChildAt(weatherContainer.getChildCount() - 1);
                        }
                        LinearLayout.LayoutParams shortcutLP = (LinearLayout.LayoutParams) shortcutView.getLayoutParams();
                        if (shortcutLP.bottomMargin > 0) {
                            shortcutLP.bottomMargin = 0;
                        }
                    }
                });
    }

    // HeaderView#onAttachedToWindow()
    private void hookOnAttachedToWindow() {
        findAndHookMethod(mHeaderViewClass,
                "onAttachedToWindow",
                new MethodHookWrapper() {
                    @Override
                    protected void after(MethodHookParam param) {
                        WeatherMonitor.get(mAppContext).registerObserver(HeaderViewHook.this);
                    }
                });
    }

    // HeaderView#onDetachedFromWindow()
    private void hookOnDetachedFromWindow() {
        findAndHookMethod(mHeaderViewClass,
                "onDetachedFromWindow",
                new MethodHookWrapper() {
                    @Override
                    protected void after(MethodHookParam param) {
                        WeatherMonitor.get(mAppContext).unregisterObserver(HeaderViewHook.this);
                    }
                });
    }

    public void openParaparia(){
        Log.i("Roebin", "We almost there");
        Toast.makeText(mModContext, "Test1", Toast.LENGTH_LONG).show();
        PackageManager manager = mAppContext.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage("com.ronjar.paraparia");
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            Toast.makeText(mAppContext, i.getPackage(), Toast.LENGTH_LONG).show();
            mAppContext.startActivity(i);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mAppContext, "Sad test", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWeatherChanged(String newWeatherInfo) {
        if (mWeatherInfoTv != null) {
            mWeatherInfoTv.setText(newWeatherInfo);
        }
    }
}
