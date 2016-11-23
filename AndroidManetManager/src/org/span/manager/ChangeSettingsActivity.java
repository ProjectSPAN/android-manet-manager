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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.span.R;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.system.CoreTask;
import org.span.service.system.DeviceConfig;
import org.span.service.system.ManetConfig;
import org.span.service.system.ManetConfig.AdhocModeEnum;
import org.span.service.system.ManetConfig.WifiChannelEnum;
import org.span.service.system.ManetConfig.WifiEncryptionAlgorithmEnum;
import org.span.service.system.ManetConfig.WifiEncryptionSetupMethodEnum;
import org.span.service.system.ManetConfig.WifiTxpowerEnum;

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
	
    private ManetConfig manetcfg = null;
	
	private ProgressDialog progressDialog = null;
	
	private PreferenceGroup wifiGroupPref = null;
	
	private Handler handler = new Handler();
	
	private Button btnCommit = null;
	private Button btnCancel = null;
    
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
    	
    	// user id
        EditTextPreference uidEditTextPref = (EditTextPreference)findPreference("uidpref");
        uidEditTextPref.setText(manetcfg.getUserId());
    	
    	// wifi group
    	wifiGroupPref = (PreferenceGroup)findPreference("wifiprefs");
		boolean bluetoothOn = manetcfg.isUsingBluetooth();
		wifiGroupPref.setEnabled(!bluetoothOn);
    	
		// wifi encryption algorithm
		WifiEncryptionAlgorithmEnum encAlgorithm = manetcfg.getWifiEncryptionAlgorithm();
		ListPreference wifiEncAlgorithmPref = (ListPreference)findPreference("encalgorithmpref");
		wifiEncAlgorithmPref.setEnabled(false); // TODO: disable until tested
		wifiEncAlgorithmPref.setEntries(WifiEncryptionAlgorithmEnum.descriptionValues());
		wifiEncAlgorithmPref.setEntryValues(WifiEncryptionAlgorithmEnum.stringValues());
		wifiEncAlgorithmPref.setValueIndex(encAlgorithm.ordinal());
		
		// wifi encryption setup method
        ListPreference wifiEncSetupMethodPref = (ListPreference)findPreference("encsetuppref");
        wifiEncSetupMethodPref.setEnabled(false); // TODO: disable until tested
        if (encAlgorithm == WifiEncryptionAlgorithmEnum.NONE) {
        	wifiEncSetupMethodPref.setEnabled(false);
        } else {
        	wifiEncSetupMethodPref.setEnabled(true);
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
        }
		
        // wifi encryption password
        final EditTextPreference wifiEncPasswordEditTextPref = (EditTextPreference)findPreference("passwordpref");
        wifiEncPasswordEditTextPref.setEnabled(false); // TODO: disable until tested
        if (encAlgorithm == WifiEncryptionAlgorithmEnum.NONE) {
        	wifiEncPasswordEditTextPref.setEnabled(false);
        } else {
        	wifiEncPasswordEditTextPref.setEnabled(true);
	        final int origTextColorWifiEncKey = wifiEncPasswordEditTextPref.getEditText().getCurrentTextColor();
	        if (manetcfg.getWifiDriver().startsWith("softap")
	        		|| manetcfg.getWifiDriver().equals(DeviceConfig.DRIVER_HOSTAP)) {
	        	Validation.setupWpaEncryptionValidators(wifiEncPasswordEditTextPref, origTextColorWifiEncKey);
	        } else {
	        	Validation.setupWepEncryptionValidators(wifiEncPasswordEditTextPref, origTextColorWifiEncKey);
	        }
	        wifiEncPasswordEditTextPref.setText(manetcfg.getWifiEncryptionPassword());
        }
        
        // wifi SSID
        EditTextPreference wifiSsidEditTextPref = (EditTextPreference)findPreference("ssidpref");
        Validation.setupWifiSsidValidator(wifiSsidEditTextPref);
        wifiSsidEditTextPref.setText(manetcfg.getWifiSsid());
        
        // wifi channel
        ListPreference channelpref = (ListPreference)findPreference("channelpref");
        channelpref.setEnabled(false); // TODO: disable until tested
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
        if (true) { // !manetcfg.isBluetoothSupported() // TODO: disable until tested
        	PreferenceGroup btGroup = (PreferenceGroup)findPreference("btprefs");
        	btGroup.setEnabled(false);
        }
		
		// bluetooth
		// NOTE: bluetooth dependencies are specified in the layout XML
		// CheckBoxPreference bluetoothCheckboxPref = (CheckBoxPreference)findPreference("bluetoothonpref");
        
        // bluetooth keep wifi
        CheckBoxPreference btKeepWifiCheckBoxPref = (CheckBoxPreference)findPreference("bluetoothkeepwifipref");
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.ECLAIR) {
        	PreferenceGroup btGroup = (PreferenceGroup)findPreference("btprefs");
        	if (btKeepWifiCheckBoxPref != null) {
        		btGroup.removePreference(btKeepWifiCheckBoxPref);
        	}
        } else {
        	btKeepWifiCheckBoxPref.setChecked(!manetcfg.isWifiDisabledWhenUsingBluetooth());
        }
        
        // bluetooth discoverable
    	CheckBoxPreference btdiscoverablePreference = (CheckBoxPreference)findPreference("bluetoothdiscoverablepref");
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
        
        // dns server
        EditTextPreference dnsServerEditTextPref = (EditTextPreference)findPreference("dnspref");
        Validation.setupIpAddressValidator(dnsServerEditTextPref);
        dnsServerEditTextPref.setText(manetcfg.getDnsServer());        
        
        // routing protocol
        String currRoutingProtocol = manetcfg.getRoutingProtocol();
    	List<String> routingProtocolList = CoreTask.getRoutingProtocols();
    	String[] routingProtocols = new String[routingProtocolList.size()];
    	routingProtocolList.toArray(routingProtocols);
        
    	ListPreference routingProtocolPreference = (ListPreference)findPreference("routingprotocolpref");
    	routingProtocolPreference.setEntries(routingProtocols);
    	routingProtocolPreference.setEntryValues(routingProtocols);
    	routingProtocolPreference.setValue(currRoutingProtocol);
        
        // routing ignore list
        JSONArray array = new JSONArray(manetcfg.getRoutingIgnoreList());
        sharedPreferences.edit().putString("ignorepref", array.toString()).commit();
                
        // wifi interface
        String currInterface = manetcfg.getWifiInterface();
        String defaultInterface = DeviceConfig.getWifiInterface(manetcfg.getDeviceType());
    	List<String> interfaceList = CoreTask.getNetworkInterfaces();
    	if(!interfaceList.contains(defaultInterface)) {
    		interfaceList.add(defaultInterface);
    	}
    	String[] interfaces = new String[interfaceList.size()];
    	interfaceList.toArray(interfaces);
        
    	ListPreference interfacePreference = (ListPreference)findPreference("interfacepref");
    	interfacePreference.setEntries(interfaces);
    	interfacePreference.setEntryValues(interfaces);
    	
    	if (interfaceList.contains(currInterface)) {
    		interfacePreference.setValue(currInterface);
    	} else {
    		interfacePreference.setValue(defaultInterface);
    		currInterface = defaultInterface;
    	}
        
        // routing gateway
        String currGatewayInterface = manetcfg.getGatewayInterface();
    	interfaceList.remove(currInterface); // remove ad-hoc interface
    	interfaceList.add(0, ManetConfig.GATEWAY_INTERFACE_NONE);
    	interfaces = new String[interfaceList.size()];
    	interfaceList.toArray(interfaces);
        
    	ListPreference gatewayPreference = (ListPreference)findPreference("gatewaypref");
    	gatewayPreference.setEntries(interfaces);
    	gatewayPreference.setEntryValues(interfaces);
    	gatewayPreference.setValue(currGatewayInterface);
    	
    	if (interfaceList.contains(currGatewayInterface)) {
    		gatewayPreference.setValue(currGatewayInterface);
    	} else {
    		gatewayPreference.setValue(ManetConfig.GATEWAY_INTERFACE_NONE);
    	}
    	
    	
        // screen on
    	CheckBoxPreference screenOnPreference = (CheckBoxPreference)findPreference("screenonpref");
    	screenOnPreference.setChecked(manetcfg.isScreenOnWhenInAdhocMode());
    	
        
        // battery temperature
        

        setupFlag = true;
    }
    
    private void updateConfig() {
        for (String key : sharedPreferences.getAll().keySet()) {
            updateConfig(key);
        }
    }
    
    // invoked each time a preference is changed
    private void updateConfig(String key) {
    	
    	boolean updateFlag = false;
    	Map<String,String> oldcfgmap = new TreeMap<String,String>(manetcfg.toMap());
    	
		if (key.equals("uidpref")) {
			String userId = sharedPreferences.getString("uidpref", ManetConfig.USER_ID_DEFAULT.toString());
			manetcfg.setUserId(userId);
		}
		else if (key.equals("encalgorithmpref")) {
			String encAlgorithm = sharedPreferences.getString("encalgorithmpref", 
					ManetConfig.WIFI_ENCRYPTION_ALGORITHM_DEFAULT.toString());
			manetcfg.setWifiEncryptionAlgorithm(WifiEncryptionAlgorithmEnum.fromString(encAlgorithm));
			updateFlag = true;
		}
		else if (key.equals("encsetuppref")) {
			String encSetupMethod = sharedPreferences.getString("encsetuppref", 
					ManetConfig.WIFI_ENCRYPTION_SETUP_METHOD_DEFAULT.toString());
			manetcfg.setWifiEncryptionSetupMethod(WifiEncryptionSetupMethodEnum.fromString(encSetupMethod));
		}
		else if (key.equals("passwordpref")) {
			String encPassword = sharedPreferences.getString("passwordpref", ManetConfig.WIFI_ENCRYPTION_PASSWORD_DEFAULT);
			manetcfg.setWifiEncryptionPassword(encPassword);
    	}
		else if (key.equals("ssidpref")) {
    		String wifiSsid = sharedPreferences.getString("ssidpref", ManetConfig.WIFI_ESSID_DEFAULT);
    		manetcfg.setWifiSsid(wifiSsid);
    	}
    	else if (key.equals("channelpref")) {
    		String wifiChannel = sharedPreferences.getString("channelpref", 
    				ManetConfig.WIFI_CHANNEL_DEFAULT.toString());
    		manetcfg.setWifiChannel(WifiChannelEnum.fromString(wifiChannel));
    	}
    	else if (key.equals("txpowerpref")) {
    		String wifiTxpower = sharedPreferences.getString("txpowerpref", 
    				ManetConfig.WIFI_TXPOWER_DEFAULT.toString());
    		manetcfg.setWifiTxPower(WifiTxpowerEnum.fromString(wifiTxpower));
    	}
    	else if (key.equals("interfacepref")) {
    		String wifiInterface = sharedPreferences.getString("interfacepref", 
    				ManetConfig.WIFI_INTERFACE_DEFAULT.toString());
    		manetcfg.setWifiInterface(wifiInterface);
    		updateFlag = true;
    	}
    	else if (key.equals("ippref")) {
	    	String ipAddress = sharedPreferences.getString("ippref", ManetConfig.IP_ADDRESS_DEFAULT);
	    	manetcfg.setIpAddress(ipAddress);
    	}
    	else if (key.equals("dnspref")) {
	    	String dnsServer = sharedPreferences.getString("dnspref", ManetConfig.DNS_SERVER_DEFAULT);
	    	manetcfg.setDnsServer(dnsServer);
    	}
    	else if (key.equals("bluetoothonpref")) {
    		final Boolean bluetoothOn = sharedPreferences.getBoolean("bluetoothonpref", 
    				ManetConfig.ADHOC_MODE_DEFAULT == AdhocModeEnum.BLUETOOTH);
    		if (bluetoothOn) {
    			manetcfg.setAdhocMode(AdhocModeEnum.BLUETOOTH);
    		} else {
    			manetcfg.setAdhocMode(AdhocModeEnum.WIFI);
    		}
    		updateFlag = true;
    	}
    	else if (key.equals("bluetoothkeepwifipref")) {
    		Boolean btKeepWifi = sharedPreferences.getBoolean("bluetoothkeepwifipref", !ManetConfig.BLUETOOTH_DISABLE_WIFI_DEFAULT);
    		manetcfg.setDisableWifiWhenUsingBluetooth(!btKeepWifi);
    	}
    	else if (key.equals("bluetoothdiscoverablepref")) {
    		Boolean btDiscoverable = sharedPreferences.getBoolean("bluetoothdiscoverablepref", ManetConfig.BLUETOOTH_DISCOVERABLE_DEFAULT);
    		manetcfg.setBlutoothDiscoverableWhenInAdhocMode(btDiscoverable);
    	}
    	else if (key.equals("routingprotocolpref")) {
    		String routingProtocol = sharedPreferences.getString("routingprotocolpref", ManetConfig.ROUTING_PROTOCOL_DEFAULT);
    		manetcfg.setRoutingProtocol(routingProtocol);
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
    	else if (key.equals("gatewaypref")) {
    		String gatewayInterface = sharedPreferences.getString("gatewaypref", 
    				ManetConfig.GATEWAY_INTERFACE_DEFAULT.toString());
    		manetcfg.setGatewayInterface(gatewayInterface);
    		updateFlag = true;
    	}
		else if (key.equals("screenonpref")) {
    		Boolean screenOn = sharedPreferences.getBoolean("screenonpref", ManetConfig.SCREEN_ON_DEFAULT);
    		manetcfg.setScreenOnWhenInAdhocMode(screenOn);
		}
    	
    	Map<String,String> newcfgmap = manetcfg.toMap();
    	dirtyFlag |= !oldcfgmap.equals(newcfgmap);
    	
    	if (updateFlag) {
    		updateView(); // selecting some options may change the available choices for other options
    	}
    }
    
    private void checkIfDirty() {
    	if (dirtyFlag) {
			openConfirmDialog();
			dirtyFlag = false; // reset flag
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
    	updateConfig(key);
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
