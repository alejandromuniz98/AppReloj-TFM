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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.HrAccuracy
import androidx.health.services.client.data.PassiveMonitoringUpdate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AndroidEntryPoint
class PassiveDataReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PassiveDataRepository

    override fun onReceive(context: Context, intent: Intent) {

        Log.d(TAG, "Received new INFO")
        if (intent?.action != PassiveMonitoringUpdate.ACTION_DATA) {
            return
        }
        val state = PassiveMonitoringUpdate.fromIntent(intent) ?: return

        val dataPoints = state.dataPoints


       runBlocking {
           for(point in dataPoints){
               val type = point.dataType
               if(type==DataType.DAILY_CALORIES){
                   repository.storeLatestDailyCalories(point.value.asDouble())
               }
               if(type==DataType.DAILY_DISTANCE){
                   repository.storeLatestDailyDistance(point.value.asDouble())
               }

               if(type==DataType.DAILY_STEPS){
                   repository.storeLatestDailySteps(point.value.asLong().toDouble())
               }

               if(type==DataType.HEART_RATE_BPM){
                   repository.storeLatestHeartRate(point.value.asDouble())
               }


           }

        }
    }
}
