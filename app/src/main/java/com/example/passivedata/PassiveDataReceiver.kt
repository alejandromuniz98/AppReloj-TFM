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
        Log.d(TAG, "Received new data")
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
