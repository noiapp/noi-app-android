package org.dpppt.android.app.util;

import android.util.Log;
import org.dpppt.android.app.BuildConfig;

public class DebugUtils {

	public static boolean isDev() {
		return BuildConfig.IS_DEV;
	}

	public static void logDebug(String text)
	{
		Log.d("[Protetti]","-------------> "+ text);
	}

}
