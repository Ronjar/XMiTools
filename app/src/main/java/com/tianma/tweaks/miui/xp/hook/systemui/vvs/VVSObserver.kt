package com.tianma.tweaks.miui.xp.hook.systemui.vvs

import com.tianma.tweaks.miui.data.http.entity.Trip

public interface VVSObserver {
    fun onTripChanged(newInfo: String, color: Int, vehicle: Int)
}