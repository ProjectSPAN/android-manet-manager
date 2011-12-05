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

import java.io.IOException;

import android.R.drawable;
import android.adhoc.manet.system.DeviceConfig;
import android.adhoc.manet.system.ManetConfig;
import android.adhoc.manet.system.ManetConfig.WifiTxpowerEnum;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class SetupPrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
		
	public static final String TAG = "SetupPrefsActivity";
	
	private ManetManagerApp app = null;
	
	private ProgressDialog progressDialog = null;

	private String adhocMode = null;
    private String routingProtocol = null;
    private String wifiSsid = null;
    private String wifiChannel = null;
    private String wifiTxpower = null;
    private String wifiEncKey = null;
    private String wifiSetupMethod = null;
    private String  ipAddress = null;
    
    private String bluetoothDisableWifi = null;
    private String bluetoothDiscoverable = null;
    private String adhocDisableWakelock = null;
    
    private EditTextPreference prefWifiEncKey = null;
    private EditTextPreference prefWifiSsid = null;
    
    private static int ID_DIALOG_RESTARTING = 10;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // init application
        app = (ManetManagerApp)getApplication();
        
        /*
        // init current prefs
        adhocMode =			app.prefs.getString(ManetConfig.ADHOC_MODE_KEY, ManetConfig.ADHOC_MODE_DEFAULT);
        routingProtocol =	app.prefs.getString(ManetConfig.ROUTING_PROTOCOL_KEY, ManetConfig.ROUTING_PROTOCOL_DEFAULT);
        wifiSsid =			app.prefs.getString(ManetConfig.WIFI_ESSID_KEY, ManetConfig.WIFI_ESSID_DEFAULT);
        wifiChannel =		app.prefs.getString(ManetConfig.WIFI_CHANNEL_KEY, ManetConfig.WIFI_CHANNEL_DEFAULT);
        wifiTxpower =		app.prefs.getString(ManetConfig.WIFI_TXPOWER_KEY, ManetConfig.WIFI_TXPOWER_KEY_DEFAULT);
        wifiEncKey =		app.prefs.getString(ManetConfig.WIFI_ENCRYPTION_KEY, ManetConfig.WIFI_ENCRYPTION_KEY_DEFAULT);
        wifiSetupMethod =	app.prefs.getString(ManetConfig.WIFI_SETUP_METHOD_KEY, ManetConfig.WIFI_SETUP_METHOD_DEFAULT);
        ipAddress =			app.prefs.getString(ManetConfig.IP_ADDRESS_KEY, ManetConfig.IP_ADDRESS_DEFAULT);
        
        bluetoothDisableWifi = app.prefs.getString(ManetConfig.BLUETOOTH_DISABLE_WIFI_KEY, 
        	Boolean.toString(ManetConfig.BLUETOOTH_DISABLE_WIFI_DEFAULT));
        bluetoothDiscoverable = app.prefs.getString(ManetConfig.BLUETOOTH_DISCOVERABLE_KEY, 
        	Boolean.toString(ManetConfig.BLUETOOTH_DISCOVERABLE_DEFAULT));
        adhocDisableWakelock =app.prefs.getString(ManetConfig.ADHOC_DISABLE_WAKELOCK_KEY, 
        	Boolean.toString(ManetConfig.ADHOC_DISABLE_WAKELOCK_DEFAULT));
        */
        
        addPreferencesFromResource(R.layout.setupprefsview); 
        
        // setup preferences menu
        setup();
    }
	
    private void setup() {
    	
    	// disable wifi group if using bluetooth
    	PreferenceGroup wifiGroup = (PreferenceGroup)findPreference("wifiprefs");
		boolean bluetoothOn = app.manetcfg.isUsingBluetooth();
		wifiGroup.setEnabled(!bluetoothOn);
    	
		 // disable bluetooth adhoc if not supported by the kernel
        if (false) { // !app.manetcfg.isBluetoothSupported()) { // DEBUG
        	PreferenceGroup btGroup = (PreferenceGroup)findPreference("btprefs");
        	btGroup.setEnabled(false);
        } else {
            // disable "bluetooth discoverable" if not supported
            if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR) {
            	PreferenceGroup btGroup = (PreferenceGroup)findPreference("btprefs");
            	CheckBoxPreference btdiscoverablePreference = (CheckBoxPreference)findPreference("bluetoothdiscoverable");
            	btGroup.removePreference(btdiscoverablePreference);
            }        	
        }
		
    	// disable "transmit power" if not supported
    	ListPreference txpowerPreference = (ListPreference)findPreference("txpowerpref");
        if (!app.manetcfg.isTransmitPowerSupported()) {
        	wifiGroup.removePreference(txpowerPreference);
        } else {
        	txpowerPreference.setEntries(WifiTxpowerEnum.stringValues());
        	txpowerPreference.setEntryValues(WifiTxpowerEnum.stringValues());
        	txpowerPreference.setValueIndex(app.manetcfg.getWifiTxpower().ordinal());
        }
        
       
        
        /*
        // disable "encryption setup method"
        ListPreference encsetupPreference = (ListPreference)findPreference("encsetuppref");
        if (app.manetcfg.getWifiDriver().startsWith("softap") 
        		|| app.manetcfg.getWifiDriver().equals(DeviceConfig.DRIVER_HOSTAP)) {
        	wifiGroup.removePreference(encsetupPreference);
        }
        
        // remove auto channel option if not supported by device
        if (!app.manetcfg.getWifiDriver().startsWith("softap")
        		|| !app.manetcfg.getWifiDriver().equals(DeviceConfig.DRIVER_HOSTAP)) {
        	ListPreference channelpref = (ListPreference)findPreference("channelpref");
        	CharSequence[] entries = channelpref.getEntries();
        	CharSequence[] targetentries = new CharSequence[entries.length-1];
        	for (int i=1;i<entries.length;i++) {
        		targetentries[i-1] = entries[i];
        	}
        	CharSequence[] entryvalues = channelpref.getEntryValues();
        	CharSequence[] targetentryvalues = new CharSequence[entries.length-1];
        	for (int i=1;i<entryvalues.length;i++) {
        		targetentryvalues[i-1] = entryvalues[i];
        	}
        	channelpref.setEntries(targetentries);
        	channelpref.setEntryValues(targetentryvalues);
        }
        
        // SSID validation
        prefWifiSsid = (EditTextPreference)findPreference("ssidpref");
        prefWifiSsid.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        	@Override
        	public boolean onPreferenceChange(Preference preference, Object newValue) {
        		String message = validateWifiSsid(newValue.toString());
        		if(!message.equals("")) {
        			app.displayToastMessage(message);
        			return false;
        		}
        		return true;
        }});

        // encryption key validation
        prefWifiEncKey = (EditTextPreference)findPreference("passphrasepref");
        final int origTextColorWifiEncKey = prefWifiEncKey.getEditText().getCurrentTextColor();

        if (app.manetcfg.getWifiDriver().startsWith("softap")
        		|| app.manetcfg.getWifiDriver().equals(DeviceConfig.DRIVER_HOSTAP)) {
        	Log.d(TAG, "Adding validators for WPA-Encryption.");
        	prefWifiEncKey.setSummary(prefWifiEncKey.getSummary() + " (WPA/WPA2-PSK)");
        	prefWifiEncKey.setDialogMessage(getString(R.string.setup_activity_error_passphrase_info));
        	
	        // encryption key change listener for WPA encryption
        	prefWifiEncKey.getEditText().addTextChangedListener(new TextWatcher() {
        		@Override
	            public void afterTextChanged(Editable s) {
	            	// Nothing
	            }
        		@Override
		        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		        	// Nothing
		        }
        		@Override
		        public void onTextChanged(CharSequence s, int start, int before, int count) {
		        	if (s.length() < 8 || s.length() > 30) {
		        		prefWifiEncKey.getEditText().setTextColor(Color.RED);
		        	}
		        	else {
		        		prefWifiEncKey.getEditText().setTextColor(origTextColorWifiEncKey);
		        	}
		        }
	        });
        	
        	prefWifiEncKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        		@Override
	        	public boolean onPreferenceChange(Preference preference,
						Object newValue) {
		        	String validChars = "ABCDEFGHIJKLMONPQRSTUVWXYZ" +
                      "abcdefghijklmnopqrstuvwxyz" +
                      "0123456789";
	        		if (newValue.toString().length() < 8) {
	        			app.displayToastMessage(getString(R.string.setup_activity_error_passphrase_tooshort));
	        			return false;
	        		} else if (newValue.toString().length() > 30) {
	        			app.displayToastMessage(getString(R.string.setup_activity_error_passphrase_toolong));
	        			return false;	        			
	        		}
	        		for (int i = 0 ; i < newValue.toString().length() ; i++) {
	        			if (!validChars.contains(newValue.toString().substring(i, i+1))) {
	        				app.displayToastMessage(getString(R.string.setup_activity_error_passphrase_invalidchars));
	        				return false;
	        		    }
	        		}
	        		return true;
	        	}
	        });
        } else {
        	Log.d(TAG, "Adding validators for WEP-Encryption.");
        	prefWifiEncKey.setSummary(prefWifiEncKey.getSummary() +" (WEP 128-bit)");
        	prefWifiEncKey.setDialogMessage(getString(R.string.setup_activity_error_passphrase_13chars));
        	
	        // encryption key change listener for WEP encryption
        	prefWifiEncKey.getEditText().addTextChangedListener(new TextWatcher() {
        		@Override
	            public void afterTextChanged(Editable s) {
	            	// Nothing
	            }
        		@Override
		        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		        	// Nothing
		        }
        		@Override
		        public void onTextChanged(CharSequence s, int start, int before, int count) {
		        	if (s.length() == 13) {
		        		prefWifiEncKey.getEditText().setTextColor(origTextColorWifiEncKey);
		        	} else {
		        		prefWifiEncKey.getEditText().setTextColor(Color.RED);
		        	}
		        }
	        });
	        
        	prefWifiEncKey.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        		@Override
	        	public boolean onPreferenceChange(Preference preference, Object newValue) {
        			String validChars = "ABCDEFGHIJKLMONPQRSTUVWXYZ" +
        					"abcdefghijklmnopqrstuvwxyz" +
        					"0123456789";
	        		if(newValue.toString().length() == 13) {
	        			for (int i = 0 ; i < 13 ; i++) {
	        				if (!validChars.contains(newValue.toString().substring(i, i+1))) {
	        					app.displayToastMessage(getString(R.string.setup_activity_error_passphrase_invalidchars));
	        					return false;
	        				}
	        			}
	        			return true;
	        		} else {
	        			app.displayToastMessage(getString(R.string.setup_activity_error_passphrase_tooshort));
	        			return false;
	        		}
	        }});
        }
        */
        

		
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "Calling onResume()");
    	super.onResume();
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "Calling onPause()");
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);   
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if (id == ID_DIALOG_RESTARTING) {
	    	progressDialog = new ProgressDialog(this);
	    	progressDialog.setTitle(getString(R.string.setup_activity_restart_adhoc_title));
	    	progressDialog.setMessage(getString(R.string.setup_activity_restart_adhoc_message));
	    	progressDialog.setIndeterminate(false);
	    	progressDialog.setCancelable(true);
	        return progressDialog;
    	}
    	return null;
    }
    
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Log.d(TAG, "onSharedPreferenceChanged()"); // DEBUG
    	update(sharedPreferences, key);
    }
    
    Handler restartingDialogHandler = new Handler(){
        public void handleMessage(Message msg) {
        	if (msg.what == 0)
        		SetupPrefsActivity.this.showDialog(SetupPrefsActivity.ID_DIALOG_RESTARTING);
        	else
        		SetupPrefsActivity.this.dismissDialog(SetupPrefsActivity.ID_DIALOG_RESTARTING);
        	super.handleMessage(msg);
        	System.gc();
        }
    };
    
   Handler displayToastMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
       		if (msg.obj != null) {
       			app.displayToastMessage((String)msg.obj);
       		}
        	super.handleMessage(msg);
        	System.gc();
        }
    };
    
    // invoked each time a preference is changed
    private void update(final SharedPreferences sharedPreferences, final String key) {
    	
    	new Thread(new Runnable(){
    		@Override
			public void run(){
				Looper.prepare();
			   	String message = null;
			   	
		    	if (key.equals("ssidpref")) {
		    		String newWifissid = sharedPreferences.getString("ssidpref", "AndroidAdhoc");
		    		if (wifiSsid.equals(newWifissid) == false) {
	    				wifiSsid = newWifissid;
	    				/*
	    				message = getString(R.string.setup_activity_info_ssid_changedto) + " '" + newWifissid + "'.";
	    				try {
		    				if (CoreTask.isNatEnabled() && CoreTask.isProcessRunning("bin/dnsmasq")) {
				    			// Show RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(0);
		    					// Restart Adhoc
				    			app.restartAdhoc();
				    			// Dismiss RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(1);
		    				}
	    				} catch (Exception ex) {
	    					message = getString(R.string.setup_activity_error_restart_adhoc);
	    				}
		    			// send Message
		    			Message msg = new Message();
		    			msg.obj = message;
		    			displayToastMessageHandler.sendMessage(msg);
		    			*/
	    				
	    				app.manetcfg.setWifiSsid(wifiSsid);
	    				
	    				app.manet.sendManetConfigUpdateCommand(app.manetcfg);
		    		}
		    	}
		    	/*
		    	else if (key.equals("channelpref")) {
		    		String newChannel = sharedPreferences.getString("channelpref", "1");
		    		if (wifiChannel.equals(newChannel) == false) {
		    			wifiChannel = newChannel;
	    				message = getString(R.string.setup_activity_info_channel_changedto)+" '"+newChannel+"'.";
	    				try{
		    				if (CoreTask.isNatEnabled() && CoreTask.isProcessRunning("bin/dnsmasq")) {
				    			// Show RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(0);
				    			// Restart Adhoc
		    					app.restartAdhoc();
				    			// Dismiss RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(1);
		    				}
	    				}
	    				catch (Exception ex) {
	    					message = getString(R.string.setup_activity_error_restart_adhoc);
	    				}
		    			// Send Message
		    			Message msg = new Message();
		    			msg.obj = message;
		    			SetupPrefsActivity.this.displayToastMessageHandler.sendMessage(msg);
		    		}
		    	}
		    	else if (key.equals("wakelockpref")) {
					try {
						boolean disableWakeLock = sharedPreferences.getBoolean("wakelockpref", true);
						if (CoreTask.isNatEnabled() && CoreTask.isProcessRunning("bin/dnsmasq")) {
							if (disableWakeLock){
								app.releaseWakeLock();
								message = getString(R.string.setup_activity_info_wakelock_disabled);
							} else{
								app.acquireWakeLock();
								message = getString(R.string.setup_activity_info_wakelock_enabled);
							}
						}
					}
					catch (Exception ex) {
						Log.e(TAG, "Can't enable/disable Wake-Lock!");
					}
					
					// Send Message
	    			Message msg = new Message();
	    			msg.obj = message;
	    			SetupPrefsActivity.this.displayToastMessageHandler.sendMessage(msg);
		    	}
		    	else if (key.equals("encpref")) {
		    		boolean enableEncryption = sharedPreferences.getBoolean("encpref", false);
		    		if (enableEncryption != currentEncryptionEnabled) {
			    		// Restarting
						try{
							if (CoreTask.isNatEnabled() && CoreTask.isProcessRunning("bin/dnsmasq")) {
				    			// Show RestartDialog
								SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(0);
				    			// Restart Adhoc
								app.restartAdhoc();
				    			// Dismiss RestartDialog
								SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(1);
							}
						}
						catch (Exception ex) {
						}
						
						currentEncryptionEnabled = enableEncryption;
						
						// Send Message
		    			Message msg = new Message();
		    			msg.obj = message;
		    			SetupPrefsActivity.this.displayToastMessageHandler.sendMessage(msg);
		    		}
		    	}
		    	else if (key.equals("passphrasepref")) {
		    		String passphrase = sharedPreferences.getString("passphrasepref", SetupPrefsActivity.app.DEFAULT_PASSPHRASE);
		    		if (passphrase.equals(wifiEncKey) == false) {
		    			// Restarting
						try{
							if (CoreTask.isNatEnabled() && CoreTask.isProcessRunning("bin/dnsmasq") && application.wpasupplicant.exists()) {
				    			// Show RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(0);
				    			// Restart Adhoc
								app.restartAdhoc();
				    			// Dismiss RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(1);
							}
						}
						catch (Exception ex) {
							Log.e(TAG, "Exception happend while restarting service - Here is what I know: "+ex);
						}
		    			
						message = getString(R.string.setup_activity_info_passphrase_changedto)+" '"+passphrase+"'.";
						wifiEncKey = passphrase;
						
		    			// Send Message
		    			Message msg = new Message();
		    			msg.obj = message;
		    			SetupPrefsActivity.this.displayToastMessageHandler.sendMessage(msg);
		    		}
		    	} else if (key.equals("txpowerpref")) {
		    		String transmitPower = sharedPreferences.getString("txpowerpref", "disabled");
		    		if (transmitPower.equals(SetupPrefsActivity.this.currentTransmitPower) == false) {
		    			// Restarting
						try{
							if (CoreTask.isNatEnabled() && CoreTask.isProcessRunning("bin/dnsmasq")) {
				    			// Show RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(0);
				    			// Restart Adhoc
								SetupPrefsActivity.app.restartAdhoc();
				    			// Dismiss RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(1);
							}
						}
						catch (Exception ex) {
							Log.e(TAG, "Exception happend while restarting service - Here is what I know: "+ex);
						}
		    			
						message = getString(R.string.setup_activity_info_txpower_changedto)+" '"+transmitPower+"'.";
						SetupPrefsActivity.this.currentTransmitPower = transmitPower;
						
		    			// Send Message
		    			Message msg = new Message();
		    			msg.obj = message;
		    			SetupPrefsActivity.this.displayToastMessageHandler.sendMessage(msg);
		    			
			   			// Display Bluetooth-Warning
						boolean shoTxPowerWarning = SetupPrefsActivity.app.settings.getBoolean("txpowerwarningpref", false);
			   			if (shoTxPowerWarning == false && transmitPower.equals("disabled") == false) {
							LayoutInflater li = LayoutInflater.from(SetupPrefsActivity.this);
					        View view = li.inflate(R.layout.txpowerwarningview, null); 
					        new AlertDialog.Builder(SetupPrefsActivity.this)
					        .setTitle(getString(R.string.setup_activity_txpower_warning_title))
					        .setView(view)
					        .setNeutralButton(getString(R.string.setup_activity_txpower_warning_ok), new DialogInterface.OnClickListener() {
					                public void onClick(DialogInterface dialog, int whichButton) {
					                        Log.d(TAG, "Close pressed");
					    		   			SetupPrefsActivity.app.preferenceEditor.putBoolean("txpowerwarningpref", true);
					    		   			SetupPrefsActivity.app.preferenceEditor.commit();
					                }
					        })
					        .show();
			   			}
		    		}
		    	}		    	
		    	else if (key.equals("ipnetworkpref")) {
		    		String ipaddress = sharedPreferences.getString("ippref", SetupPrefsActivity.app.DEFAULT_IPADDRESS);
		    		if (ipaddress.equals(SetupPrefsActivity.this.currentIp) == false) {
		    			// Restarting
						try{
							if (CoreTask.isNatEnabled() && CoreTask.isProcessRunning("bin/dnsmasq")) {
				    			// Show RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(0);
				    			// Restart Adhoc
								SetupPrefsActivity.app.restartAdhoc();
				    			// Dismiss RestartDialog
				    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(1);
							}
							message = getString(R.string.setup_activity_info_ip_changedto)+" '"+ipaddress+"'.";
							SetupPrefsActivity.this.currentIp = ipaddress;
						}
						catch (Exception ex) {
							message = getString(R.string.setup_activity_error_restart_adhoc);
							Log.e(TAG, "Exception happend while restarting service - Here is what I know: "+ex);
						}

		    			// Send Message
		    			Message msg = new Message();
		    			msg.obj = message;
		    			SetupPrefsActivity.this.displayToastMessageHandler.sendMessage(msg);
		    		}
		    	}		    	
		    	else if (key.equals("bluetoothon")) {
		    		Boolean bluetoothOn = sharedPreferences.getBoolean("bluetoothon", false);
		    		Message msg = Message.obtain();
		    		msg.what = bluetoothOn ? 0 : 1;
		    		SetupPrefsActivity.this.setWifiPrefsEnableHandler.sendMessage(msg);
					try{
						if (CoreTask.isNatEnabled() && (CoreTask.isProcessRunning("bin/dnsmasq") || CoreTask.isProcessRunning("bin/pand"))) {
			    			// Show RestartDialog
			    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(0);
			    			
			    			// Restart Adhoc
			    			SetupPrefsActivity.app.restartAdhoc();

			    			// Dismiss RestartDialog
			    			SetupPrefsActivity.this.restartingDialogHandler.sendEmptyMessage(1);
						}
					}
					catch (Exception ex) {
						message = getString(R.string.setup_activity_error_restart_adhoc);
					}

		   			// Display Bluetooth-Warning
					boolean showBtWarning = SetupPrefsActivity.app.settings.getBoolean("btwarningpref", false);
		   			if (showBtWarning == false && bluetoothOn == true) {
						LayoutInflater li = LayoutInflater.from(SetupPrefsActivity.this);
				        View view = li.inflate(R.layout.btwarningview, null); 
				        new AlertDialog.Builder(SetupPrefsActivity.this)
				        .setTitle(getString(R.string.setup_activity_bt_warning_title))
				        .setView(view)
				        .setNeutralButton(getString(R.string.setup_activity_bt_warning_ok), new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface dialog, int whichButton) {
				                        Log.d(TAG, "Close pressed");
				    		   			SetupPrefsActivity.app.preferenceEditor.putBoolean("btwarningpref", true);
				    		   			SetupPrefsActivity.app.preferenceEditor.commit();
				                }
				        })
				        .show();
		   			}
		    	}
		    	else if (key.equals("bluetoothkeepwifi")) {
		    		Boolean bluetoothWifi = sharedPreferences.getBoolean("bluetoothkeepwifi", false);
		    		if (bluetoothWifi) {
		    			SetupPrefsActivity.app.enableWifi();
		    		}
		    	}
		    	*/
		    	Looper.loop();
			}
		}).start();
    }
     
	public String validateWifiSsid(String wifiSsid) {
		String message = "";
		String validChars = "ABCDEFGHIJKLMONPQRSTUVWXYZ"
				+ "abcdefghijklmnopqrstuvwxyz" + "0123456789_.";
		for (int i = 0; i < wifiSsid.length(); i++) {
			if (!validChars.contains(wifiSsid.substring(i, i + 1))) {
				message = getString(R.string.setup_activity_error_ssid_invalidchars);
			}
		}
		if (wifiSsid.equals("")) {
			message = getString(R.string.setup_activity_error_ssid_empty);
		}
		if (message.length() > 0)
			message += getString(R.string.setup_activity_error_ssid_notsaved);
		return message;
	}
}
