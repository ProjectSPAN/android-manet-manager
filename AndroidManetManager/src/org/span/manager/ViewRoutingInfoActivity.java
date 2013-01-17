/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import java.util.HashSet;
import java.util.TreeSet;

import org.span.R;
import org.span.service.ManetObserver;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.system.ManetConfig;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ViewRoutingInfoActivity extends Activity implements ManetObserver {
	
	public static final String TAG = "ViewRoutingInfoActivity";
	
	private static final int UPDATE_WAIT_TIME_MILLISEC = 1000;
	
	private ManetManagerApp app = null;
	
    private Handler handler = new Handler();
    
    private TextView tvInfo = null;
    private Button btnGetInfo  = null;
    
    private UpdateThread updateThread = null;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
				
		setContentView(R.layout.routinginfoview);
		
		app = (ManetManagerApp)getApplication();
		app.manet.registerObserver(this);
				
		tvInfo = (TextView) findViewById(R.id.tvInfo);
		
	    btnGetInfo  = (Button) findViewById(R.id.btnGetInfo);
	    btnGetInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				app.manet.sendRoutingInfoQuery();
			}
	    });

	    // hide button since we're auto-updating
	    // TODO: give user choice between auto-updating and manual (button press) updating
	    btnGetInfo.setVisibility(View.GONE); 
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		updateThread = new UpdateThread();
		updateThread.start();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		try {
			if (updateThread != null) {
				updateThread.terminate();
				updateThread.join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		app.manet.unregisterObserver(this);
	}
	
	public static void open(Activity parentActivity) {
		Intent it = new Intent("android.intent.action.GET_ROUTING_INFO_ACTION");
		parentActivity.startActivity(it);
	}
	
	public void update(final String info) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				tvInfo.setText(info);
				// app.displayToastMessage("Info Updated");
			}
		});
	}
	
	private class UpdateThread extends Thread {
		
		private boolean alive = true;
		
		@Override
		public void run() {
			
			try {
				while (alive) {
					app.manet.sendRoutingInfoQuery();
					Thread.sleep(UPDATE_WAIT_TIME_MILLISEC);
				}
			} catch (Exception e) {
				if (alive) {
					e.printStackTrace();
				}
			}
		}
		
    	public void terminate() {
    		alive = false;
    		interrupt(); // interrupt if sleeping
    	}
	}
	
	// callback methods

	@Override
	public void onAdhocStateUpdated(AdhocStateEnum arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConfigUpdated(ManetConfig arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPeersUpdated(HashSet<Node> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRoutingInfoUpdated(String info) {
		Log.d(TAG, "onRoutingInfoUpdated()"); // DEBUG
		update(info);
	}

	@Override
	public void onServiceConnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceDisconnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceStarted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceStopped() {
		// TODO Auto-generated method stub
	}
}