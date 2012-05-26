/**
 *  This program is free software; you can redistribute it and/or modify it under 
 *  the terms of the GNU General Public License as published by the Free Software 
 *  Foundation; either version 3 of the License, or (at your option) any later 
 *  version.
 *  You should have received a copy of the GNU General Public License along with 
 *  this program; if not, see <http://www.gnu.org/licenses/>. 
 *  Use this application at your own risk.
 *
 *  Copyright (c) 2009 by Harald Mueller and Sofia Lemons.
 */

package android.adhoc.manet.manager;

import java.util.TreeSet;

import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class ManetManagerApp extends Application implements ManetObserver {

	public static final String TAG = "ManetManagerApp";
	
	// preferences
	public SharedPreferences prefs = null;
	public SharedPreferences.Editor prefEditor = null;
	
    // Notification
	public NotificationManager notificationManager = null;
	private Notification notification = null;
	
	// Intents
	private PendingIntent mainIntent = null;
	
	// MANET helper
	public ManetHelper manet = null;
	
	// MANET config
	public ManetConfig manetcfg = null;
	
	// adhoc state
	public AdhocStateEnum adhocState = null;
	
	// singleton
	private static ManetManagerApp instance = null;
	
	public static ManetManagerApp getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "onCreate()");
		
		// singleton
		instance = this;
        
        // preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        // preference editor
        prefEditor = prefs.edit();
        
        // init notification manager
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    	notification = new Notification(R.drawable.start_notification, getString(R.string.start_notification_line), System.currentTimeMillis());
    	mainIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
    	
        // init MANET helper
		manet = new ManetHelper(this);
		manet.registerObserver(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		
		Log.d(TAG, "onTerminate()");
		
    	// manet.stopAdhoc();
    	manet.disconnectFromService();
		
		// remove all notifications
		notificationManager.cancelAll();
	}
    
	/*
	Handler displayMessageHandler = new Handler(){
        public void handleMessage(Message msg) {
       		if (msg.obj != null) {
       			ManetManagerApp.this.displayToastMessage((String)msg.obj);
       		}
        	super.handleMessage(msg);
        }
    };
    */
    
	public void displayToastMessage(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	public void focusAndshowKeyboard(final View v) {
		v.requestFocus();
		v.postDelayed(new Runnable() {
              @Override
              public void run() {
                  InputMethodManager keyboard = (InputMethodManager)
                  getSystemService(getBaseContext().INPUT_METHOD_SERVICE);
                  keyboard.showSoftInput(v, 0);
              }
          },100);
	}
    
    public int getVersionNumber() {
    	int version = -1;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionCode;
        } catch (Exception e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }
    
    public String getVersionName() {
    	String version = "?";
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (Exception e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }
    
  
	// MANET callback methods
  	
	@Override
	public void onServiceConnected() {
		Log.d(TAG, "onServiceConnected()"); // DEBUG
	}

	@Override
	public void onServiceDisconnected() {
		Log.d(TAG, "onServiceDisconnected()"); // DEBUG
	}

	@Override
	public void onServiceStarted() {
		Log.d(TAG, "onServiceStarted()"); // DEBUG
	}

	@Override
	public void onServiceStopped() {
		Log.d(TAG, "onServiceStopped()"); // DEBUG
	}

	@Override
	public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
		Log.d(TAG, "onAdhocStateUpdated()"); // DEBUG
		adhocState = state;
	}
	
	@Override
	public void onConfigUpdated(ManetConfig manetcfg) {
		Log.d(TAG, "onConfigUpdated()"); // DEBUG
		this.manetcfg = manetcfg;
		
		String device = manetcfg.getDeviceType();
		Log.d(TAG, "device: " + device); // DEBUG
	}

	@Override
	public void onPeersUpdated(TreeSet<String> peers) {
		Log.d(TAG, "onPeersUpdated()"); // DEBUG
	}
	
	@Override
	public void onRoutingInfoUpdated(String info) {
		// Log.d(TAG, "onRoutingInfoUpdated()"); // DEBUG
	}
	
	@Override
	public void onError(String error) {
		Log.d(TAG, "onError()"); // DEBUG
	}
}
