/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.span.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class MessageService extends Service {
	
	public static final int MESSAGE_PORT = 9000;
	public static final int MAX_MESSAGE_LENGTH = 256; // 10;
	
	public static final String MESSAGE_FROM_KEY = "message_from";
	public static final String MESSAGE_CONTENT_KEY = "message_content";
	
	private NotificationManager notifier = null;
	
	private Notification notification = null;
	
	private PendingIntent pendingIntent = null;
	
    // one thread for all activities
    private static Thread msgListenerThread = null;
    
    private int notificationId = 0;
    
    @Override 
    public void onCreate() {
    	// do nothing until prompted by startup activity
    }    
    
    @Override    
    public int onStartCommand(Intent intent, int flags, int startId) {
    	
    	if (msgListenerThread == null) {	
	    	msgListenerThread = new MessageListenerThread();
	    	msgListenerThread.start();
    	}
    	
    	return START_STICKY; // run until explicitly stopped    
	}
    
    @Override    
    public void onDestroy() {        
    	// TODO Auto-generated method stub
	}    
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
    /**     
     * Show a notification while this service is running.     
     */    
    private void showNotification(String tickerStr, Bundle extras) {
    	
    	if (notifier == null) {
    		// get reference to notifier
    		notifier = (NotificationManager)getSystemService(NOTIFICATION_SERVICE); 
    	}
    	
    	// unique notification id
    	notificationId++;
    	
    	// set the icon, ticker text, and timestamp        
    	notification = 
    		new Notification(R.drawable.exclamation, tickerStr, System.currentTimeMillis());
    	  	
    	Intent intent = new Intent(this, ViewMessageActivity.class);
    	if (extras != null) {
    		intent.putExtras(extras);
    	}
    	
    	// NOTE: Use a unique notification id to ensure a new pending intent is created.
    	
    	// pending intent to launch main activity if the user selects notification	  
    	pendingIntent = 
    		PendingIntent.getActivity(this, notificationId, intent, 0);
    	
    	// cancel the notification after it is checked by the user
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	// vibrate
    	// notification.defaults |= Notification.DEFAULT_VIBRATE; // DEBUG

    	// set the info for the views that show in the notification panel    
    	notification.setLatestEventInfo(this, "Wireless AdHoc", tickerStr, pendingIntent);
    	
    	// NOTE: Use a unique notification id, otherwise an existing notification with the same id will be replaced.
    	
    	// send the notification
    	notifier.notify(notificationId, notification);
    }
    
    private class MessageListenerThread extends Thread {
    	
    	public void run() {
    		
    		try {
    			// bind to local machine; will receive broadcasts and directed messages
    			// will most likely bind to 127.0.0.1 (localhost)
    			DatagramSocket socket = new DatagramSocket(MESSAGE_PORT);
    			
    			byte[] buff = new byte[MAX_MESSAGE_LENGTH];
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				
				while (true) {
					try {
						// address Android issue where old packet lengths are erroneously 
						// carried over between packet reuse
						packet.setLength(buff.length); 
						
						socket.receive(packet); // blocking
						
						String msg = new String(packet.getData(), 0, packet.getLength());
						String from = msg.substring(0, msg.indexOf("\n"));
						String content = msg.substring(msg.indexOf("\n")+1);
						
						String tickerStr = "New message";
						
				    	Bundle extras = new Bundle();
				    	extras.putString(MESSAGE_FROM_KEY, from);
				    	extras.putString(MESSAGE_CONTENT_KEY, content);
						
						showNotification(tickerStr, extras);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
}