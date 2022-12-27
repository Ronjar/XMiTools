package com.tianma.tweaks.miui.xp.hook.systemui.vvs

import android.graphics.Color

public interface VVSObserver {
    fun onTripChanged(newInfo: String, color: Int)
}