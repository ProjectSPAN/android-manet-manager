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
package org.span.service.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.span.R;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.routing.OlsrProtocol;
import org.span.service.routing.RoutingProtocol;
import org.span.service.routing.SimpleProactiveProtocol;
import org.span.service.system.BluetoothConfig;
import org.span.service.system.BluetoothService;
import org.span.service.system.CoreTask;
import org.span.service.system.DeviceConfig;
import org.span.service.system.DnsmasqConfig;
import org.span.service.system.HostapdConfig;
import org.span.service.system.ManetConfig;
import org.span.service.system.ManetConfigHelper;
import org.span.service.system.RoutingIgnoreListConfig;
import org.span.service.system.TiWlanConf;
import org.span.service.system.WpaSupplicant;
import org.span.service.system.ManetConfig.WifiChannelEnum;
import org.span.service.system.ManetConfig.WifiEncryptionSetupMethodEnum;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.util.Log;
import android.widget.Toast;

public class ManetServiceHelper {
		
	private static final String TAG = "ManetServiceHelper";
	
	private static final int ALARM_INTERVAL_MILLISEC = 30000;
		
	// devices information
	private String deviceType = DeviceConfig.DEVICE_GENERIC; 
	private String interfaceDriver = DeviceConfig.DRIVER_WEXT; 
	
	// startUp check performed
	private boolean startupCheckPerformed = false;

	// wifi management
	private WifiManager wifiManager = null;
	private WifiManager.WifiLock wifiLock = null;
	
	// power management
	private PowerManager powerManager = null;
	private PowerManager.WakeLock wakeLock = null;
	
	// intent management
	private IntentReceiver intentReceiver = null;
	
	// alarm management
	private AlarmManager alarmManager = null;
	private AlarmReceiver alarmReceiver = null;

	// bluetooth
	private BluetoothService bluetoothService = null;
	
	// DNS-Server-Update Thread
	// private Thread dnsUpdateThread = null;
    
	// original states
	private static boolean origWifiState = false;
	private static boolean origBluetoothState = false;
	
	// files
	private ManetConfig manetcfg = null; 		// properties file
	private BluetoothConfig btcfg = null; 		// blue-up.sh script
	private DnsmasqConfig dnsmasqcfg = null;	// file used by ./bin/dnsmasq
	private HostapdConfig hostapdcfg = null;	// file used by /system/bin/hostapd (droidx, blade)
	private WpaSupplicant wpasupplicant = null;	// file used by /system/bin/wpa_supplicant (encryption)
	private TiWlanConf tiwlan = null;			// file used by /system/bin/wlan (HTC dream, HTC legend)
	private RoutingIgnoreListConfig routingignorelistcfg = null; // file used by routing protocol
	
	private ManetConfigHelper manetcfgHelper = null;
	
	// routing protocol
	private RoutingProtocol routingProtocol = null;
	
	// MANET service
	private ManetService service = null;
	
	private DisplayMessageHandler displayMessageHandler = null;
	
	private AdhocStateEnum adhocState = AdhocStateEnum.UNKNOWN;
	private String adhocInfo = null;
	
	// singleton
	private static ManetServiceHelper instance = null;
	
	private ManetServiceHelper() {}
	
	public static ManetServiceHelper getInstance() {
		if (instance == null) {
			instance = new ManetServiceHelper();
		}
		return instance;
	}
	
	public void setService(ManetService service) {
		this.service = service;
	}
	
	public AdhocStateEnum getAdhocState() {
		return adhocState;
	}
	
	public String getAdhocInfo() {
		return adhocInfo;
	}

	public boolean setup() {
		Log.d(TAG, "setup");

		displayMessageHandler = new DisplayMessageHandler();
		
		// configure CoreTask
		String path = service.getFilesDir().getParent();
		CoreTask.setPath(path);
		Log.d(TAG, "Current directory is " + path);
        
        // supplicant config
        wpasupplicant = new WpaSupplicant();
        
        // tiwlan config
        tiwlan = new TiWlanConf();
        
    	// dnsmasq config
    	dnsmasqcfg = new DnsmasqConfig();
    	
    	// hostapd config
    	hostapdcfg = new HostapdConfig();
    	
    	// ignore list config
    	routingignorelistcfg = new RoutingIgnoreListConfig();
    	
    	// blue-up.sh
    	btcfg = new BluetoothConfig();        
            	
        // wifi management
        wifiManager = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "ADHOC_WIFI_LOCK");

