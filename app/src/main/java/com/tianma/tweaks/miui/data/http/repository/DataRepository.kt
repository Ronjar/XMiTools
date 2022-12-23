package com.tianma.tweaks.miui.data.http.repository

import com.tianma.tweaks.miui.data.http.APIConst
import com.tianma.tweaks.miui.data.http.entity.Hitokoto
import com.tianma.tweaks.miui.data.http.entity.Poem
import com.tianma.tweaks.miui.data.http.entity.Trip
import com.tianma.tweaks.miui.data.http.service.HitokotoService
import com.tianma.tweaks.miui.data.http.service.PoemService
import com.tianma.tweaks.miui.data.http.service.ServiceGenerator
import com.tianma.tweaks.miui.data.http.service.VVSService
import io.reactivex.Observable

object DataRepository {

    @JvmStatic
    fun getHitokoto(categories: List<String>): Observable<Hitokoto?> {
        val hitokotoService = ServiceGenerator.instance
                .createService(APIConst.HITOKOTO_BASE_URL, HitokotoService::class.java)
        return hitokotoService.getHitokoto(categories)
    }

    @JvmStatic
    fun getPoem(category: String): Observable<Poem?> {
        val poemService = ServiceGenerator.instance
                .createService(APIConst.POEM_BASE_URL, PoemService::class.java)
        return poemService.getPoem(category)
    }

    @JvmStatic
    fun getVVS(from: Trip.STATION, to: Trip.STATION): Observable<Trip?> {
        val vvsService = ServiceGenerator.instance
            .createService(APIConst.VVS_BASE_URL, VVSService::class.java)
        return vvsService.getNextTrip(from.value, to.value)
    }
}