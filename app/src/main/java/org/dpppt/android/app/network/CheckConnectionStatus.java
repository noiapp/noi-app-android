package org.dpppt.android.app.network;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import org.dpppt.android.app.util.DebugUtils;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.dpppt.android.app.R;
import org.dpppt.android.app.notifications.NotificationService;


public class CheckConnectionStatus extends JobService {

    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;
    private static boolean flagRegisteredCallBack = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean onStartJob(JobParameters job) {
        DebugUtils.logDebug("onStartJob");

        if (flagRegisteredCallBack) {
            DebugUtils.logDebug("onStartJob - callback already registered");
            return true;
        }

        if (!InfoStatusNetwork.isConnect(getApplicationContext())) {
            flagRegisteredCallBack = false;
            ScheduleMonitoringStatusNetwork.scheduleJob(getApplicationContext());
            DebugUtils.logDebug("onStartJob - rescheduled because the connection is down");
            handlerLosNetworkConnection();
            return true;
        }

        registerNetworkCallBack();
        DebugUtils.logDebug("onStartJob - callback registered because the connection is alive");

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void registerNetworkCallBack() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;

        NotificationService notificationService = new NotificationService(getApplicationContext());

        connectivityManager.registerDefaultNetworkCallback(
                networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        Toast.makeText(getApplicationContext(), "Network up", Toast.LENGTH_LONG).show();
                        DebugUtils.logDebug("Network up");
                        InfoStatusNetwork.sendUpdateBroadcast(getApplicationContext());
                        notificationService.cancelNotification(NotificationService.NOTIFICATION_ID);
                    }

                    @Override
                    public void onLost(@NonNull Network network) {

                        if (!InfoStatusNetwork.isConnect(getApplicationContext())) {
                            handlerLosNetworkConnection();
                        }
                    }

                });

        DebugUtils.logDebug("networkCallback registered");
        flagRegisteredCallBack = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handlerLosNetworkConnection() {
        NotificationService notificationService = new NotificationService(getApplicationContext());

        Toast.makeText(getApplicationContext(), "Network down", Toast.LENGTH_LONG).show();
        DebugUtils.logDebug("Network down");

        InfoStatusNetwork.sendUpdateBroadcast(getApplicationContext());

        notificationService.pushNotification(
                getString(R.string.INFO),
                getApplicationContext().getString(R.string.warning_text_network_ko),
                NotificationService.NOTIFICATION_ID
        );
    }


    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

}
