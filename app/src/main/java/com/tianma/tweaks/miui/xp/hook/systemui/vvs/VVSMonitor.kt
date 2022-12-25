package com.tianma.tweaks.miui.xp.hook.systemui.vvs

import android.content.Context
import android.widget.Toast
import com.tianma.tweaks.miui.data.http.entity.Trip
import com.tianma.tweaks.miui.data.http.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VVSMonitor(val appContext: Context) {

    lateinit var vvsObserver: VVSObserver

    public fun addRefreshListener(vvsObserver: VVSObserver){
        this.vvsObserver = vvsObserver
    }

    public fun Refresh(){
        Toast.makeText(appContext, "Test1", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.IO).launch {
            val trip = DataRepository.getVVS(Trip.STATION.KORNWESTHEIM, Trip.STATION.UNIVERSITAET, appContext)
            withContext(Dispatchers.Main) {
                vvsObserver.onTripChanged("test")
            }
        }
    }
}