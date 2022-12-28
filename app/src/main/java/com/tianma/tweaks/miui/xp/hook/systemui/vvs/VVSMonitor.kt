package com.tianma.tweaks.miui.xp.hook.systemui.vvs

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.os.postAtTime
import com.tianma.tweaks.miui.R
import com.tianma.tweaks.miui.data.http.entity.Trip
import com.tianma.tweaks.miui.data.http.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class VVSMonitor(val appContext: Context) {

    lateinit var vvsObserver: VVSObserver
    private val mSecondsHandler = Handler(Looper.getMainLooper())


    public fun addRefreshListener(vvsObserver: VVSObserver){
        this.vvsObserver = vvsObserver
    }

    public fun refresh(){
        //Toast.makeText(appContext, "Test123456", Toast.LENGTH_LONG).show()
        CoroutineScope(Dispatchers.IO).launch {
            val trip = DataRepository.getVVS(Trip.STATION.KORNWESTHEIM, Trip.STATION.UNIVERSITAET, appContext)
            withContext(Dispatchers.Main) {
                //Toast.makeText(appContext, trip.getRealTime(), Toast.LENGTH_LONG).show()
                val time = trip.getRealTime()?.format(DateTimeFormatter.ofPattern("HH:mm"))?: "Not working"
                val color = if (trip.isOnTime) Color.rgb(83, 179, 48) else Color.rgb(195, 25, 36)
                val icon = trip.getVehicle()?.drawable?:Resources.ID_NULL
                vvsObserver.onTripChanged(time, color, icon)
                //trip.getRealTime()?.let { mSecondsHandler.postAtTime(mSecondsTicker, getUptime(trip.getRealTime()!!)) }
            }
        }
    }

    private fun getUptime(date: LocalDateTime) =
         SystemClock.uptimeMillis() + Duration.between(date, LocalDateTime.now(ZoneOffset.ofHours(1))).toMillis()

    private val mSecondsTicker: Runnable = kotlinx.coroutines.Runnable {
        //val now = SystemClock.uptimeMillis()
        //val next = now + (1000 - now % 1000)
        //mSecondsHandler.postAtTime( {refresh()}, next)
        refresh()
    }
}