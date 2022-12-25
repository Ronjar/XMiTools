package com.tianma.tweaks.miui.data.http.entity

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime

class Trip(private var jsonText: String) {

    private val json: JSONObject

    init {
        Log.wtf("OUTPUT", jsonText)
        //json = JSONObject()
        Log.e("com.tianma.tweaks.miui", jsonText)
        json = JSONArray(jsonText).getJSONArray("journeys").getJSONObject(0).getJSONObject("legs")
    }

    fun getScheduleTime(): LocalDateTime{
        val time = json/*.getJSONArray("journeys").getJSONObject(0).getJSONObject("legs")*/.getString("departureTimePlanned")
        return LocalDateTime.parse(time)
    }
    fun getRealTime(): LocalDateTime{
        val time = json/*.getJSONArray("journeys").getJSONObject(0).getJSONObject("legs")*/.getString("departureTimeEstimated")
        return LocalDateTime.parse(time)
    }
    fun getOffset() = Duration.between(getRealTime(), getScheduleTime())

    enum class STATION(val value: String){
        KORNWESTHEIM("5001402"),
        HAUPTBAHNHOF("5006115"),
        UNIVERSITAET("5006008")
    }
}