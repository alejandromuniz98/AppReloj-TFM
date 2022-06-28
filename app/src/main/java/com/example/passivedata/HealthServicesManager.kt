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

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveMonitoringConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class HealthServicesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    healthServicesClient: HealthServicesClient
) {
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
    private val dataTypes = setOf(DataType.DAILY_CALORIES,
        DataType.DAILY_DISTANCE,
        DataType.DAILY_FLOORS,
        DataType.DAILY_STEPS,
        DataType.DISTANCE,
        DataType.FLOORS,
        DataType.HEART_RATE_BPM,
        DataType.STEPS,
        DataType.TOTAL_CALORIES,
        DataType.WALKING_STEPS)
    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = passiveMonitoringClient.capabilities.await()
        return (
                DataType.DAILY_CALORIES in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.DAILY_DISTANCE in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.DAILY_FLOORS in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.DAILY_STEPS in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.DISTANCE in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.FLOORS in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.HEART_RATE_BPM in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.STEPS in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.TOTAL_CALORIES in capabilities.supportedDataTypesPassiveMonitoring &&
                        DataType.WALKING_STEPS in capabilities.supportedDataTypesPassiveMonitoring
                        )
    }

    suspend fun registerForHeartRateData() {
        Log.i(TAG, "Registering for background data.")
        val componentName = ComponentName(context, PassiveDataReceiver::class.java)
        val config = PassiveMonitoringConfig.builder()
            .setDataTypes(dataTypes)
            .setComponentName(componentName)
            .build()
        passiveMonitoringClient.registerDataCallback(config).await()
    }

    suspend fun unregisterForHeartRateData() {
        Log.i(TAG, "Unregistering for background data.")
        passiveMonitoringClient.unregisterDataCallback().await()
    }
}
