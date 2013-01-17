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
package org.span.service.system;

import java.util.TreeMap;

import org.span.service.system.ManetConfig.WifiEncryptionAlgorithmEnum;
import org.span.service.system.ManetConfig.WifiEncryptionSetupMethodEnum;
import org.span.service.system.ManetConfig.WifiTxpowerEnum;


import android.os.Environment;

public class ManetConfigHelper {

	public ManetConfig readConfig() {
		String filename = CoreTask.DATA_FILE_PATH + "/conf/manet.conf";
		return readConfig(filename);
	}
	
	public ManetConfig readConfig(String filename) {
		TreeMap<String,String> map = new TreeMap<String,String>();
		
		for (String line : CoreTask.readLinesFromFile(filename)) {
			if (line.startsWith("#"))
				continue;
			if (!line.contains("="))
				continue;
			String[] data = line.split("=");
			if (data.length > 1) {
				map.put(data[0], data[1]);
			} 
			else {
				map.put(data[0], "");
			}
		}
		return new ManetConfig(map);
	}
	
	public boolean writeConfig(ManetConfig manetcfg) {
		TreeMap<String,String> map = manetcfg.toMap();
		String lines = new String();
		for (String key : map.keySet()) {
			lines += key + "=" + map.get(key) + "\n";
		}
		// attempt to write manet file to SD
		CoreTask.writeLinesToFile(Environment.getExternalStorageDirectory() + "/manet.conf", lines);
		// write manet file to /data/data
		return CoreTask.writeLinesToFile(CoreTask.DATA_FILE_PATH + "/conf/manet.conf", lines);
	}
	

	// create new configuration with derived settings
	public ManetConfig update(final ManetConfig manetcfg) {
			
		WifiTxpowerEnum txpower = manetcfg.getWifiTxpower();
		WifiEncryptionSetupMethodEnum wepsetupMethod = manetcfg.getWifiEncryptionSetupMethod();
		WifiEncryptionAlgorithmEnum wifiEncyptionAlgorithm = manetcfg.getWifiEncryptionAlgorithm();
		
		TreeMap<String,String> map = manetcfg.toMap(); // manipulate map directly
		
        // device
        String deviceType = DeviceConfig.getDeviceType();
        
		map.put(ManetConfig.DEVICE_TYPE_KEY, deviceType);
		        
		if (DeviceConfig.enableFixPersist(deviceType)) {
			map.put(ManetConfig.ADHOC_FIX_PERSIST_KEY, "true");
		} else {
			map.put(ManetConfig.ADHOC_FIX_PERSIST_KEY, "false");
		}
		
		if (DeviceConfig.enableFixRoute()) {
			map.put(ManetConfig.ADHOC_FIX_ROUTE_KEY, "true");
		} else {
			map.put(ManetConfig.ADHOC_FIX_ROUTE_KEY, "false");
		}
		
		/*
		// TODO: quick and ugly workaround for nexus
		if (deviceType.equals(DeviceConfig.DEVICE_NEXUSONE) &&
				DeviceConfig.getWifiInterfaceDriver(deviceType).equals(DeviceConfig.DRIVER_SOFTAP_GOG)) {			
			map.put(ManetConfig.WIFI_INTERFACE_KEY, "wl0.1");
		}
		*/
		if (manetcfg.getWifiInterface().equals(ManetConfig.WIFI_INTERFACE_DEFAULT)) {
			map.put(ManetConfig.WIFI_INTERFACE_KEY, DeviceConfig.getWifiInterface(deviceType));
		}

		map.put(ManetConfig.WIFI_TXPOWER_KEY, txpower.toString());

		
		// WEP encryption
		String interfaceDriver = DeviceConfig.getWifiInterfaceDriver(deviceType);
		
		if (wifiEncyptionAlgorithm != WifiEncryptionAlgorithmEnum.NONE) {
			if (interfaceDriver.startsWith("softap")) {
				map.put(ManetConfig.WIFI_ENCRYPTION_ALGORITHM_KEY, WifiEncryptionAlgorithmEnum.WPA2.toString());
			} else if (interfaceDriver.equals(DeviceConfig.DRIVER_HOSTAP)) {
				map.put(ManetConfig.WIFI_ENCRYPTION_ALGORITHM_KEY, WifiEncryptionAlgorithmEnum.NONE.toString());
			} else {
				map.put(ManetConfig.WIFI_ENCRYPTION_ALGORITHM_KEY, WifiEncryptionAlgorithmEnum.WEP.toString());
			}

			// getting encryption method if setup method on auto 
			if (wepsetupMethod.equals("auto")) {
				wepsetupMethod = DeviceConfig.getEncryptionAutoMethod(deviceType);
			}
			
			// setting setup mode
			map.put(ManetConfig.WIFI_ENCRYPTION_SETUP_METHOD_KEY, wepsetupMethod.toString());
        }
		
		// determine driver wpa_supplicant
		map.put(ManetConfig.WIFI_DRIVER_KEY, DeviceConfig.getWifiInterfaceDriver(deviceType));
		
		// determine supported kernel features
		boolean bluetoothSupport = DeviceConfig.hasKernelFeature("CONFIG_BT_BNEP=");
		map.put(ManetConfig.BLUETOOTH_KERNEL_SUPPORT_KEY, Boolean.toString(bluetoothSupport));
		
		// create a new config
		ManetConfig newcfg = new ManetConfig(map);
		
		// write the new config file
		writeConfig(newcfg);
		
		return newcfg;
	}
}