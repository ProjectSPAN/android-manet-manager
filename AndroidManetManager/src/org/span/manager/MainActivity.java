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

import java.util.HashSet;

import org.span.R;
import org.span.service.ManetObserver;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.legal.EulaHelper;
import org.span.service.legal.EulaObserver;
import org.span.service.routing.Node;
import org.span.service.system.CoreTask;
import org.span.service.system.ManetConfig;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements EulaObserver, ManetObserver {
	
	public static final String TAG = "MainActivity";
	
	public static final int MESSAGE_CHECK_LOG 			= 1;
	public static final int MESSAGE_CANT_START_ADHOC 	= 2;
	
	private static int ID_DIALOG_STARTING 	= 0;
	private static int ID_DIALOG_STOPPING 	= 1;
	private static int ID_DIALOG_CONNECTING = 2;
	private static int ID_DIALOG_CONFIG 	= 3;
	
	private static ManetManagerApp app = null;
		
	private ProgressDialog progressDialog = null;

	private ImageView startBtn = null;
	private OnClickListener startBtnListener = null;
	private ImageView stopBtn = null;
	private OnClickListener stopBtnListener = null;
	private ImageView radioModeImage = null;
	private RelativeLayout batteryTemperatureLayout = null;
	private RelativeLayout headerMainLayout = null;
	
	private TextView batteryTemperature = null;
	
	private TableRow startTblRow = null;
	private TableRow stopTblRow = null;
	
	private ScaleAnimation animation = null;
	
	private int currDialogId = -1;
			
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate()"); // DEBUG
        
        setContentView(R.layout.main);
        
        app = (ManetManagerApp)getApplication();
        app.manet.registerObserver(this);

        // init table rows
        startTblRow = (TableRow)findViewById(R.id.startAdhocRow);
        stopTblRow = (TableRow)findViewById(R.id.stopAdhocRow);
        radioModeImage = (ImageView)findViewById(R.id.radioModeImage);
        batteryTemperatureLayout = (RelativeLayout)findViewById(R.id.layoutBatteryTemp);
        headerMainLayout = (RelativeLayout)findViewById(R.id.layoutHeaderMain);
        
        batteryTemperature = (TextView)findViewById(R.id.batteryTempText);

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
        startBtn = (ImageView) findViewById(R.id.startAdhocBtn);
        startBtnListener = new OnClickListener() {
        	@Override
			public void onClick(View v) {
				Log.d(TAG, "StartBtn pressed ...");
		    	showDialog(ID_DIALOG_STARTING);
		    	currDialogId = ID_DIALOG_STARTING;
		    	app.manet.sendStartAdhocCommand();
			}
		};
		startBtn.setOnClickListener(this.startBtnListener);

		// stop button
		stopBtn = (ImageView) findViewById(R.id.stopAdhocBtn);
		stopBtnListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "StopBtn pressed ...");
		    	showDialog(ID_DIALOG_STOPPING);
		    	currDialogId = ID_DIALOG_STOPPING;
		    	app.manet.sendStopAdhocCommand();
			}
		};
		stopBtn.setOnClickListener(this.stopBtnListener);
		
   		// start messenger service so that it runs even if no active activities are bound to it
   		startService(new Intent(this, MessageService.class));
        Intent theIntent = getIntent();
        String action = theIntent.getAction();
        
        String intentData = theIntent.getDataString();
        if (action != null && action.equals(Intent.ACTION_VIEW) ) {
        	Bundle bundle = new Bundle(1);
        	bundle.putString("filepath", intentData);
			showDialog(ID_DIALOG_CONFIG, bundle);
		}
        
        EulaHelper eula = new EulaHelper(this, this);
        eula.showDialog();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	Log.d(TAG, "onStart()"); // DEBUG
    }
    
    @Override
	public void onStop() {
		super.onStop();
    	Log.d(TAG, "onStop()"); // DEBUG
	}

    @Override
	public void onDestroy() {
    	super.onDestroy();
    	Log.d(TAG, "onDestroy()"); // DEBUG
		try {
			unregisterReceiver(this.intentReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}    	
	}

    @Override
	public void onResume() {
		super.onResume();
    	Log.d(TAG, "onResume()"); // DEBUG
				
		// check if the battery temperature should be displayed
		if(app.prefs.getString("batterytemppref", "fahrenheit").equals("disabled") == false) {
	        // create the IntentFilter that will be used to listen
	        // to battery status broadcasts
	        intentFilter = new IntentFilter();
	        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
	        registerReceiver(intentReceiver, intentFilter);
	        batteryTemperatureLayout.setVisibility(View.VISIBLE);
		} else {
			try {
				unregisterReceiver(this.intentReceiver);
			} catch (Exception e) {;}
			batteryTemperatureLayout.setVisibility(View.INVISIBLE);
		}
		
		/*
        Window window = getWindow();
        // window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
        // window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        */
	}
	
	private static final int MENU_CHANGE_SETTINGS 		= 0;
	private static final int MENU_VIEW_LOG 				= 1;
	private static final int MENU_ABOUT 				= 2;
	private static final int MENU_SEND_MESSAGE			= 3;
	private static final int MENU_VIEW_ROUTING_INFO		= 4;
	private static final int MENU_SHARE					= 5;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean supRetVal = super.onCreateOptionsMenu(menu);
    	SubMenu setup = menu.addSubMenu(0, MENU_CHANGE_SETTINGS, 0, getString(R.string.main_activity_settings));
    	setup.setIcon(drawable.ic_menu_preferences);
    	SubMenu about = menu.addSubMenu(0, MENU_ABOUT, 0, getString(R.string.main_activity_about));
    	about.setIcon(drawable.ic_menu_info_details);
    	SubMenu send = menu.addSubMenu(0, MENU_SEND_MESSAGE, 0, getString(R.string.main_activity_send_message));
    	SubMenu info = menu.addSubMenu(0, MENU_VIEW_ROUTING_INFO, 0, getString(R.string.main_activity_view_routing_info));
    	// info.setIcon(drawable.ic_menu_agenda);
    	// SubMenu log = menu.addSubMenu(0, MENU_VIEW_LOG, 0, getString(R.string.main_activity_show_log));
    	SubMenu share = menu.addSubMenu(0, MENU_SHARE, 0, getString(R.string.main_activity_share));
    	return supRetVal;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	boolean supRetVal = super.onOptionsItemSelected(menuItem);
    	switch (menuItem.getItemId()) {
	    	case MENU_CHANGE_SETTINGS :
	    		// TODO: create enums for MANET config fields and set via manager app activity
		        startActivityForResult(new Intent(
		        	MainActivity.this, ChangeSettingsActivity.class), 0);
		        break;
	    	case MENU_ABOUT :
	    		openAboutDialog();
	    		break;
	    	case MENU_VIEW_LOG :
	    		ViewLogActivity.open(this);
	    		break;
	    	case MENU_SEND_MESSAGE :
	    		SendMessageActivity.open(this);
	    		break;
	    	case MENU_VIEW_ROUTING_INFO :
	    		ViewRoutingInfoActivity.open(this);
	    		break;
	    	case MENU_SHARE :
	    		ShareActivity.open(this);
	    		break;
	    }
    	return supRetVal;
    }    

    @Override
    protected Dialog onCreateDialog(int id) {
    	if (id == ID_DIALOG_STARTING) {
	    	progressDialog = new ProgressDialog(this);
	    	progressDialog.setTitle(getString(R.string.main_activity_start));
	    	progressDialog.setMessage(getString(R.string.main_activity_start_summary));
	    	progressDialog.setIndeterminate(false);
	    	progressDialog.setCancelable(true);
	        return progressDialog;
    	} else if (id == ID_DIALOG_STOPPING) {
	    	progressDialog = new ProgressDialog(this);
	    	progressDialog.setTitle(getString(R.string.main_activity_stop));
	    	progressDialog.setMessage(getString(R.string.main_activity_stop_summary));
	    	progressDialog.setIndeterminate(false);
	    	progressDialog.setCancelable(true);
	        return progressDialog;  		
    	} else if (id == ID_DIALOG_CONNECTING) {
    		progressDialog = new ProgressDialog(this);
	    	progressDialog.setTitle(getString(R.string.main_activity_connect));
	    	progressDialog.setMessage(getString(R.string.main_activity_connect_summary));
	    	progressDialog.setIndeterminate(false);
	    	progressDialog.setCancelable(true);
	        return progressDialog;  		
    	} 
    	return null;
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args){
    	Log.d(TAG, "onCreateDialog()"); // DEBUG
    	if (id == ID_DIALOG_STARTING) {
	        return onCreateDialog(id);
    	} else if (id == ID_DIALOG_STOPPING) {
    		return onCreateDialog(id);		
    	} else if (id == ID_DIALOG_CONNECTING) {
    		return onCreateDialog(id);
    	} else if (id == ID_DIALOG_CONFIG) {
    		//Config load dialogue
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		final String filepath = args.getString("filepath");
    		final String filename = filepath.substring(filepath.indexOf(':') + 3);
    		builder.setMessage("Are you sure you want to load this external configuration file?\n" + filepath)
    		       .setCancelable(false)
    		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		               //Load the Configuration
    		        	   String command = "cp " + filename + " /data/data/org.span/conf/manet.conf";
    		        	   System.out.println(command);//debug
    		        	   //CoreTask.runCommand(command);
    		        	   app.manet.sendManetConfigLoadCommand(filename);
    		        	   dialog.cancel();
    		           }
    		       })
    		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                dialog.cancel();
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		return alert;
    	}
    	return null;
    }


    private IntentFilter intentFilter;

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
    	@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            	int temp = (intent.getIntExtra("temperature", 0));
            	int celsius = (int)((temp+5)/10);
            	int fahrenheit = (int)(((temp/10)/0.555)+32+0.5);
            	Log.d(TAG, "Temp ==> "+temp+" -- Celsius ==> "+celsius+" -- Fahrenheit ==> "+fahrenheit);
            	String tempPref = MainActivity.this.app.prefs.getString("batterytemppref", "fahrenheit");
            	if (tempPref.equals("celsius")) {
            		batteryTemperature.setText("" + celsius + getString(R.string.main_activity_temperatureunit_celsius));
            	} else {
            		batteryTemperature.setText("" + fahrenheit + getString(R.string.main_activity_temperatureunit_fahrenheit));
            	}
            }
    	}
    };

    public Handler viewUpdateHandler = new Handler(){
        public void handleMessage(Message msg) {
        	switch(msg.what) {
        	case MESSAGE_CHECK_LOG :
        		Log.d(TAG, "Error detected. Check log.");
        		app.displayToastMessage(getString(R.string.main_activity_start_errors));
        		app.manet.sendAdhocStatusQuery();
            	break;
        	case MESSAGE_CANT_START_ADHOC :
        		Log.d(TAG, "Unable to start ad-hoc mode!");
        		app.displayToastMessage(getString(R.string.main_activity_start_unable));
        		app.manet.sendAdhocStatusQuery();
            	break;
        	default:
        		app.manet.sendAdhocStatusQuery();
        	}
        	super.handleMessage(msg);
        }
   };
   
    /*
   	private void openNoNetfilterDialog() {
		LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.nonetfilterview, null); 
		new AlertDialog.Builder(MainActivity.this)
        .setTitle(getString(R.string.main_activity_nonetfilter))
        .setView(view)
        .setNegativeButton(getString(R.string.main_activity_exit), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "Close pressed");
                        MainActivity.this.finish();
                }
        })
        .setNeutralButton(getString(R.string.main_activity_ignore), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "Override pressed");
                    MainActivity.this.app.displayToastMessage("Ignoring, note that this application will NOT work correctly.");
                }
        })
        .show();
   	}
   	
   	private void openNotRootDialog() {
		LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.norootview, null); 
		new AlertDialog.Builder(MainActivity.this)
        .setTitle(getString(R.string.main_activity_notroot))
        .setView(view)
        .setNegativeButton(getString(R.string.main_activity_exit), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "Exit pressed");
                        MainActivity.this.finish();
                }
        })
        .setNeutralButton(getString(R.string.main_activity_ignore), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "Ignore pressed");
                    MainActivity.this.app.installFiles();
                    MainActivity.this.app.displayToastMessage("Ignoring, note that this application will NOT work correctly.");
                }
        })
        .show();
   	}
    */
   
   	private void openAboutDialog() {
		LayoutInflater li = LayoutInflater.from(this);
        View view = li.inflate(R.layout.aboutview, null); 
        // TextView versionName = (TextView)view.findViewById(R.id.versionName);
        // versionName.setText(this.application.getVersionName());        
		new AlertDialog.Builder(MainActivity.this)
        .setTitle(getString(R.string.main_activity_about))
        .setView(view)
        .setNegativeButton(getString(R.string.main_activity_close), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "Close pressed");
                }
        })
        .show();  		
   	}

  	private void showRadioMode(boolean usingBluetooth) {
  		if (usingBluetooth) {
  			radioModeImage.setImageResource(R.drawable.bluetooth);
  		} else {
  			radioModeImage.setImageResource(R.drawable.wifi);
  		}
  	}
  	
  	private void showAdhocMode(AdhocStateEnum state) {
  		headerMainLayout.setVisibility(View.VISIBLE);
		
		if (state == AdhocStateEnum.STARTED) {
			startTblRow.setVisibility(View.GONE);
			stopTblRow.setVisibility(View.VISIBLE);
			
			// animation
			if (animation != null) {
				stopBtn.startAnimation(animation);
			}
					
			/*
		    // checking, if "wired adhoc" is currently running
		    String adhocMode = CoreTask.getProp("adhoc.mode");
		    String adhocStatus = CoreTask.getProp("adhoc.status");
		    if (adhocStatus.equals("running")) {
		    	if (!(adhocMode.equals("wifi") == true || adhocMode.equals("bt") == true)) {
		    		MainActivity.this.application.displayToastMessage(getString(R.string.main_activity_start_wiredadhoc_running));
		    	}
		    }
		    
		    // checking, if cyanogens usb-adhoc is currently running
		    adhocStatus = CoreTask.getProp("adhocing.enabled");
		    if  (adhocStatus.equals("1")) {
		    	MainActivity.this.application.displayToastMessage(getString(R.string.main_activity_start_usbadhoc_running));
		    }
		    */
		    
			// app.showStartNotification();
			
		} else if (state == AdhocStateEnum.STOPPED) {
			startTblRow.setVisibility(View.VISIBLE);
			stopTblRow.setVisibility(View.GONE);
			
			// animation
			if (animation != null) {
				startBtn.startAnimation(this.animation);
			}
						
		} else { // AdhocStateEnum.UNKNOWN
			startTblRow.setVisibility(View.VISIBLE);
			stopTblRow.setVisibility(View.VISIBLE);
		}
		
 		/*
 		Log.d(TAG, "onAdhocStarted()"); // DEBUG 
 		 
 		new Thread(new Runnable(){
			public void run(){
				MainActivity.this.dismissDialog(MainActivity.ID_DIALOG_STARTING);
				Message message = Message.obtain();
				if (success != true) {
					message.what = MESSAGE_CANT_START_ADHOC;
				} else {
					// make device discoverable if checked
					if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR) {
						boolean bluetoothPref = MainActivity.this.app.settings.getBoolean("bluetoothon", false);
						if (bluetoothPref) {
							boolean bluetoothDiscoverable = MainActivity.this.app.settings.getBoolean("bluetoothdiscoverable", false);
							if (bluetoothDiscoverable) {
								MainActivity.this.makeDiscoverable();
							}
						}
					}
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						// taking a small nap
					}
					String wifiStatus = CoreTask.getProp("adhoc.status");
					if (wifiStatus.equals("running") == false) {
						message.what = MESSAGE_CHECK_LOG;
					}
				}
				MainActivity.this.viewUpdateHandler.sendMessage(message);
			}
		}).start();
		*/
  	}
  	
  	// callback methods
  	
	@Override
	public void onEulaAccepted() {
		// used to be part of onPostCreate()
		// connect to MANET service
        if (!app.manet.isConnectedToService()) {
			showDialog(ID_DIALOG_CONNECTING);
			currDialogId = ID_DIALOG_CONNECTING;
			app.manet.connectToService();
        } else {
    		showAdhocMode(app.adhocState);
    		showRadioMode(app.manetcfg.isUsingBluetooth());
        }
	}
  	
 	@Override
 	public void onServiceConnected() {
 		Log.d(TAG, "onServiceConnected()"); // DEBUG
 		removeDialog();
 		app.manet.sendManetConfigQuery();
 		app.manet.sendAdhocStatusQuery();
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
 	
 	public void removeDialog() {
    	Log.d(TAG, "removeDialog()"); // DEBUG
		if (currDialogId != -1) {
			super.removeDialog(currDialogId);
			currDialogId = -1;
		}
 	}

	@Override
	public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
		Log.d(TAG, "onAdhocStateUpdated()"); // DEBUG
		removeDialog();
		showAdhocMode(state);
		app.displayToastMessage(info);
	}

	@Override
	public void onConfigUpdated(ManetConfig manetcfg) {
		Log.d(TAG, "onConfigUpdated()"); // DEBUG
		showRadioMode(manetcfg.isUsingBluetooth());
	}
	
	@Override
	public void onPeersUpdated(HashSet<Node> peers) {
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

