/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.core;

import org.span.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class ManetService extends Service {

	private final static String TAG = "ManetService";
	
	public final static String ACTION_SERVICE_STARTED		= "org.span.service.intent.action.SERVICE_STARTED";
	public final static String ACTION_SERVICE_STOPPED		= "org.span.service.intent.action.SERVICE_STOPPED";
	public final static String ACTION_ADHOC_STATE_UPDATED	= "org.span.service.intent.action.ADHOC_STATE_UPDATED";
	public final static String ACTION_CONFIG_UPDATED		= "org.span.service.intent.action.CONFIG_UPDATED";
	public final static String ACTION_LOG_UPDATED			= "org.span.service.intent.action.LOG_UPDATED";
		
    public static final int COMMAND_REGISTER 	  = 0;    
    public static final int COMMAND_UNREGISTER	  = 1;
    public static final int COMMAND_START_ADHOC	  = 2;
    public static final int COMMAND_STOP_ADHOC	  = 3;
    public static final int COMMAND_RESTART_ADHOC = 4;
    public static final int COMMAND_MANET_CONFIG_UPDATE	= 5;
    public static final int COMMAND_MANET_CONFIG_LOAD = 6;
    
    public static final int QUERY_ADHOC_STATUS	= 10;
    public static final int QUERY_MANET_CONFIG 	= 11;
    public static final int QUERY_PEERS			= 12;
    public static final int QUERY_ROUTING_INFO	= 13;
    
    public static final String STATE_KEY 	= "state";
    public static final String INFO_KEY		= "info";
    public static final String CONFIG_KEY 	= "config";
    public static final String PEERS_KEY 	= "peers";
    public static final String FILE_KEY		= "filename";
    public static final String LOG_KEY		= "log";
    
    public static enum AdhocStateEnum {
    	STARTED, STOPPED, UNKNOWN
    }
    
	// unique id for the notification
	private static final int NOTIFICATION_ID = 0;
    
	private final Messenger receiveMessenger = new Messenger(new IncomingHandler());
	
	// private ArrayList<Messenger> clientMessengers = new ArrayList<Messenger>();
	
	// notification management
	private NotificationManager notificationManager = null;
	
	private Notification notification = null;
	
	private PendingIntent pendingIntent = null;
	
	private ManetServiceHelper helper = null;

	@Override
	public IBinder onBind(Intent intent) {
		return receiveMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()"); // DEBUG
				
		// android.os.Debug.waitForDebugger(); // DEBUG
		
		// notification management
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll(); // in case service was force-killed
				
		helper = ManetServiceHelper.getInstance();
		helper.setService(this);
		helper.setup();
		
		// TODO: After "No longer want ..." eventually kills the service,
		// this method will be called. Gracefully resume operations.
		// Routing protocols executing as a separate binary process will be fine (i.e. OLSR),
		// but protocols running as part of the service (i.e. Simple Proactive Routing) will not.
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()"); // DEBUG
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart"); // DEBUG
	}
	
	// called by the system every time a client explicitly starts the service by calling startService(Intent)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand"); // DEBUG
		return START_STICKY; // run service until explicitly stopped   
	}
	
	public void showNotification(String content) {
		
		int icon;
		if (helper.getAdhocState() == AdhocStateEnum.STARTED) {
			icon = R.drawable.adhoc_on_notification;
		} else {
			icon = R.drawable.adhoc_off_notification;
		}
		
    	if (notification == null || notification.icon != icon) {    		
	    	// set the icon, ticker text, and timestamp        
	    	notification = new Notification(icon, content, System.currentTimeMillis());
	    	
	    	// try to prevent service from being killed with "no longer want";
	    	// this only prolongs the inevitable
	    	startForeground(NOTIFICATION_ID, notification);
	
	    	// pending intent to launch main activity if the user selects notification        
	    	// pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, DummyActivity.class), 0);
	    	
	    	Intent launchIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
	    	launchIntent.setComponent(new ComponentName("org.span", "org.span.manager.MainActivity"));
	    	pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
	    	
	    	// don't allow user to clear notification
	    	notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
    	} else {
    		// set the ticker text
    		notification.tickerText = content;
    	}

    	// set the info for the views that show in the notification panel    
    	notification.setLatestEventInfo(this, "MANET Service", content, pendingIntent);
    	
    	// send the notification        
    	notificationManager.notify(NOTIFICATION_ID, notification);
    }
	
	private class IncomingHandler extends Handler {    
		
		@Override        
		public void handleMessage(Message rxmessage) {    
			switch (rxmessage.what) {
				/*
				case MSG_COMMAND_REGISTER:
					clientMessengers.add(rxmessage.replyTo);                    
					break;            
					
				case MSG_COMMAND_UNREGISTER:
					clientMessengers.remove(rxmessage.replyTo);                    
					break;      
				*/	
				case COMMAND_START_ADHOC:
			        helper.handleStartAdhocCommand(rxmessage);
					break;    
					
				case COMMAND_STOP_ADHOC:
					helper.handleStopAdhocCommand(rxmessage);
					break;
					
				case COMMAND_RESTART_ADHOC:
					helper.handleRestartAdhocCommand(rxmessage);
					break;
					
				case COMMAND_MANET_CONFIG_UPDATE:
					helper.handleManetConfigUpdateCommand(rxmessage);
					break;
					
				case COMMAND_MANET_CONFIG_LOAD:
					helper.handleManetConfigLoadCommand(rxmessage);
					break;

				case QUERY_ADHOC_STATUS:
					helper.handleAdhocStatusQuery(rxmessage);
					break;
					
				case QUERY_MANET_CONFIG:
					helper.handleManetConfigQuery(rxmessage);
					break;
					
				case QUERY_PEERS:
					helper.handlePeersQuery(rxmessage);
					break;
					
				case QUERY_ROUTING_INFO:
					helper.handleRoutingInfoQuery(rxmessage);
					break;
					
				default:                    
					super.handleMessage(rxmessage);
			}
		}
	}
}