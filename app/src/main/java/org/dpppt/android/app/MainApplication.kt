/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.dpppt.android.app.inject.AppComponent
import org.dpppt.android.app.inject.AppModule
import org.dpppt.android.app.inject.DaggerAppComponent
import org.dpppt.android.app.sdk.DP3T
import org.dpppt.android.sdk.TracingStatus
import javax.inject.Inject

class MainApplication : Application() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "contact-channel"
        fun getAppComponent(context: Context): AppComponent {
            val app = context.applicationContext as MainApplication
            return app.appComponent
        }
    }

    private val applicationScope = CoroutineScope(SupervisorJob())
    private lateinit var appComponent: AppComponent

    @Inject
    lateinit var DP3T: DP3T

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        appComponent.inject(this)

        DP3T.init(applicationScope)
        applicationScope.launch {
            DP3T.statusUpdates.collect {
                onStatusUpdate(it)
            }
        }
    }

    private fun onStatusUpdate(status: TracingStatus) {
        val prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notification_shown", false)) {
            if (status.wasContactExposed()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel()
                }
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                var contentIntent: PendingIntent? = null
                if (launchIntent != null) {
                    contentIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.push_exposed_title))
                    .setContentText(getString(R.string.push_exposed_text))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSmallIcon(R.drawable.ic_begegnungen)
                    .setContentIntent(contentIntent)
                    .build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(42, notification)
                prefs.edit().putBoolean("notification_shown", true).apply()
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelName = getString(R.string.app_name)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }
}