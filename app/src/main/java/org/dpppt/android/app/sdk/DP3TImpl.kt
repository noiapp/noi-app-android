package org.dpppt.android.app.sdk

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.dpppt.android.sdk.TracingStatus
import org.dpppt.android.sdk.internal.backend.CallbackListener
import org.dpppt.android.sdk.internal.backend.models.ApplicationInfo
import org.dpppt.android.sdk.internal.backend.models.ExposeeAuthData
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.dpppt.android.sdk.DP3T as RealSDK

class DP3TImpl @Inject constructor(
    private val application: Application
) : DP3T {

    private val channel = ConflatedBroadcastChannel<TracingStatus>()

    companion object {
        private const val APP_ID = "it.noiapp.demo"
        private const val BACKEND_BASE_URL = "https://protetti.app/"
    }

    override fun init(applicationScope: CoroutineScope) {
        RealSDK.init(application, ApplicationInfo(APP_ID, BACKEND_BASE_URL))
        application.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = RealSDK.getStatus(context)
                applicationScope.launch { channel.send(status) }
            }
        }, RealSDK.getUpdateIntentFilter())
    }

    override val statusUpdates: Flow<TracingStatus>
        get() = channel.asFlow()

    override val lastStatus: TracingStatus
        get() = RealSDK.getStatus(application)

    override fun start() = RealSDK.start(application)

    override fun stop() = RealSDK.stop(application)

    override suspend fun clearData() = suspendCancellableCoroutine<Unit> { continuation ->
        RealSDK.clearData(application) {
            continuation.resume(Unit)
        }
    }

    override suspend fun sendIWasExposed(inputBase64: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            // TODO: HARDCODED ONSET DATE - NOT FINAL

            val calendarNow: Calendar = GregorianCalendar()
            calendarNow.add(Calendar.DAY_OF_YEAR, -14)
            RealSDK.sendIWasExposed(
                application,
                Date(calendarNow.timeInMillis),
                ExposeeAuthData(inputBase64),
                object : CallbackListener<Void> {
                    override fun onSuccess(response: Void?) {
                        continuation.resume(Unit)
                    }

                    override fun onError(throwable: Throwable) {
                        continuation.resumeWithException(throwable)
                    }
                })
        }
    }
}