/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
/**
 *  Portions of this code are copyright (c) 2009 Harald Mueller and Sofia Lemons.
 * 
 *  This program is free software; you can redistribute it and/or modify it under 
 *  the terms of the GNU General Public License as published by the Free Software 
 *  Foundation; either version 3 of the License, or (at your option) any later 
 *  version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with 
 *  this program; if not, see <http://www.gnu.org/licenses/>. 
 *  Use this application at your own risk.
 */
package org.span.manager;

import java.io.File;
import java.io.IOException;

import org.servalproject.SimpleWebServer;
import org.servalproject.system.WiFiRadio;
import org.span.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class ShareActivity extends Activity {
	
	public static final String TAG = "ShareActivity";
	
	public static final String SSID = "AndroidMaster";
	public static final String DEFAULT_AP_ADDRESS = "192.168.43.1";
	public static final int JIBBLE_PORT = 8080;
	
	public static final String INSTRUCTIONS =
			"Webserver is up. Peers should:\n" +
			"1. Join the " + SSID + " network.\n" +
			"2. Browse to " + DEFAULT_AP_ADDRESS + ":" + JIBBLE_PORT + ".\n" +
			"3. Click on packages.\n" +
			"4. Click on the name of the app they want.\n" +
			"5. Open the Downloads app.\n" +
			"6. Click on the app apk file to install it.";

	public static String DATA_FILE_PATH = null;
	
	private ImageView startBtn = null;
	private OnClickListener startBtnListener = null;
	private ImageView stopBtn = null;
	private OnClickListener stopBtnListener = null;
	private TextView tvShareInstructions = null;
	
	private TableRow startTblRow = null;
	private TableRow stopTblRow = null;
	
	private ScaleAnimation animation = null;
	
	private WiFiRadio radio = null;
	
	private SimpleWebServer webServer = null;
	
			
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate()"); // DEBUG
        
        setContentView(R.layout.share);
        
        radio = new WiFiRadio(this);

        // init table rows
        startTblRow = (TableRow)findViewById(R.id.startMasterRow);
        stopTblRow = (TableRow)findViewById(R.id.stopMasterRow);
        
        tvShareInstructions = (TextView)findViewById(R.id.tvShareTnstructions);
        tvShareInstructions.setText(INSTRUCTIONS);

        // define animation
        animation = new ScaleAnimation(
                0.9f, 1, 0.9f, 1, // From x, to x, from y, to y
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(600);
        animation.setFillAfter(true); 
        animation.setStartOffset(0);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);

        // start button
        startBtn = (ImageView) findViewById(R.id.startMasterBtn);
        startBtnListener = new OnClickListener() {
        	@Override
			public void onClick(View v) {
		    	startMasterMode();
			}
		};
		startBtn.setOnClickListener(this.startBtnListener);

		// stop button
		stopBtn = (ImageView) findViewById(R.id.stopMasterBtn);
		stopBtnListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopMasterMode();
			}
		};
		stopBtn.setOnClickListener(this.stopBtnListener);
		
		// make sure all this folders exist, even if empty
		String[] dirs = {"/htdocs", "/htdocs/packages"};
		for (String dirname : dirs) {
			new File(getFilesDir().getParent() + dirname).mkdirs();
		}
		
		showMasterMode(false);
    }
    
    public void onPause() {
    	super.onPause();
    	stopMasterMode();
    }
    
	public static void open(Activity parentActivity) {
		Intent it = new Intent("android.intent.action.SHARE_ACTION");
		parentActivity.startActivity(it);
	}
	
	// TODO: check if currently in ad-hoc mode
	private void startMasterMode() {
		try {
    		radio.startAp(SSID);
    		showMasterMode(true);
    		
    		if (webServer == null) {
    			webServer = new SimpleWebServer(new File(getFilesDir().getParent() + "/htdocs"), JIBBLE_PORT);
			}
    		
    	} catch(IOException e) {
    		showMasterMode(false);
    	}
	}
	
	private void stopMasterMode() {
	    try {
			radio.stopAp();
			showMasterMode(false);
			
			if (webServer != null) {
				webServer.interrupt();
				webServer = null;
			}
    	} catch(IOException e) {
    		showMasterMode(true);
		}
	}
  	
  	private void showMasterMode(boolean enabled) {
		
		if (enabled) {
			startTblRow.setVisibility(View.GONE);
			stopTblRow.setVisibility(View.VISIBLE);
			
			// animation
			if (animation != null) {
				stopBtn.startAnimation(animation);
			}
			
		} else {
			startTblRow.setVisibility(View.VISIBLE);
			stopTblRow.setVisibility(View.GONE);
			
			// animation
			if (animation != null) {
				startBtn.startAnimation(this.animation);
			}
						
		}
  	}
}

