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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.span.service.system.ManetConfig.WifiEncryptionSetupMethodEnum;


import android.os.Build;

public class DeviceConfig {

	public static final String DEVICE_NEXUSONE   		= "nexusone";
	public static final String DEVICE_GALAXY1X   		= "galaxy1x";
	public static final String DEVICE_GALAXY2X   		= "galaxy2x";
	public static final String DEVICE_LEGEND     		= "legend";
	public static final String DEVICE_DREAM      		= "dream";
	public static final String DEVICE_MOMENT     		= "moment";
	public static final String DEVICE_ALLY       		= "ally";
	public static final String DEVICE_DROIDX     		= "droidx";
	public static final String DEVICE_BLADE      		= "blade";
	public static final String DEVICE_GENERIC    		= "generic";
	public static final String DEVICE_GALAXYTABORIG		= "galaxytaborig";
	public static final String DEVICE_GALAXYS2EPICTOUCH	= "galaxys2epictouch";
	public static final String DEVICE_TRANSFORMERPRIME  = "transformerprime";
	public static final String DEVICE_GALAXYTAB10_1		= "galaxytab10.1";
	public static final String DEVICE_GALAXYNEXUS		= "galaxynexus";
	public static final String DEVICE_DROIDRAZR			= "droidrazr"; // Droid Razr Maxx
	public static final String DEVICE_NEXUS7			= "nexus7";
	public static final String DEVICE_GALAXYS3SGHI747	= "galaxys3sghi747";
	public static final String DEVICE_GALAXYS3GTI9300	= "galaxys3gti9300";
	public static final String DEVICE_GALAXYNOTE2GTN7100	= "galaxynote2gtn7100";
	
	public static final String DRIVER_TIWLAN0     = "tiwlan0";
	public static final String DRIVER_WEXT        = "wext";
	public static final String DRIVER_SOFTAP_HTC1 = "softap_htc1";
	public static final String DRIVER_SOFTAP_HTC2 = "softap_htc2";
	public static final String DRIVER_SOFTAP_GOG  = "softap_gog";
	public static final String DRIVER_HOSTAP      = "hostap";
	
	/**
	 * Returns the device-type as string.
	 * A very ugly hack - checking for wifi-kernel-modules.
	 */
	
	public static String getDeviceType() {
		/*
		// UNTESTED
		if ((new File("/system/lib/modules/bcm4329.ko")).exists() == true) {
			return DEVICE_NEXUSONE;
		}
		else if ((new File("/system/libmodules/bcm4325.ko")).exists() == true) {
			int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
        	if (sdkVersion >= Build.VERSION_CODES.DONUT) {
        		return DEVICE_GALAXY2X;
        	}
			return DEVICE_GALAXY1X;
		}
		else if ((new File("/system/lib/modules/tiap_drv.ko")).exists() == true
				&& (new File("/system/bin/Hostapd")).exists() == true
				&& (new File("/system/etc/wifi/fw_tiwlan_ap.bin")).exists() == true
				&& (new File("/system/etc/wifi/tiwlan_ap.ini")).exists() == true) {
			return DEVICE_DROIDX;
		}
		else if ((new File("/system/lib/modules/tiwlan_drv.ko")).exists() == true 
				&& (new File("/system/etc/wifi/Fw1273_CHIP.bin")).exists() == true) {
			return DEVICE_LEGEND;
		}
		else if ((new File("/system/lib/modules/wlan.ko")).exists() == true) {
			return DEVICE_DREAM;
		}
		else if ((new File("/lib/modules/dhd.ko")).exists() == true
				&& (new File("/etc/rtecdc.bin")).exists() == true) {
			return DEVICE_MOMENT;
		}
		else if ((new File("/system/lib/modules/wireless.ko")).exists() == true
				&& (new File("/system/etc/wl/rtecdc.bin")).exists() == true
				&& (new File("/system/etc/wl/nvram.txt")).exists() == true) {
			return DEVICE_ALLY;
		}
		else if ((new File("/system/wifi/ar6000.ko")).exists() == true
				&& (new File("/system/bin/hostapd")).exists() == true) {
			return DEVICE_BLADE;
		}
		*/
		
		System.out.println("Build.MODEL: " + Build.MODEL); // DEBUG
		
		if (Build.MODEL.equals("Galaxy Nexus")) {
			return DEVICE_GALAXYNEXUS;
		} else if(Build.MODEL.equals("Transformer Prime TF201")) {
			return DEVICE_TRANSFORMERPRIME;
		} else if(Build.MODEL.equals("GT-P7510")) {
			return DEVICE_GALAXYTAB10_1;
		} else if(Build.MODEL.equals("DROID RAZR")) {
			return DEVICE_DROIDRAZR;
		} else if (Build.MODEL.equals("SPH-D710")) {
			return DEVICE_GALAXYS2EPICTOUCH;
		} else if (Build.MODEL.equals("Nexus 7")) {
			return DEVICE_NEXUS7;
		} else if (Build.MODEL.equals("SAMSUNG-SGH-I747")) {
			return DEVICE_GALAXYS3SGHI747;
		} else if (Build.MODEL.equals("GT-I9300")) {
			return DEVICE_GALAXYS3GTI9300;
		} else if (Build.MODEL.equals("GT-N7100")){
			return DEVICE_GALAXYNOTE2GTN7100;
		} else if ((new File("/lib/modules/dhd.ko")).exists() == true
				&& (new File("/system/etc/wifi/bcm4329_sta.bin")).exists() == true) {
			// TODO: use Build.MODEL as conditional
			return DEVICE_GALAXYTABORIG;
		}
		return DEVICE_GENERIC; // (e.g. Nexus S 4G)
	}
	
	
	/**
	 * Returns the wpa_supplicant-driver which should be used
	 * on wpa_supplicant-start 
	 */
	public static String getWifiInterfaceDriver(String deviceType) {
		/*
		if (deviceType.equals(DEVICE_DREAM)) {
			return DRIVER_TIWLAN0;
		}
		// Extremely ugly stuff here - we really need a better method to detect such stuff
		else if (deviceType.equals(DEVICE_NEXUSONE) && hasKernelFeature("CONFIG_BCM4329_SOFTAP=")) {
			if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.FROYO) {
				return DRIVER_SOFTAP_HTC2;
			}
			return DRIVER_SOFTAP_HTC1;
		}
		else if (deviceType.equals(DEVICE_NEXUSONE) && (
				(new File("/etc/firmware/fw_bcm4329_apsta.bin")).exists() || (new File("/vendor/firmware/fw_bcm4329_apsta.bin")).exists())
			) {
			return DRIVER_SOFTAP_GOG;
		}
		else if (deviceType.equals(DEVICE_DROIDX) || deviceType.equals(DEVICE_BLADE)) {
			return DRIVER_HOSTAP;
		}
		*/
		return DRIVER_WEXT;
	}
	
