package org.dpppt.android.app.network;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import org.dpppt.android.app.util.DebugUtils;

public class ScheduleMonitoringStatusNetwork {
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, CheckConnectionStatus.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(1000);
        builder.setOverrideDeadline(1000);

        DebugUtils.logDebug("Schedule job");

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        assert jobScheduler != null;
        jobScheduler.schedule(builder.build());

        DebugUtils.logDebug("Job scheduled");

    }
}
