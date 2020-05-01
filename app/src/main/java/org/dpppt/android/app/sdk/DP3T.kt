package org.dpppt.android.app.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.dpppt.android.sdk.TracingStatus

interface DP3T {

    fun init(applicationScope: CoroutineScope)

    val statusUpdates: Flow<TracingStatus>

    val lastStatus: TracingStatus

    fun start()

    fun stop()

    suspend fun clearData()

    suspend fun sendIWasExposed(inputBase64: String)
}