        // bluetooth service
        bluetoothService = BluetoothService.getInstance();
        bluetoothService.setApplication(service.getApplication());
                
        // startup check; prevent performing these actions multiple times
        if (startupCheckPerformed == false) {
	        startupCheckPerformed = true;
	        
	        // check home dir, or create it
	        checkDirs();
	        
	    	// Check if required kernel-features are enabled
	    	if (!CoreTask.isNetfilterSupported()) {
	    		service.showNotification(service.getString(R.string.error_nonetfilter));
	    		return false;
	    	}
	    		
        	// Check root-permission, files
	    	if (!CoreTask.hasRootPermission()) {
	    		service.showNotification(service.getString(R.string.error_notroot));
	    		return false;
	    	}
	    	
	        // manet.cfg
	        manetcfgHelper = new ManetConfigHelper();
	        // manetcfgHelper.writeConfig(new ManetConfig()); // TODO
	        manetcfg = manetcfgHelper.readConfig();
	    	
	    	// Check if binaries need to be updated
	    	// DEBUG: Always (re)install binaries for development purposes.
	    	// if (this.application.binariesExists() == false || CoreTask.filesetOutdated()) {
	        	installFiles();
	        // }
              
            // update all configs
            updateConfigs(manetcfg);
            
            if (!DeviceConfig.loadKernelModules(manetcfg.getDeviceType())) {
        		displayMessageHandler.sendMessage("Could not load kernel modules!");
        		return false;
            }
            
            this.updateAdhocState(null); // DEBUG
        }
        
