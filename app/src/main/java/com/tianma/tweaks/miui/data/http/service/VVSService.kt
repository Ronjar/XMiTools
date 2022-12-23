package com.tianma.tweaks.miui.data.http.service

import com.tianma.tweaks.miui.data.http.entity.Trip
import retrofit2.http.GET
import io.reactivex.Observable
import retrofit2.http.Query

interface VVSService {
    @GET("?outputFormat=rapidJSON&type_destination=any&useRealtime=1&version=10.2.10.139&type_origin=any&calcOneDirection=1&useUT=1&routeType=leasttime&SpEncId=0&language=de")
    fun getNextTrip(@Query("name_origin") from: String, @Query("name_destination") to: String): Observable<Trip?>
}