package org.dpppt.android.app.network;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.dpppt.android.app.util.DebugUtils;

public class InfoStatusNetwork {

    public static final String UPDATE_NETWORK_STATUS = "org.dpppt.android.network.UPDATE_STATUS";


    public static boolean isConnect(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null;
    }

    public static IntentFilter getUpdateIntentFilter() {
        return new IntentFilter(UPDATE_NETWORK_STATUS);
    }

    public static void sendUpdateBroadcast(Context context) {
        DebugUtils.logDebug("Send notification");

        Intent intent = new Intent();
        intent.setAction(UPDATE_NETWORK_STATUS);
        context.sendBroadcast(intent);

        DebugUtils.logDebug("Notification sent");

    }

}