	public static String getWifiInterface(String deviceType) {
		if (deviceType.equals(DEVICE_GALAXYNEXUS) || 
				deviceType.equals(DEVICE_TRANSFORMERPRIME) ||
				deviceType.equals(DEVICE_NEXUS7) ||
				deviceType.equals(DEVICE_GALAXYS3SGHI747) ||
				deviceType.equals(DEVICE_GALAXYS3GTI9300) ||
				deviceType.equals(DEVICE_GALAXYNOTE2GTN7100)) {
			return "wlan0";
		} else if (deviceType.equals(DEVICE_DROIDRAZR)) {
			return "tiwlan0";
		}
		return ManetConfig.WIFI_INTERFACE_DEFAULT;
	}

	/**
	 * Returns the wpa_supplicant-driver which should be used on wpa_supplicant-start 
	 */
	public static WifiEncryptionSetupMethodEnum getEncryptionAutoMethod(String deviceType) {
		if (deviceType.equals(DEVICE_LEGEND) || deviceType.equals(DEVICE_NEXUSONE)) {
			return WifiEncryptionSetupMethodEnum.IWCONFIG;
		}
		return WifiEncryptionSetupMethodEnum.WPA_SUPPLICANT;
	}
	
	/**
	 * Returns a boolean if fix_persist.sh is required
	 * @param feature
	 * @return
	 */
	public static boolean enableFixPersist(String deviceType) {
		if ((new File("/system/lib/modules/tiwlan_drv.ko")).exists() == true 
				&& (new File("/system/etc/wifi/fw_wlan1271.bin")).exists() == true
				&& getWifiInterfaceDriver(getDeviceType()).equals(DRIVER_WEXT) == true){
			return true;
		}
		if (deviceType.equals(DEVICE_LEGEND) == true) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns a boolean if fix_persist.sh is required
	 * @param feature
	 * @return
	 */
	public static boolean enableFixRoute() {
		if ((new File("/system/etc/iproute2/rt_tables")).exists() == true 
				&& CoreTask.getProp("ro.product.manufacturer").equalsIgnoreCase("HTC")) {
			return true;
		}
		return false;
	}	
	
	// TODO: update this method to work with modern devices
    public static boolean hasKernelFeature(String feature) {
    	try {
			File cfg = new File("/proc/config.gz");
			if (cfg.exists() == false) {
				return true;
			}
			FileInputStream fis = new FileInputStream(cfg);
			GZIPInputStream gzin = new GZIPInputStream(fis);
			BufferedReader in = null;
			String line = "";
			in = new BufferedReader(new InputStreamReader(gzin));
			while ((line = in.readLine()) != null) {
				   if (line.startsWith(feature)) {
					    gzin.close();
						return true;
					}
			}
			gzin.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    public static boolean loadKernelModules(String deviceType) {
    	
    	boolean success = true;
    	
    	if (deviceType.equals(DEVICE_TRANSFORMERPRIME)) {
    		// assumes kernel compiled with ALFA support
    		
    		CoreTask.runRootCommand("rmmod rtl8187");
    		CoreTask.runRootCommand("rmmod eeprom_93cx6");
    		CoreTask.runRootCommand("rmmod mac80211");
    		
    		if (success) {
    			success = CoreTask.runRootCommand("insmod /system/lib/modules/mac80211.ko");
    		}
    		if (success) {
    			success = CoreTask.runRootCommand("insmod /system/lib/modules/eeprom_93cx6.ko");
    		}
    		if (success) {
    			success = CoreTask.runRootCommand("insmod /system/lib/modules/rtl8187.ko");
    		}
    	}
    	
    	return success;
    }
}
