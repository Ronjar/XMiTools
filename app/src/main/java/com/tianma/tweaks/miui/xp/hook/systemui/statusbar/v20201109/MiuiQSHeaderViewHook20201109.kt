package com.tianma.tweaks.miui.xp.hook.systemui.statusbar.v20201109

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat.OrientationMode
import androidx.core.content.ContextCompat
import androidx.core.view.marginRight
import androidx.viewpager2.widget.ViewPager2.Orientation
import com.tianma.tweaks.miui.BuildConfig
import com.tianma.tweaks.miui.R
import com.tianma.tweaks.miui.app.VVSSettingsActivity
import com.tianma.tweaks.miui.data.sp.XPrefContainer
import com.tianma.tweaks.miui.utils.ResolutionUtils
import com.tianma.tweaks.miui.utils.get
import com.tianma.tweaks.miui.utils.logD
import com.tianma.tweaks.miui.xp.hook.BaseSubHook
import com.tianma.tweaks.miui.xp.hook.systemui.helper.ResHelpers
import com.tianma.tweaks.miui.xp.hook.systemui.vvs.VVSMonitor
import com.tianma.tweaks.miui.xp.hook.systemui.vvs.VVSObserver
import com.tianma.tweaks.miui.xp.hook.systemui.weather.WeatherObserver
import com.tianma.tweaks.miui.xp.utils.appinfo.AppInfo
import com.tianma.tweaks.miui.xp.wrapper.MethodHookWrapper
import com.tianma.tweaks.miui.xp.wrapper.XposedWrapper

/**
 * 下拉状态栏头部View Hook（下拉状态栏显示天气等)，适用版本 MiuiSystemUI(versionCode>=202011090)
 */