        return true;
	}
	
	// TODO: consider eventually using a factory class to mix-and-match protocol modules based on settings
	private RoutingProtocol createRoutingProtocol() {
		if (manetcfg.getRoutingProtocol().equals(SimpleProactiveProtocol.NAME)) {
			return new SimpleProactiveProtocol();
		} else if (manetcfg.getRoutingProtocol().equals(OlsrProtocol.NAME)) {
			return new OlsrProtocol();
		} else {
			return null;
		}
	}
	
	private void displayToastMessage(String message) {
		Toast.makeText(service, message, Toast.LENGTH_LONG).show();
	}
	
	private boolean setBluetoothState(boolean enabled) {
		boolean connected = false;
		if (enabled == false) {
			this.bluetoothService.stopBluetooth();
			return false;
		}
		origBluetoothState = this.bluetoothService.isBluetoothEnabled();
		if (origBluetoothState == false) {
			connected = this.bluetoothService.startBluetooth();
			if (connected == false) {
				Log.d(TAG, "Enable bluetooth failed");
			}
		} else {
			connected = true;
		}
		return connected;
	}
	
	private void updateConfigs(ManetConfig cfg) {
		
		manetcfg = manetcfgHelper.update(cfg); // determine derived settings
		
		long startStamp = System.currentTimeMillis();
		    
		boolean wifiEncEnabled = manetcfg.isWifiEncryptionEnabled();
		String wifiEncPassword = manetcfg.getWifiEncryptionPassword();
		WifiEncryptionSetupMethodEnum wifiEncSetupMethod = manetcfg.getWifiEncryptionSetupMethod();
		String wifiSsid = manetcfg.getWifiSsid();
		WifiChannelEnum wifiChannel = manetcfg.getWifiChannel();
		String ipNetwork = manetcfg.getIpNetwork();
		String ipGateway = manetcfg.getIpGateway();
		List<String> routingIgnoreList = manetcfg.getRoutingIgnoreList();
		
		
		// WEP encryption
		if (wifiEncEnabled) {
			// prepare wpa_supplicant config if wpa_supplicant selected
			if (wifiEncSetupMethod == WifiEncryptionSetupMethodEnum.WPA_SUPPLICANT) {
				
				// install wpa_supplicant.conf-template
				if (!wpasupplicant.exists()) {
					installWpaSupplicantConfig();
				}
				
				// update wpa_supplicant.conf
				Hashtable<String,String> values = new Hashtable<String,String>();
				values.put("ssid", "\"" + wifiSsid + "\"");
				values.put("wep_key0", "\"" + wifiEncPassword + "\"");
				wpasupplicant.write(values);
			}
        } else {
			// make sure to remove wpa_supplicant.conf
			if (wpasupplicant.exists()) {
				wpasupplicant.remove();
			}			
		}
		
		// dnsmasq.conf
		dnsmasqcfg.set(ipNetwork);
		dnsmasqcfg.write();
		
		
		// hostapd.conf
		if (interfaceDriver.equals(DeviceConfig.DRIVER_HOSTAP)) {
			installHostapdConfig();
			hostapdcfg.read();
			
			// update the hostapd-configuration in case we have Motorola Droid X
			if (deviceType.equals(DeviceConfig.DEVICE_DROIDX)) {
				hostapdcfg.put("ssid", wifiSsid);
				hostapdcfg.put("channel", wifiChannel.toString());
				if (wifiEncEnabled) {
					hostapdcfg.put("wpa", ""+2);
					hostapdcfg.put("wpa_pairwise", "CCMP");
					hostapdcfg.put("rsn_pairwise", "CCMP");
					hostapdcfg.put("wpa_passphrase", wifiEncPassword);
				}
			}
			// update the hostapd-configuration in case we have ZTE Blade
			else if (deviceType.equals(DeviceConfig.DEVICE_BLADE)) {
				hostapdcfg.put("ssid", wifiSsid);
				hostapdcfg.put("channel_num", wifiChannel.toString());
				if (wifiEncEnabled) {
					hostapdcfg.put("wpa", ""+2);
					hostapdcfg.put("wpa_key_mgmt", "WPA-PSK");
					hostapdcfg.put("wpa_pairwise", "CCMP");
					hostapdcfg.put("wpa_passphrase", wifiEncPassword);
				}				
			}
			hostapdcfg.write();
		}
		
		// blue-up.sh
		btcfg.set(ipGateway);
		btcfg.write();

		
		// TODO: need to find a better method to identify if the used device is a HTC Dream aka T-Mobile G1
		if (deviceType.equals(DeviceConfig.DEVICE_DREAM)) {
			Hashtable<String,String> values = new Hashtable<String,String>();
			values.put("dot11DesiredSSID", wifiSsid);
			values.put("dot11DesiredChannel", wifiChannel.toString());
			tiwlan.write(values);
		}
		
        // create initial routing protocol
		if (routingProtocol == null) {
			routingProtocol = createRoutingProtocol();
		}
        
        // routing ignore list
        routingignorelistcfg.set(routingIgnoreList);
        routingignorelistcfg.write();
        
		Log.d(TAG, "Creation of configuration files took ==> " + (System.currentTimeMillis()-startStamp) + " milliseconds.");
		
		
		Bundle data = new Bundle();
		data.putSerializable(ManetService.CONFIG_KEY, manetcfg);
		
		// send broadcast
		Intent intent = new Intent(ManetService.ACTION_CONFIG_UPDATED);
		intent.putExtras(data);
    	service.sendBroadcast(intent);
	}

	public void handleStartAdhocCommand(Message rxmessage) {
		
		boolean cont = true;
		
		if (manetcfg.isUsingBluetooth()) {
    		if (!setBluetoothState(true)){
    			cont = false;
    		}
			if (manetcfg.isWifiDisabledWhenUsingBluetooth()) {
	        	disableWifi();
			}
        } else {
        	// disable the default wifi interface to set it in ad-hoc mode
        	// if this device is a gateway and the default interface isn't selected for ad-hoc mode,
        	// then keep the default wifi interface enabled
        	String defaultInterface = DeviceConfig.getWifiInterface(manetcfg.getDeviceType());
        	if (manetcfg.getWifiInterface().equals(defaultInterface)) {
        		disableWifi();
        	}
        }
		
        if (manetcfg.isScreenOnWhenInAdhocMode()) {
            // power management
        	// PARTIAL_WAKE_LOCK will not allow the user to force the device to sleep by pressing the power button
        	// TODO: UDP packets cannot be received while the screen is off, thus we must use SCREEN_DIM_WAKE_LOCK
            powerManager = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
        	wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ADHOC_WAKE_LOCK");
            wakeLock.setReferenceCounted(false);
            
            // can't register for these actions in the Android manifest
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            // intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            intentReceiver = new IntentReceiver();
            service.registerReceiver(intentReceiver, intentFilter);
            
	        // alarm management
            IntentFilter alarmFilter = new IntentFilter();
	        alarmFilter.addAction("org.span.service.intent.action.ALARM_ACTION");
	        AlarmReceiver alarmReceiver = new AlarmReceiver();
	        service.registerReceiver(alarmReceiver, alarmFilter);
	        
	        Intent intent = new Intent(alarmFilter.getAction(0));
	        PendingIntent pending = PendingIntent.getBroadcast(service, 0, intent, 0);  
	        
	        alarmManager = (AlarmManager) service.getSystemService(Context.ALARM_SERVICE);      
	        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, ALARM_INTERVAL_MILLISEC, pending);
        }

        // update resolv.conf-file
        // String dns[] = CoreTask.updateResolvConf();     
        
    	if (cont) {
    		if (CoreTask.startAdhocMode(manetcfg)) {
	    		routingProtocol = createRoutingProtocol();
	        	if (routingProtocol.start(manetcfg)) {
		    		// this.dnsUpdateEnable(dns, true);
	
	        		// initial DNS server setting
	        		CoreTask.setProp("net.dns1", manetcfg.getDnsServer());
	        		
		    		acquireLocks();
	        	}
    		}
    	}
	        
    	// send broadcast
    	updateAdhocState(null);
	}
    
	public void handleStopAdhocCommand(Message rxmessage) {

		// disable polling-threads
    	// this.trafficCounterEnable(false);
		// this.dnsUpdateEnable(false);
		// this.clientConnectEnable(false);
    	
    	releaseLocks();
    	
    	if (intentReceiver != null) {
    		service.unregisterReceiver(intentReceiver);
    		intentReceiver = null;
    	}
    	if (alarmReceiver != null) {
    		service.unregisterReceiver(alarmReceiver);
    		alarmReceiver = null;
    	}
    	
    	routingProtocol.stop();
    	
    	CoreTask.stopAdhocMode(manetcfg);
    	
		// this.notificationManager.cancelAll();
		
		// put wifi and bluetooth back, if applicable
		if (manetcfg.isUsingBluetooth()) {
			if (origBluetoothState == false) {
				setBluetoothState(false);
			}
			if (manetcfg.isWifiDisabledWhenUsingBluetooth()) {
				enableWifi();
			} 
		} else {
			// if the wifi interface set in ad-hoc mode is not the default interface, 
			// then there is no need to enable the default interface
	    	String defaultInterface = DeviceConfig.getWifiInterface(manetcfg.getDeviceType());
	    	if (manetcfg.getWifiInterface().equals(defaultInterface)) {
	    		enableWifi();
	    	}
		}
		
    	// send broadcast
    	updateAdhocState(null);
	}
	
	public void handleRestartAdhocCommand(Message rxmessage) {
		handleStopAdhocCommand(rxmessage);
		handleStartAdhocCommand(rxmessage);
	}
	
	public void handleManetConfigUpdateCommand(Message rxmessage) {
		Bundle data = rxmessage.getData();
		ManetConfig cfg = (ManetConfig)data.getSerializable(ManetService.CONFIG_KEY);
		updateConfigs(cfg);
	}
	
	public void handleManetConfigLoadCommand(Message rxmessage) {
		Bundle data = rxmessage.getData();
		String filename = data.getString(ManetService.FILE_KEY);
		ManetConfig cfg = manetcfgHelper.readConfig(filename);
		updateConfigs(cfg);
		handleRestartAdhocCommand(rxmessage);
	}
    
    public void handleAdhocStatusQuery(Message rxmessage) {
    	updateAdhocState(rxmessage);
    }
    
    private void updateAdhocState(Message rxmessage) {
    	
    	try {
	    	boolean routingProtocolRunning = routingProtocol.isRunning();
	    	boolean adHocModeEnabled = CoreTask.isAdHocModeEnabled(manetcfg);
			
	    	AdhocStateEnum prevAdhocState = adhocState;
	    	
			if (adHocModeEnabled == true && routingProtocolRunning == true) {
				adhocState = AdhocStateEnum.STARTED;
				adhocInfo = "Ad-Hoc mode is running.";
			} else if (adHocModeEnabled == false && routingProtocolRunning == false) {
				adhocState = AdhocStateEnum.STOPPED;
				adhocInfo = "Ad-Hoc mode is not running.";
			} else {
				adhocState = AdhocStateEnum.UNKNOWN;
				adhocInfo = "Your device is currently in an unknown state.\n" +
					"Ad-Hoc mode enabled: " + adHocModeEnabled + "\n" +
					routingProtocol.getName() + " protocol running: " + routingProtocolRunning;
			}
			
			if (prevAdhocState != adhocState) {
				service.showNotification(adhocInfo.split("\n")[0]);
			}
				
			Bundle data = new Bundle();
	    	data.putSerializable(ManetService.STATE_KEY, adhocState);
	    	data.putString(ManetService.INFO_KEY, adhocInfo);
	    	
	    	if (rxmessage == null) {
	    		// send broadcast
	    		Intent intent = new Intent(ManetService.ACTION_ADHOC_STATE_UPDATED);
	    		intent.putExtras(data);
	        	service.sendBroadcast(intent);
		    	
	    	} else {
		    	// send broadcast
		    	Message msg = new Message();
		    	msg.what = rxmessage.what;
		    	msg.setData(data);
		    	rxmessage.replyTo.send(msg);
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public synchronized void updateLog(String content) {
		Bundle data = new Bundle();
		data.putString(ManetService.LOG_KEY, content);
    	
		// send broadcast
		Intent intent = new Intent(ManetService.ACTION_LOG_UPDATED);
		intent.putExtras(data);
    	service.sendBroadcast(intent);
    }
    
    public void handleManetConfigQuery(Message rxmessage) {
    	try {
    		Bundle data = new Bundle();
    		data.putSerializable(ManetService.CONFIG_KEY, manetcfg);
	    	
	    	// send response
	    	Message msg = new Message();
	    	msg.what = rxmessage.what;
	    	msg.setData(data);
	    	rxmessage.replyTo.send(msg);
	    	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void handlePeersQuery(Message rxmessage) {
    	try {
        	HashSet<Node> peers = routingProtocol.getPeers();
    		
    		Bundle data = new Bundle();
    		data.putSerializable(ManetService.PEERS_KEY, peers);
	    	
	    	// send response
	    	Message msg = new Message();
	    	msg.what = rxmessage.what;
	    	msg.setData(data);
	    	rxmessage.replyTo.send(msg);
	    	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void handleRoutingInfoQuery(Message rxmessage) {
    	try {
        	String info = routingProtocol.getInfo();
    		
    		Bundle data = new Bundle();
    		data.putString(ManetService.INFO_KEY, info);
	    	
	    	// send response
	    	Message msg = new Message();
	    	msg.what = rxmessage.what;
	    	msg.setData(data);
	    	rxmessage.replyTo.send(msg);
	    	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
        
    /*
    private String getAdhocNetworkDevice() {
        if (manetcfg.isUsingBluetooth()) {
			return "bnep";
        } else {
			// TODO: Quick and ugly workaround for nexus
			if (DeviceConfig.getDeviceType().equals(DeviceConfig.DEVICE_NEXUSONE) &&
					DeviceConfig.getWifiInterfaceDriver(this.deviceType).equals(DeviceConfig.DRIVER_SOFTAP_GOG)) {
				return "wl0.1";
			} else {
				return CoreTask.getProp("wifi.interface");
			}
		}
    }
    */
    
    /*
    private void makeDiscoverable() {
        Log.d(TAG, "Making device discoverable ...");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);
    }
    */

    // disable the default wifi interface for this device
    private void disableWifi() {
    	if (origWifiState = this.wifiManager.isWifiEnabled()) {
    		this.wifiManager.setWifiEnabled(false);
    		Log.d(TAG, "Wifi disabled!");
        	// Waiting for interface-shutdown
    		try {
    			Thread.sleep(5000);
    		} catch (InterruptedException e) {
    			// nothing
    		}
    	}
    }
    
    // enable the default wifi interface for this device
    private void enableWifi() {
    	// re-enable only if enabled before previously disabled
    	if (origWifiState) {
        	// Waiting for interface-restart
    		this.wifiManager.setWifiEnabled(true);
    		try {
    			Thread.sleep(5000);
    		} catch (InterruptedException e) {
    			// nothing
    		}
    		Log.d(TAG, "Wifi started!");
    	}
    }
    
	public void acquireLocks() {
		try {
			Log.d(TAG, "Trying to acquire locks now");
			
			if (manetcfg.isScreenOnWhenInAdhocMode()) {
				// acquire wakelock to turn on screen
				wakeLock.acquire();
			}
			
			if(!wifiLock.isHeld()) {
				wifiLock.acquire();
			}
		} catch (Exception ex) {
			Log.d(TAG, "Error while trying to acquire lock: " + ex.getMessage());
		}
	}
	
	private void releaseLocks() {
		try {
			Log.d(TAG, "Trying to release locks now");
			wakeLock.release();
			
			if(wifiLock.isHeld()) {
				wifiLock.release();
			}
		} catch (Exception ex) {
			Log.d(TAG, "Error while trying to release lock: " + ex.getMessage());
		}
	}
    
	private boolean binariesExists() {
    	File file = new File(CoreTask.DATA_FILE_PATH + "/bin/adhoc");
    	return file.exists();
    }
    
	private void installWpaSupplicantConfig() {
    	copyFile(CoreTask.DATA_FILE_PATH + "/conf/wpa_supplicant.conf", "0644", R.raw.wpa_supplicant_conf);
    }
    
	private void installHostapdConfig() {
    	if (deviceType.equals(DeviceConfig.DEVICE_DROIDX)) {
    		copyFile(CoreTask.DATA_FILE_PATH + "/conf/hostapd.conf", "0644", R.raw.hostapd_conf_droidx);
    	} else if (deviceType.equals(DeviceConfig.DEVICE_BLADE)) {
    		copyFile(CoreTask.DATA_FILE_PATH + "/conf/hostapd.conf", "0644", R.raw.hostapd_conf_blade);
    	}
    }

	private void installFiles() {
    	
		String message = null;
		
		// check if manet file on SD card
		File manetFile = new File(Environment.getExternalStorageDirectory() + "/manet.conf");
		if (manetFile.exists()) {
			// copy manet file from SD to /data/data
			updateConfigs(manetcfgHelper.readConfig(manetFile.getAbsolutePath()));
		} else {
			manetFile = new File(CoreTask.DATA_FILE_PATH + "/conf/manet.conf");
			// check if file installation is necessary
			if (manetFile.exists()) {
				return;
			}
			// manet.cfg
			if (message == null) {
				message = copyFile(CoreTask.DATA_FILE_PATH + "/conf/manet.conf", "0644", R.raw.manet_conf);
			}
		}
		
		// dnsmasq.conf
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/conf/dnsmasq.conf", "0644", R.raw.dnsmasq_conf);
			CoreTask.updateDnsmasqFilepath();
		}
    	// tiwlan.ini
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/conf/tiwlan.ini", "0644", R.raw.tiwlan_ini);
		}
		// edify script
		if (message == null) { // DEBUG
			message = copyFile(CoreTask.DATA_FILE_PATH + "/conf/adhoc.edify", "0644", R.raw.adhoc_edify);
		}

		// wpa_supplicant drops privileges, we need to make files readable.
		CoreTask.chmod(CoreTask.DATA_FILE_PATH + "/conf/", "0755");
		
    	// adhoc
    	if (message == null) {
    		message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/adhoc", "0755", R.raw.adhoc);
    	}
    	// dnsmasq
    	if (message == null) {
	    	message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/dnsmasq", "0755", R.raw.dnsmasq);
    	}
    	// iptables
    	if (message == null) {
	    	message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/iptables", "0755", R.raw.iptables);
    	}
    	// ifconfig
    	if (message == null) {
	    	message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/ifconfig", "0755", R.raw.ifconfig);
    	}	
    	// iwconfig
    	if (message == null) {
	    	message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/iwconfig", "0755", R.raw.iwconfig);
    	}
    	// ultra_bcm_config (file used by softap wifi driver)
    	if (message == null) {
	    	message = copyFile(CoreTask.DATA_FILE_PATH+"/bin/ultra_bcm_config", "0755", R.raw.ultra_bcm_config);
    	}
    	// pand
    	if (message == null) {
	    	message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/pand", "0755", R.raw.pand);
    	}
    	// blue-up.sh
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/blue-up.sh", "0755", R.raw.blue_up_sh);
		}
		// blue-down.sh
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/blue-down.sh", "0755", R.raw.blue_down_sh);
		}
		
		// installing fix scripts if needed
		if (DeviceConfig.enableFixPersist(deviceType)) {	
			// fixpersist.sh
			if (message == null) {
				message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/fixpersist.sh", "0755", R.raw.fixpersist_sh);
			}				
		}
		if (DeviceConfig.enableFixRoute()) {
			// fixroute.sh
			if (message == null) {
				message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/fixroute.sh", "0755", R.raw.fixroute_sh);
			}
		}
		
		// olsrd
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/olsrd", "0755", R.raw.olsrd);
		}
		// olsrd.conf
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/conf/olsrd.conf.tpl", "0644", R.raw.olsrd_conf_tpl);
		}
		// olsrd_txtinfo.so.0.1
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/olsrd_txtinfo.so.0.1", "0755", R.raw.olsrd_txtinfo_so_0_1);
		}
		// routing_ignore_list.conf
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/conf/routing_ignore_list.conf", "0755", R.raw.routing_ignore_list_conf);
		}
				
		// tcpdump
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/tcpdump", "0755", R.raw.tcpdump);
		}
		
		// netcat
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/nc", "0755", R.raw.nc);
		}
		
		// iperf
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/iperf", "0755", R.raw.iperf);
		}
		
		// asus_forward.sh // TODO
		if (message == null) {
			message = copyFile(CoreTask.DATA_FILE_PATH + "/bin/asus-forward.sh", "0755", R.raw.asus_forward_sh);
		}
		
		/*
		// busybox
		if (message == null) {
			if (!CoreTask.runCommand("ls /system/xbin/busybox") && 
					!CoreTask.runCommand("ls /sbin/busybox")) {
				message = "Busybox not installed!";
			}
		}
		*/
		
		if (message == null) {
	    	message = service.getString(R.string.global_application_installed);
		}
		
		displayMessageHandler.sendMessage(message);
    }
    
    private String copyFile(String filename, String permission, int ressource) {
    	String result = copyFile(filename, ressource);
    	if (result != null) {
    		return result;
    	}
    	if (CoreTask.chmod(filename, permission) != true) {
    		result = "Can't change file-permission for '" + filename + "'!";
    	}
    	return result;
    }
    
    private String copyFile(String filename, int ressource) {
    	File outFile = new File(filename);
    	if (!outFile.exists()) { // don't overwrite existing files
	    	Log.d(TAG, "Copying file '" + filename + "' ...");
	    	InputStream is = service.getResources().openRawResource(ressource);
	    	byte buf[] = new byte[1024];
	        int len;
	        try {
	        	OutputStream out = new FileOutputStream(outFile);
	        	while((len = is.read(buf))>0) {
					out.write(buf,0,len);
				}
	        	out.close();
	        	is.close();
			} catch (IOException e) {
				return "Couldn't install file - " + filename + "!";
			}
    	}
		return null;
    }
    
    private void checkDirs() {
    	File dir = new File(CoreTask.DATA_FILE_PATH);
    	if (dir.exists() == false) {
    			// this.displayToastMessage("Application data-dir does not exist!");
    	}
    	else {
    		String[] dirs = { "/bin", "/var", "/conf" };
    		for (String dirname : dirs) {
    			dir = new File(CoreTask.DATA_FILE_PATH + dirname);
    	    	if (dir.exists() == false) {
    	    		if (!dir.mkdir()) {
    	    			// this.displayToastMessage("Couldn't create " + dirname + " directory!");
    	    		}
    	    	}
    	    	else {
    	    		Log.d(TAG, "Directory '" + dir.getAbsolutePath() + "' already exists!");
    	    	}
    		}
    	}
    }

    /*
    private void dnsUpdateEnable(boolean enable) {
    	this.dnsUpdateEnable(null, enable);
    }
    */
    
    /*
    private void dnsUpdateEnable(String[] dns, boolean enable) {
   		if (enable == true) {
			if (this.dnsUpdateThread == null || this.dnsUpdateThread.isAlive() == false) {
				this.dnsUpdateThread = new Thread(new DnsUpdate(dns));
				this.dnsUpdateThread.start();
			}
   		} else {
	    	if (this.dnsUpdateThread != null)
	    		this.dnsUpdateThread.interrupt();
   		}
   	}
    */  
       
    /*
    private class DnsUpdate implements Runnable {

    	String[] dns;
    	
    	public DnsUpdate(String[] dns) {
    		this.dns = dns;
    	}
    	
		public void run() {
            while (!Thread.currentThread().isInterrupted()) {
            	String[] currentDns = CoreTask.getCurrentDns();
            	if (this.dns == null || this.dns[0].equals(currentDns[0]) == false || this.dns[1].equals(currentDns[1]) == false) {
            		this.dns = CoreTask.updateResolvConf();
            	}
                // Taking a nap
       			try {
    				Thread.sleep(10000);
    			} catch (InterruptedException e) {
    				Thread.currentThread().interrupt();
    			}
            }
		}
    }
    */
    
    private void handleError(String error) {
    	displayMessageHandler.sendMessage(error);
    }
    
    private class DisplayMessageHandler extends Handler {
    	
    	@Override
        public void handleMessage(Message msg) {
       		if (msg.obj != null) {
       			displayToastMessage((String)msg.obj);
       		}
        	super.handleMessage(msg);
        }
    	
    	public void sendMessage(String content) {
    		Message msg = new Message();
    		msg.obj = content;
    		this.sendMessage(msg);
    	}
    };
    
    private class IntentReceiver extends BroadcastReceiver {
    	@Override
        public void onReceive(Context context, Intent intent) {   		 
    		String action = intent.getAction();
            
    		if (action.equals(Intent.ACTION_SCREEN_OFF)) {
       		 	Log.d(TAG, "Intent received: ACTION_SCREEN_OFF"); // DEBUG
       		 	
    			// re-acquire wakelock to turn on screen
    			if (adhocState.equals(AdhocStateEnum.STARTED)) {
    	    		ManetServiceHelper.this.acquireLocks();
    			}
    		}
    	}
    };
    
    private class AlarmReceiver extends BroadcastReceiver {
    	 @Override
    	 public void onReceive(Context context, Intent intent) {
    		 Log.d(TAG, "Alarm activated!"); // DEBUG
    		 
 			// re-acquire wakelock to turn on screen
 			if (adhocState.equals(AdhocStateEnum.STARTED)) {
 	    		ManetServiceHelper.this.acquireLocks();
 			}
    	 }
    };
}
