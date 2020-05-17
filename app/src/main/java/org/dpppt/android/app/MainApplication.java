/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.RequiresApi;

import org.dpppt.android.app.network.ScheduleMonitoringStatusNetwork;
import org.dpppt.android.app.notifications.NotificationService;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.internal.util.ProcessUtil;


public class MainApplication extends Application {

	private NotificationService notificationService;

	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	public void onCreate() {
		super.onCreate();

		notificationService = new NotificationService(getApplicationContext());

		if (ProcessUtil.isMainProcess(this)) {
			registerReceiver(broadcastReceiver, DP3T.getUpdateIntentFilter());
			ScheduleMonitoringStatusNetwork.scheduleJob(this);
			DP3T.init(this, new ApplicationInfo("it.noiapp.demo", "https://protetti.app/"));
		}
	}

	@Override
	public void onTerminate() {
		if (ProcessUtil.isMainProcess(this)) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onTerminate();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
			if (!prefs.getBoolean("notification_shown", false)) {
				TracingStatus status = DP3T.getStatus(context);
				if (status.wasContactExposed()) {

					notificationService.pushNotification(
							context.getString(R.string.push_exposed_title),
							context.getString(R.string.push_exposed_text),
							NotificationService.NOTIFICATION_ID
					);
				}
			}
		}
	};

}