class MiuiQSHeaderViewHook20201109(classLoader: ClassLoader?, appInfo: AppInfo?) :
    BaseSubHook(classLoader, appInfo), WeatherObserver, VVSObserver {

    private var mMiuiQSHeaderViewClass: Class<*>? = null
    private var mMiuiHeaderViewClass: Class<*>? = null

    private var mModContext: Context? = null
    private var mAppContext: Context? = null

    private var mWeatherInfoTextView: TextView? = null
    private var mWeatherInfoIcon: ImageView? = null

    private val mWeatherEnabled: Boolean = XPrefContainer.isDropdownStatusBarWeatherEnabled
    private var mWeatherTextColor = 0
    private var mWeatherTextSize = 20f

    private lateinit var mVVSMonitor: VVSMonitor

    companion object {
        // MIUI quick settings header view
        private const val CLASS_MIUI_QS_HEADER_VIEW = "com.android.systemui.qs.MiuiQSHeaderView"

        // MIUI header view
        private const val CLASS_MIUI_HEADER_VIEW = "com.android.systemui.qs.MiuiHeaderView"
    }

    init {
        if (mWeatherEnabled) {
            mWeatherTextColor = XPrefContainer.dropdownStatusBarWeatherTextColor
            mWeatherTextSize = XPrefContainer.getDropdownStatusBarWeatherTextSize()
        }
    }

    override fun startHook() {
        logD("Hooking MiuiQSHeaderView...")
        if (mWeatherEnabled) {
            mMiuiQSHeaderViewClass =
                XposedWrapper.findClass(CLASS_MIUI_QS_HEADER_VIEW, mClassLoader)
            if (mMiuiQSHeaderViewClass != null) {
                hookConstructor()
                hookOnFinishInflate()
                hookOnAttachedToWindow()
                hookOnDetachedFromWindow()
                hookUpdateLayout()
            }

            mMiuiHeaderViewClass = XposedWrapper.findClass(CLASS_MIUI_HEADER_VIEW, mClassLoader)
            if (mMiuiHeaderViewClass != null) {
                //hookShowUnimportantNotifications()
                //hookShowNotificationsAnim()
            }
        }
    }

    // MiuiQSHeaderView#constructor()
    private fun hookConstructor() {
        XposedWrapper.hookAllConstructors(mMiuiQSHeaderViewClass,
            object : MethodHookWrapper() {
                override fun after(param: MethodHookParam) {
                    if (mAppContext == null) {
                        val headerView = param.thisObject as View
                        mModContext = headerView.context.applicationContext
                        try {
                            mAppContext = mModContext?.createPackageContext(
                                BuildConfig.APPLICATION_ID,
                                Context.CONTEXT_IGNORE_SECURITY
                            )
                        } catch (e: PackageManager.NameNotFoundException) {
                            // ignore
                        }
                    }
                }
            })
    }

    // MiuiQSHeaderView#onFinishInflate()
    private fun hookOnFinishInflate() {
        XposedWrapper.findAndHookMethod(mMiuiQSHeaderViewClass,
            "onFinishInflate",
            object : MethodHookWrapper() {
                override fun after(param: MethodHookParam) {
                    // MiuiQSHeaderView extends RelativeLayout
                    val miuiQSHeaderView = param.thisObject as ViewGroup

                    // 右上角齿轮快捷按钮
                    val context = miuiQSHeaderView.context
                    val shortcutViewId =
                        ResHelpers.getId(context.resources, "notification_shade_shortcut")
                    val shortcutView = miuiQSHeaderView.findViewById<View>(shortcutViewId)

                    val weatherInfoLp = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).also {
                        it.addRule(RelativeLayout.START_OF, shortcutView.id)
                        it.addRule(RelativeLayout.ALIGN_TOP, shortcutView.id)
                        it.addRule(RelativeLayout.ALIGN_BOTTOM, shortcutView.id)
                        it.marginEnd =
                            ResolutionUtils.dp2px(miuiQSHeaderView.context, 3f).toInt()
                    }

                    mWeatherInfoIcon = ImageView(miuiQSHeaderView.context).also {
                        it.background = mAppContext?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_train) }
                        it.setPadding(0, 0, 20, 0)
                    }

                    mWeatherInfoTextView = TextView(miuiQSHeaderView.context).also {
                        it.text = "Uhrzeit"
                        it.includeFontPadding = false
                        it.gravity = Gravity.CENTER
                        it.setTextColor(mWeatherTextColor)
                        it.setPadding(10,0,0,0)
                        it.textSize = mWeatherTextSize
                        //it.layoutParams = weatherInfoLp
                    }

                    val ll = LinearLayout(miuiQSHeaderView.context).also {
                        it.orientation = LinearLayout.HORIZONTAL
                        it.layoutParams = weatherInfoLp
                        it.addView(mWeatherInfoIcon)
                        it.addView(mWeatherInfoTextView)
                        it.gravity = Gravity.CENTER
                        it.id = R.id.weather_info_text_view
                        it.setOnClickListener { openConfigurator() }
                        it.setOnLongClickListener { refreshData() }
                    }
                    miuiQSHeaderView.addView(ll)
                }
            })
    }

    fun openConfigurator() {
        val notifyIntent = Intent(mAppContext, VVSSettingsActivity::class.java)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        mAppContext?.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        mAppContext!!.startActivity(notifyIntent)
        /*val manager = mAppContext!!.packageManager
        try {
            val i = manager.getLaunchIntentForPackage("com.ronjar.paraparia")
            i?.addCategory(Intent.CATEGORY_LAUNCHER)
            Toast.makeText(mAppContext, i?.getPackage(), Toast.LENGTH_LONG).show()
            mAppContext!!.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(mAppContext, "Sad test", Toast.LENGTH_LONG).show()
        }*/
    }

    fun refreshData(): Boolean{
        mVVSMonitor.refresh()
        Toast.makeText(mAppContext, "Refreshing", Toast.LENGTH_LONG).show()
        return true
    }

    // MiuiQSHeaderView#onAttachedToWindow()
    private fun hookOnAttachedToWindow() {
        XposedWrapper.findAndHookMethod(mMiuiQSHeaderViewClass,
            "onAttachedToWindow",
            object : MethodHookWrapper() {
                override fun after(param: MethodHookParam) {
                    val context = mAppContext ?: return
                    //WeatherMonitor.get(context)
                    //    .registerObserver(this@MiuiQSHeaderViewHook20201109)
                    mVVSMonitor = VVSMonitor(context).also {
                        it.addRefreshListener(this@MiuiQSHeaderViewHook20201109)
                    }
                }
            })
    }

    // MiuiQSHeaderView#onDetachedFromWindow()
    private fun hookOnDetachedFromWindow() {
        XposedWrapper.findAndHookMethod(mMiuiQSHeaderViewClass,
            "onDetachedFromWindow",
            object : MethodHookWrapper() {
                override fun after(param: MethodHookParam) {
                    val context = mAppContext ?: return
                    //WeatherMonitor.get(context)
                    //    .unregisterObserver(this@MiuiQSHeaderViewHook20201109)
                }
            })
    }

    // MiuiQSHeaderView#updateLayout()
    private fun hookUpdateLayout() {
        XposedWrapper.findAndHookMethod(
            mMiuiQSHeaderViewClass,
            "updateLayout",
            object : MethodHookWrapper() {
                override fun after(param: MethodHookParam) {
                    val miuiQSHeaderView = param.thisObject as View
                    logD("MiuiQSHeaderView mLastOrientation = ${miuiQSHeaderView["mLastOrientation"]}")
                    // 当前屏幕方向
                    val orientation = miuiQSHeaderView["mLastOrientation"]
                    if (Configuration.ORIENTATION_PORTRAIT == orientation) {
                        // 竖屏
                        mWeatherInfoTextView?.visibility = View.VISIBLE
                    } else {
                        // 其他情况，隐藏天气信息
                        mWeatherInfoTextView?.visibility = View.GONE
                    }
                }
            })
    }

    // MiuiHeaderView#showUnimportantNotifications()
    private fun hookShowUnimportantNotifications() {
        XposedWrapper.findAndHookMethod(
            mMiuiHeaderViewClass,
            "showUnimportantNotifications",
            object : MethodHookWrapper() {
                override fun after(param: MethodHookParam) {
                    val headerView = param.thisObject as View
                    val weatherInfoTextView =
                        headerView.findViewById<View>(R.id.weather_info_text_view) ?: return

                    weatherInfoTextView.visibility = View.GONE
                }
            })
    }

    private fun hookShowNotificationsAnim() {
        XposedWrapper.findAndHookMethod(
            mMiuiHeaderViewClass,
            "showNotificationsAnim",
            object : MethodHookWrapper() {
                override fun after(param: MethodHookParam) {
                    val headerView = param.thisObject as View
                    val weatherInfoTextView =
                        headerView.findViewById<View>(R.id.weather_info_text_view) ?: return
                    // 显示天气信息
                    headerView.postDelayed({
                        weatherInfoTextView.visibility = View.VISIBLE
                    }, 250L)
                }
            })
    }

    override fun onWeatherChanged(newWeatherInfo: String) {
        logD("onWeatherChanged: %s", newWeatherInfo)
        //mWeatherInfoTextView?.text = newWeatherInfo
    }

    override fun onTripChanged(newInfo: String, color: Int, vehicle: Int) {
        mWeatherInfoTextView?.text = newInfo
        mWeatherInfoTextView?.setTextColor(color)
        mAppContext?.let { mWeatherInfoIcon?.background = ContextCompat.getDrawable(it, vehicle) }
    }
}