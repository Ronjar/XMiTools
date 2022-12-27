package com.tianma.tweaks.miui.xp.hook.systemui.vvs

import android.content.Context
import android.graphics.Color
import android.widget.Toast
import com.tianma.tweaks.miui.data.http.entity.Trip
import com.tianma.tweaks.miui.data.http.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class VVSMonitor(val appContext: Context) {

    lateinit var vvsObserver: VVSObserver

    public fun addRefreshListener(vvsObserver: VVSObserver){
        this.vvsObserver = vvsObserver
    }

    public fun refresh(){
        //Toast.makeText(appContext, "Test123456", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.IO).launch {
            val trip = DataRepository.getVVS(Trip.STATION.KORNWESTHEIM, Trip.STATION.UNIVERSITAET, appContext)
            withContext(Dispatchers.Main) {
                //Toast.makeText(appContext, trip.getRealTime(), Toast.LENGTH_LONG).show()
                val color = if (trip.isOnTime) Color.rgb(124, 255, 158) else Color.rgb(255, 124, 124)
                vvsObserver.onTripChanged(trip.getRealTime()?.format(DateTimeFormatter.ofPattern("HH:mm"))?: "Not working", color)
            }
        }
    }
}