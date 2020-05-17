package org.dpppt.android.app.network;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
            handleNetworkDown();
            return true;
        }

        registerNetworkCallBack();
        DebugUtils.logDebug("onStartJob - callback registered because the connection is alive");

        return true;
    }

    private void registerNetworkCallBack() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            handleNetworkUp();
                        }

                        @Override
                        public void onLost(@NonNull Network network) {

                            if (!InfoStatusNetwork.isConnect(getApplicationContext())) {
                                handleNetworkDown();
                            }
                        }

                    });
        } else {
            registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {

                            if (!InfoStatusNetwork.isConnect(getApplicationContext())) {
                                handleNetworkDown();
                                return;
                            }
                            handleNetworkUp();
                        }
                    },
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            );
        }

        flagRegisteredCallBack = true;
        DebugUtils.logDebug("networkCallback registered");
    }

    private void handleNetworkUp() {
        NotificationService notificationService = new NotificationService(getApplicationContext());

        Toast.makeText(getApplicationContext(), "Network up", Toast.LENGTH_LONG).show();
        DebugUtils.logDebug("Network up");
        InfoStatusNetwork.sendUpdateBroadcast(getApplicationContext());
        notificationService.cancelNotification(NotificationService.NOTIFICATION_ID);
    }


    private void handleNetworkDown() {
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
