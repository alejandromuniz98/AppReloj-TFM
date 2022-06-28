/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.passivedata

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.Exception
import java.net.URL
import java.time.LocalDateTime
import javax.inject.Inject


class PassiveDataRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun SendData(NumeroPasos:Double,Pulsaciones:Double,Calorias:Double,Distancia:Double){
            Log.d(TAG, "Sending INFO")
            var client: OkHttpClient = OkHttpClient();
            val mediaType: MediaType? = "application/x-www-form-urlencoded".toMediaType()
            val body = RequestBody.create(
                mediaType,
                "idDispositivo=8b9eba4397c4df00"+
                        "&lecturaNumeroPasos=" + NumeroPasos +
                        "&lecturaPulsaciones="+Pulsaciones +
                        "&lecturaDistancia="+Distancia +
                        "&lecturaCalorias="+Calorias
            )
            val url = URL("https://www.it.uniovi.es/hosesbackend/reloj.php")
            val request = Request.Builder().url(url)
                .addHeader("Authorization", "Basic YWxlamFuZHJvOlRGTXNtc3VuZzIwMjJf")
                .method("POST", body)
                .build()
            val response = client.newCall(request).execute()

    }

    val passiveDataEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PASSIVE_DATA_ENABLED] ?: false
    }

    suspend fun setPassiveDataEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PASSIVE_DATA_ENABLED] = enabled
            Log.d(TAG, "DAILY_CALORIES: "+ prefs[LATEST_DAILY_CALORIES])
            Log.d(TAG, "DAILY DISTANCE: "+ prefs[LATEST_DAILY_DISTANCE])
            Log.d(TAG, "DAILY_STEPS: "+ prefs[LATEST_DAILY_STEPS])
            Log.d(TAG, "HEART_RATE_BPM: "+ prefs[LATEST_HEART_RATE])
            Log.d(TAG, "SUM_PULSACIONES: "+ prefs[SUM_PULSACIONES])
            Log.d(TAG, "NUM: "+ prefs[NUM])
            Log.d(TAG, "UPDATE: "+ prefs[LATEST_UPDATE])
            Log.d(TAG, "SET PASIVE DATA ENABLED")
            prefs[SUM_PULSACIONES]=0.0
            prefs[NUM]=0.0
        }
    }

    suspend fun storeLatestDailyCalories(DailyCalories: Double) {
        dataStore.edit { prefs ->
            prefs[LATEST_DAILY_CALORIES] = DailyCalories
            Log.d(TAG, "DAILY_CALORIES: "+ prefs[LATEST_DAILY_CALORIES])
            Log.d(TAG, "DAILY DISTANCE: "+ prefs[LATEST_DAILY_DISTANCE])
            Log.d(TAG, "DAILY_STEPS: "+ prefs[LATEST_DAILY_STEPS])
            Log.d(TAG, "HEART_RATE_BPM: "+ prefs[LATEST_HEART_RATE])
            Log.d(TAG, "SUM_PULSACIONES: "+ prefs[SUM_PULSACIONES])
            Log.d(TAG, "NUM: "+ prefs[NUM])
            Log.d(TAG, "UPDATE: "+ prefs[LATEST_UPDATE])

            var NumeroPasos=prefs[LATEST_DAILY_STEPS]
            var Pulsaciones=prefs[SUM_PULSACIONES]
            var Distancia=prefs[LATEST_DAILY_DISTANCE]
            var Calorias=prefs[LATEST_DAILY_CALORIES]
            var NumeroPulsaciones=prefs[NUM]
            if(NumeroPasos==null){
                NumeroPasos=0.0
            }
            if(Pulsaciones==null){
                Pulsaciones=0.0
            }
            if(Distancia==null){
                Distancia=0.0
            }
            if(Calorias==null){
                Calorias=0.0
            }
            if(NumeroPulsaciones==null){
                NumeroPulsaciones=0.0
            }

            val current = LocalDateTime.now()
            val latestUpdateString=prefs[LATEST_UPDATE]
            var latestUpdatePlus5=current.minusHours(1)

            try {
                val latestUpdate=LocalDateTime.parse(latestUpdateString)
                latestUpdatePlus5=latestUpdate.plusMinutes(10)
            }catch (e: Exception){}

            if(current>latestUpdatePlus5) {
                try {
                    var PulsacionesMedia=Pulsaciones/NumeroPulsaciones
                    SendData(NumeroPasos, PulsacionesMedia, Calorias, Distancia)
                    prefs[LATEST_UPDATE]=current.toString()
                    prefs[NUM]=0.0
                    prefs[SUM_PULSACIONES]=0.0
                }catch (e: Exception){ }
            }
        }
    }

    val lastestHeartRate: Flow<Double> = dataStore.data.map { prefs ->
        prefs[LATEST_HEART_RATE] ?: 0.0
    }

    suspend fun storeLatestHeartRate(heartRate: Double) {
        dataStore.edit { prefs ->
            if(heartRate!=0.0){
                prefs[LATEST_HEART_RATE]=heartRate
                prefs[SUM_PULSACIONES]=prefs[SUM_PULSACIONES]!!+heartRate
                prefs[NUM]= prefs[NUM]!! +1

                if(prefs[NUM]!! >500){
                    prefs[SUM_PULSACIONES]= prefs[SUM_PULSACIONES]!! / prefs[NUM]!!
                    prefs[NUM]=1.0

                }
            }
        }
    }

    val lastestDailyCalories: Flow<Double> = dataStore.data.map { prefs ->
        prefs[LATEST_DAILY_CALORIES] ?: 0.0
    }

    val lastestDailySteps: Flow<Double> = dataStore.data.map { prefs ->
        prefs[LATEST_DAILY_STEPS] ?: 0.0
    }

    val lastestDailyDistance: Flow<Double> = dataStore.data.map { prefs ->
        prefs[LATEST_DAILY_DISTANCE] ?: 0.0
    }

    val lastestUpdate: Flow<String> = dataStore.data.map { prefs ->
        prefs[LATEST_UPDATE] ?: ""
    }

    suspend fun storeLatestUpdate(Update: String) {
        dataStore.edit { prefs ->
            prefs[LATEST_UPDATE] = Update
        }
    }

    suspend fun storeLatestDailyDistance(DailyDistance: Double) {
        dataStore.edit { prefs ->
            prefs[LATEST_DAILY_DISTANCE] = DailyDistance
        }
    }

    suspend fun storeLatestDailySteps(DailySteps: Double) {
        dataStore.edit { prefs ->
            prefs[LATEST_DAILY_STEPS] = DailySteps
        }
    }


    companion object {
        const val PREFERENCES_FILENAME = "passive_data_prefs"
        private val PASSIVE_DATA_ENABLED = booleanPreferencesKey("passive_data_enabled")
        private val LATEST_DAILY_CALORIES = doublePreferencesKey("latest_daily_calories")
        private val LATEST_DAILY_DISTANCE = doublePreferencesKey("latest_daily_distance")
        private val LATEST_DAILY_STEPS = doublePreferencesKey("latest_daily_steps")
        private val LATEST_HEART_RATE = doublePreferencesKey("latest_heart_rate")
        private val LATEST_UPDATE= stringPreferencesKey("latest_update")
        private val NUM= doublePreferencesKey("NUM")
        private val SUM_PULSACIONES= doublePreferencesKey("MEDIA_PULSACIONES")
    }
}
