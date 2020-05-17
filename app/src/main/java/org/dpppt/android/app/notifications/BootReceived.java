package org.dpppt.android.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.dpppt.android.app.network.ScheduleMonitoringStatusNetwork;
import org.dpppt.android.app.util.DebugUtils;

public class BootReceived extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        {
            DebugUtils.logDebug("Boot job");

            ScheduleMonitoringStatusNetwork.scheduleJob(context);
        }
    }
}
