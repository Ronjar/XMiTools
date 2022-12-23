package com.tianma.tweaks.miui.data.http.entity

import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime

class Trip(private var json: JSONObject) {

    init {
        json = json.getJSONArray("journeys").getJSONObject(0).getJSONObject("legs")
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