/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ManetCustomBroadcastReceiver extends BroadcastReceiver {

	public static final String TAG = "ManetCustomBroadcastReceiver";
	
	// custom intent
	public static final String START_SERVICE_INTENT = "org.span.service.intent.action.START_SERVICE_ACTION";

	@Override
	public void onReceive(Context context, Intent intent) {
		/*
		 * NOTE: Allow the service to be started by applications with the proper
		 * permissions.
		 */
		if (START_SERVICE_INTENT.equals(intent.getAction())) {
			Log.i(TAG, "Starting service");
			context.startService(new Intent(context, ManetService.class));
		} else {
			Log.e(TAG, "Received unexpected intent " + intent.toString());
		}
	}
}