package com.tianma.tweaks.miui.data.http.entity

import com.tianma.tweaks.miui.R
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Trip(jsonText: String) {

    val json: JSONObject?

    init {
        json = JSONObject(jsonText).getJSONArray("journeys").getJSONObject(0).getJSONArray("legs").getJSONObject(0)
    }

    fun getScheduleTime(): LocalDateTime? {
        val timeString = json?.getJSONObject("origin")?.getString("departureTimePlanned")
        return timeString?.toLocalDateTime()?.plusHours(1)
    }
    fun getRealTime(): LocalDateTime? {
        val timeString = json?.getJSONObject("origin")?.getString("departureTimeEstimated")
        return timeString?.toLocalDateTime()?.plusHours(1)
    }
    fun getOffset() = Duration.between(getRealTime(), getScheduleTime())

    val isOnTime = getOffset().isZero

    fun getVehicle():VEHICLE?{
        return VEHICLE from json?.getJSONObject("transportation")?.getJSONObject("product")?.getString("name")
    }

    enum class STATION(val value: String){
        KORNWESTHEIM("5001402"),
        STAMMHEIM("5000100"),
        HAUPTBAHNHOF("5006115"),
        UNIVERSITAET("5006008")
    }
    enum class VEHICLE(val apiName: String, val drawable: Int){
        TRAIN("S-Bahn", R.drawable.ic_train),
        BUS("Bus", R.drawable.ic_bus);
        companion object {
            infix fun from(value: String?): VEHICLE? = VEHICLE.values().firstOrNull { it.apiName == value }
        }
    }

    private fun String.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
    }
}