/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ManetBootBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "ManetBootBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		 * NOTE: In general, a service is usually started manually by a "parent" application 
		 * or activity by calling startService(). Although the service's lifetime is not
		 * necessarily bound to that of its "parent", its performance can be affected by that
		 * of its parent and vice-versa. 
		 * 
		 * To avoid potential issues, we start this service at boot time by listening for 
		 * the right intent.
		 */
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			Log.i(TAG, "Starting service");
			context.startService(new Intent(context, ManetService.class));
		} else {
			Log.e(TAG, "Received unexpected intent " + intent.toString());
		}
	}
}