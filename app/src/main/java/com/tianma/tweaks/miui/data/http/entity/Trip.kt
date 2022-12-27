package com.tianma.tweaks.miui.data.http.entity

import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Trip(jsonText: String) {

    val json: JSONObject?

    init {
        json = JSONObject(jsonText).getJSONArray("journeys").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("origin")
    }

    fun getScheduleTime(): LocalDateTime? {
        val timeString = json?.getString("departureTimePlanned")
        return timeString?.toLocalDateTime()?.plusHours(1)
    }
    fun getRealTime(): LocalDateTime? {
        val timeString = json?.getString("departureTimeEstimated")
        return timeString?.toLocalDateTime()?.plusHours(1)
    }
    fun getOffset() = Duration.between(getRealTime(), getScheduleTime())

    val isOnTime = getOffset().isZero

    enum class STATION(val value: String){
        KORNWESTHEIM("5001402"),
        HAUPTBAHNHOF("5006115"),
        UNIVERSITAET("5006008")
    }

    private fun String.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
    }
}