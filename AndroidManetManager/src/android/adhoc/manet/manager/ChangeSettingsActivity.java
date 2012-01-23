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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.DeviceConfig;
import android.adhoc.manet.system.ManetConfig;
import android.adhoc.manet.system.ManetConfig.AdhocModeEnum;
import android.adhoc.manet.system.ManetConfig.WifiChannelEnum;
import android.adhoc.manet.system.ManetConfig.WifiEncryptionSetupMethodEnum;
import android.adhoc.manet.system.ManetConfig.WifiTxpowerEnum;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ChangeSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
		
	public static final String TAG = "ChangeSettingsActivity";
	
	private static int ID_DIALOG_RESTARTING = 10;
	
	private ManetManagerApp app = null;
	
	private ProgressDialog progressDialog = null;
	
	private PreferenceGroup wifiGroupPref = null;
	
	private Handler handler = new Handler();
	
	private Button btnCommit = null;
	private Button btnCancel = null;
    
    private ManetConfig manetcfg = null;
    
    private SharedPreferences sharedPreferences = null;
    
    private boolean setupFlag = false;
    
    private boolean dirtyFlag = false;
    
    // will be called when setting menu option is pressed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // init application
        app = (ManetManagerApp)getApplication();
                
        addPreferencesFromResource(R.layout.settingsview); 
        setContentView(R.layout.settingsviewwrapper);
        
        btnCommit = (Button) findViewById(R.id.btnCommit);
	  	btnCommit.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				app.manet.sendManetConfigUpdateCommand(manetcfg);
				if (app.adhocState == AdhocStateEnum.STARTED) {
					openRestartDialog();
				} else {
					finish();
				}
	  		}
		});
        
        btnCancel = (Button) findViewById(R.id.btnCancel);
	  	btnCancel.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			checkIfDirty();
	  		}
		});
    }
    
    // will be called after initial creation and when backing out of EditIgnoreListActivity
    @Override
    protected void onResume() {
    	Log.d(TAG, "Calling onResume()");
    	super.onResume();

        sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    	
        // copy MANET config in case the app's version of it is updated while the user is making changes
        manetcfg = new ManetConfig(app.manetcfg.toMap());
    	
    	if (setupFlag) {
    		updateConfig(); // update MANET config because we backed out of another preferences activity
    	}
		updateView();
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "Calling onPause()");
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);   
    }
	
    private void updateView() {
    	
    	// wifi group
    	wifiGroupPref = (PreferenceGroup)findPreference("wifiprefs");
		boolean bluetoothOn = manetcfg.isUsingBluetooth();
		wifiGroupPref.setEnabled(!bluetoothOn);
    	
		// wifi encryption
		// NOTE: wifi encryption dependencies are specified in the layout XML
		// CheckBoxPreference wifiEncCheckboxPref = (CheckBoxPreference)findPreference("encpref");
		
		// wifi encryption setup method
        ListPreference wifiEncSetupMethodPref = (ListPreference)findPreference("encsetuppref");
        if (manetcfg.getWifiDriver().startsWith("softap") 
        		|| manetcfg.getWifiDriver().equals(DeviceConfig.DRIVER_HOSTAP)) {
        	if (wifiEncSetupMethodPref != null) {
        		wifiGroupPref.removePreference(wifiEncSetupMethodPref);
        	}
        } else {
        	wifiEncSetupMethodPref.setEntries(WifiEncryptionSetupMethodEnum.descriptionValues());
        	wifiEncSetupMethodPref.setEntryValues(WifiEncryptionSetupMethodEnum.stringValues());
        	wifiEncSetupMethodPref.setValueIndex(manetcfg.getWifiEncryptionSetupMethod().ordinal());
        }
		
        // wifi encryption key
        final EditTextPreference wifiEncKeyEditTextPref = (EditTextPreference)findPreference("passphrasepref");
        final int origTextColorWifiEncKey = wifiEncKeyEditTextPref.getEditText().getCurrentTextColor();
        if (manetcfg.getWifiDriver().startsWith("softap")
        		|| manetcfg.getWifiDriver().equals(DeviceConfig.DRIVER_HOSTAP)) {
        	Validation.setupWpaEncryptionValidators(wifiEncKeyEditTextPref, origTextColorWifiEncKey);
        } else {
        	Validation.setupWepEncryptionValidators(wifiEncKeyEditTextPref, origTextColorWifiEncKey);
        }
        wifiEncKeyEditTextPref.setText(manetcfg.getWifiEncryptionKey());
        
        // wifi SSID
        EditTextPreference wifiSsidEditTextPref = (EditTextPreference)findPreference("ssidpref");
        Validation.setupWifiSsidValidator(wifiSsidEditTextPref);
        wifiSsidEditTextPref.setText(manetcfg.getWifiSsid());
        
        // wifi channel
        ListPreference channelpref = (ListPreference)findPreference("channelpref");
        String[] channelStrValues = WifiChannelEnum.stringValues();
        String[] channelDescValues = WifiChannelEnum.descriptionValues();
        // remove auto channel option if not supported by device
        if (!manetcfg.getWifiDriver().startsWith("softap")
        		|| !manetcfg.getWifiDriver().equals(DeviceConfig.DRIVER_HOSTAP)) {
        	// auto channel option at first index
        	String[] newChannelStrValues = new String[channelStrValues.length-1];
        	String[] newChannelDescValues = new String[channelStrValues.length-1];
        	for (int i = 1; i < channelStrValues.length; i++) {
        		newChannelStrValues[i-1] = channelStrValues[i];
        		newChannelDescValues[i-1] = channelDescValues[i];
        	}
        	channelpref.setEntries(newChannelDescValues);
        	channelpref.setEntryValues(newChannelStrValues);
        	WifiChannelEnum wifiChannel = manetcfg.getWifiChannel();
        	if (wifiChannel == WifiChannelEnum.AUTO) {
        		channelpref.setValueIndex(WifiChannelEnum.CHANNEL_1.ordinal()-1);
        	} else {
        		channelpref.setValueIndex(wifiChannel.ordinal()-1);
        	}
        } else {
        	channelpref.setEntries(channelDescValues);
        	channelpref.setEntryValues(channelStrValues);
        	channelpref.setValueIndex(manetcfg.getWifiChannel().ordinal());
        }
        
        // wifi transmit power
    	ListPreference txpowerPreference = (ListPreference)findPreference("txpowerpref");
        if (!manetcfg.isTransmitPowerSupported()) { // DEBUG
        	if (txpowerPreference != null) {
        		wifiGroupPref.removePreference(txpowerPreference);
        	}
        } else {
        	txpowerPreference.setEntries(WifiTxpowerEnum.descriptionValues());
        	txpowerPreference.setEntryValues(WifiTxpowerEnum.stringValues());
        	txpowerPreference.setValueIndex(manetcfg.getWifiTxpower().ordinal());
        }
        
        // bluetooth group
		// disable bluetooth adhoc if not supported by the kernel
        if (!manetcfg.isBluetoothSupported()) {
        	PreferenceGroup btGroup = (PreferenceGroup)findPreference("btprefs");
        	btGroup.setEnabled(true); // DEBUG // false);
        }
		
		// bluetooth
		// NOTE: bluetooth dependencies are specified in the layout XML
		// CheckBoxPreference bluetoothCheckboxPref = (CheckBoxPreference)findPreference("bluetoothon");
        
        // bluetooth keep wifi
        CheckBoxPreference btKeepWifiCheckBoxPref = (CheckBoxPreference)findPreference("bluetoothkeepwifi");
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR) {
        	PreferenceGroup btGroup = (PreferenceGroup)findPreference("btprefs");
        	if (btKeepWifiCheckBoxPref != null) {
        		btGroup.removePreference(btKeepWifiCheckBoxPref);
        	}
        } else {
        	btKeepWifiCheckBoxPref.setChecked(!manetcfg.isWifiDisabledWhenUsingBluetooth());
        }
        
        // bluetooth discoverable
    	CheckBoxPreference btdiscoverablePreference = (CheckBoxPreference)findPreference("bluetoothdiscoverable");
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR) {
        	PreferenceGroup btGroup = (PreferenceGroup)findPreference("btprefs");
        	if (btdiscoverablePreference != null) {
        		btGroup.removePreference(btdiscoverablePreference);
        	}
        } else {
        	btdiscoverablePreference.setChecked(manetcfg.isBluetoothDiscoverableWhenInAdhocMode());
        }
        
        // ip address
        EditTextPreference ipAddressEditTextPref = (EditTextPreference)findPreference("ippref");
        Validation.setupIpAddressValidator(ipAddressEditTextPref);
        ipAddressEditTextPref.setText(manetcfg.getIpAddress());        
        
        // routing ignore list
        JSONArray array = new JSONArray(manetcfg.getRoutingIgnoreList());
        sharedPreferences.edit().putString("ignorepref", array.toString()).commit();
        
        // wake lock
        
        // battery temperature
        
        setupFlag = true;
    }
    
    // invoked each time a preference is changed
    private void updateConfig() {
    	
    	Map<String,String> oldcfgmap = new TreeMap<String,String>(manetcfg.toMap());
    	
    	Map<String,Object> map = (Map<String, Object>) sharedPreferences.getAll();
    	for (String key : map.keySet()) {
			   	
	    	if (key.equals("ssidpref")) {
	    		String wifiSsid = sharedPreferences.getString("ssidpref", ManetConfig.WIFI_ESSID_DEFAULT);
	    		manetcfg.setWifiSsid(wifiSsid);
	    	}
	    	else if (key.equals("channelpref")) {
	    		String wifiChannel = sharedPreferences.getString("channelpref", 
	    				ManetConfig.WIFI_CHANNEL_DEFAULT.toString());
	    		manetcfg.setWifiChannel(WifiChannelEnum.fromString(wifiChannel));
	    	}
	    	else if (key.equals("encsetuppref")) {
	    		String wifiEncSetupMethod = sharedPreferences.getString("encsetuppref", 
	    				ManetConfig.WIFI_ENCRYPTION_SETUP_METHOD_DEFAULT.toString());
	    		manetcfg.setWifiEncryptionSetupMethod(WifiEncryptionSetupMethodEnum.fromString(wifiEncSetupMethod));
	    	}
	    	else if (key.equals("passphrasepref")) {
	    		String wifiEncKey = sharedPreferences.getString("passphrasepref", 
	    				ManetConfig.WIFI_ENCRYPTION_KEY_DEFAULT);
	    		manetcfg.setWifiEncryptionKey(wifiEncKey);
	    	} 
	    	else if (key.equals("txpowerpref")) {
	    		String wifiTxpower = sharedPreferences.getString("txpowerpref", 
	    				ManetConfig.WIFI_TXPOWER_DEFAULT.toString());
	    		manetcfg.setWifiTxPower(WifiTxpowerEnum.fromString(wifiTxpower));
	    	}
	    	else if (key.equals("ippref")) {
		    	String ipAddress = sharedPreferences.getString("ippref", ManetConfig.IP_ADDRESS_DEFAULT);
		    	manetcfg.setIpAddress(ipAddress);
	    	}
	    	else if (key.equals("bluetoothon")) {
	    		final Boolean bluetoothOn = sharedPreferences.getBoolean("bluetoothon", 
	    				ManetConfig.ADHOC_MODE_DEFAULT == AdhocModeEnum.BLUETOOTH);
	    		if (bluetoothOn) {
	    			manetcfg.setAdhocMode(AdhocModeEnum.BLUETOOTH);
	    		} else {
	    			manetcfg.setAdhocMode(AdhocModeEnum.WIFI);
	    		}
	    	}
	    	else if (key.equals("bluetoothkeepwifi")) {
	    		Boolean btKeepWifi = sharedPreferences.getBoolean("bluetoothkeepwifi", !ManetConfig.BLUETOOTH_DISABLE_WIFI_DEFAULT);
	    		manetcfg.setDisableWifiWhenUsingBluetooth(!btKeepWifi);
	    	}
	    	else if (key.equals("bluetoothdiscoverable")) {
	    		Boolean btDiscoverable = sharedPreferences.getBoolean("bluetoothdiscoverable", ManetConfig.BLUETOOTH_DISCOVERABLE_DEFAULT);
	    		manetcfg.setBlutoothDiscoverableWhenInAdhocMode(btDiscoverable);
	    	}
	    	else if (key.equals("ignorepref")) {
	    		List<String> ignoreList = new ArrayList<String>();
				try {
					JSONArray array = new JSONArray(sharedPreferences.getString("ignorepref", "[]"));
					for (int i = 0 ; i < array.length(); i++){ 
						ignoreList.add(array.get(i).toString());
					} 
				} catch (JSONException e) {
					e.printStackTrace();
				}
				manetcfg.setRoutingIgnoreList(ignoreList);
	    	}
    	}
    	
    	Map<String,String> newcfgmap = manetcfg.toMap();
    	dirtyFlag = !oldcfgmap.equals(newcfgmap);
    }
    
    private void checkIfDirty() {
    	if (dirtyFlag) {
			openConfirmDialog();
		} else {
			finish();
		}
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
    
    @Override
	public void onBackPressed() {
    	checkIfDirty();
    }
    
	private void openConfirmDialog() {
		new AlertDialog.Builder(this)
        	.setTitle("Confirm Settings?")
        	.setMessage("Some settings were modified. Do you wish to confirm those settings? If not, all changes will be lost.")
        	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
    				app.manet.sendManetConfigUpdateCommand(manetcfg);
    				if (app.adhocState == AdhocStateEnum.STARTED) {
    					openRestartDialog();
    				} else {
    					finish();
    				}
                }
        	})
        	.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	finish();
                }
        	})
        	.show();  		
   	}
	
	private void openRestartDialog() {
		new AlertDialog.Builder(this)
        	.setTitle("Restart Ad-Hoc mode?")
        	.setMessage("Some settings were modified. " +
        			"Changes will not take effect until Ad-Hoc mode is restarted. " +
        			"Do you wish to restart it now?")
        	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	app.displayToastMessage("Restarting Ad-Hoc mode. Please wait ...");
    				app.manet.sendRestartAdhocCommand();
    				finish();
                }
        	})
        	.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	finish();
                }
        	})
        	.show();  		
   	}
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Log.d(TAG, "onSharedPreferenceChanged()"); // DEBUG
    	updateConfig();
    }
    
    Handler restartingDialogHandler = new Handler(){
        public void handleMessage(Message msg) {
        	if (msg.what == 0) {
        		showDialog(ID_DIALOG_RESTARTING);
        	} else {
        		dismissDialog(ID_DIALOG_RESTARTING);
        	}
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
}
