/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.span.service.routing.OlsrProtocol;



// data container to be passed to/from applications and the MANET service
public class ManetConfig implements Serializable {

	// static variables
	
	// automatically configured or derived
	public static final String DEVICE_TYPE_KEY 				= "device.type";			// galaxys2epictouch, etc.
	public static final String WIFI_INTERFACE_KEY			= "wifi.interface"; 		// eth0, wl0.1
	public static final String WIFI_DRIVER_KEY 				= "wifi.driver";			// wext, softap_htc1, softap_htc2, softap_gog
	public static final String IP_NETMASK_KEY 				= "ip.netmask";				// e.g. 255.255.255.0
	public static final String IP_NETWORK_KEY 				= "ip.network";				// e.g. 192.168.1.0
	public static final String IP_GATEWAY_KEY 				= "ip.gateway";				// e.g. 192.168.1.254
	
	public static final String ADHOC_FIX_PERSIST_KEY		= "adhoc.fix.persist";
	public static final String ADHOC_FIX_ROUTE_KEY			= "adhoc.fix.route";
	
	// optionally configured
	public static final String ADHOC_MODE_KEY				= "adhoc.mode";				// bluetooth, wifi
	public static final String ROUTING_PROTOCOL_KEY 		= "routing.protocol";		// olsr, etc.
	public static final String ROUTING_IGNORE_LIST_KEY		= "routing.ignore.list";
	public static final String WIFI_ESSID_KEY				= "wifi.essid";
	public static final String WIFI_CHANNEL_KEY				= "wifi.channel";
	public static final String WIFI_TXPOWER_KEY 			= "wifi.txpower";
	public static final String IP_ADDRESS_KEY 				= "ip.address";
	public static final String DNS_SERVER_KEY 				= "dns.server";
	
	public static final String BLUETOOTH_KERNEL_SUPPORT_KEY	= "bluetooth.kernel.support";
	public static final String BLUETOOTH_DISABLE_WIFI_KEY 	= "bluetooth.disable.wifi";
	public static final String BLUETOOTH_DISCOVERABLE_KEY 	= "bluetooth.make.discoverable";
	
	public static final String GATEWAY_INTERFACE_KEY		= "gateway.interface";
	
	public static final String WIFI_ENCRYPTION_ALGORITHM_KEY = "wifi.encryption.algorithm";	// wpa2-psk, wep, none
	public static final String WIFI_ENCRYPTION_SETUP_METHOD_KEY = "wifi.encryption.setup";	// iwconfig, wpa_supplicant, auto
	public static final String WIFI_ENCRYPTION_PASSWORD_KEY = "wifi.encryption.password";
		
	public static final String USER_ID_KEY					= "userid";
	
	public static final String SCREEN_ON_KEY				= "screen.on";
	
	// constants
	public static final String WIFI_DRIVER_WEXT = "wext";
	public static final String GATEWAY_INTERFACE_NONE = "none";
	

	// enumerations
	
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String str) {
		if( c != null && str != null ) {
			T vals[] = c.getEnumConstants();
			for (int i = 0; i < vals.length; i++) {
				if(vals[i].toString().equals(str)) {
					return vals[i];
				}
			} 
	    }
	    return null;
	}
	
	public static <T extends Enum<T>> String[] getStringValuesFromEnum(Class<T> c) {
		T vals[] = c.getEnumConstants();
		String strvals[] = new String[vals.length];
		for (int i = 0; i < vals.length; i++) {
			strvals[i] = vals[i].toString();
		}
		return strvals;
	}
	
	public static enum AdhocModeEnum {
		WIFI("wifi", "wifi"),
		BLUETOOTH("bluetooth", "bluetooth");

		private String text;
		private String desc;
		AdhocModeEnum(String text, String desc) {
			this.text = text;
			this.desc = desc;
		}
		public String toString() {
			return this.text;
		}
		public String getDescription() {
			return this.desc;
		}
		public static AdhocModeEnum fromString(String name) {
		    return getEnumFromString(AdhocModeEnum.class, name);
		}
		public static String[] stringValues() {
			return getStringValuesFromEnum(AdhocModeEnum.class);
		}
		public static String[] descriptionValues() {
			AdhocModeEnum vals[] = AdhocModeEnum.values();
			String strvals[] = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strvals[i] = vals[i].getDescription();
			}
			return strvals;
		}
	}

	public static enum WifiTxpowerEnum {
		/*
		AUTO("auto", "auto"), 
		TXPOWER_3MW( "3mW",  "3 mW (5 dBm)"), 
		TXPOWER_5MW( "5mW",  "5 mW (7 dBm)"), 
		TXPOWER_10MW("10mW", "10 mW (10 dBm)"), 
		TXPOWER_15MW("15mW", "15 mW (12 dBm)"),
		TXPOWER_20MW("20mW", "20 mW (13 dBm)"), 
		TXPOWER_25MW("25mw", "25 mW (14 dBm)"), 
		TXPOWER_30MW("30mW", "30 mW (15 dBm)");
		*/
		
		// conversion:
		// P = 10 * log(W)
		// W = 10^(P/10)
		
		// power levels supported by Samsung Galaxy S II
		AUTO("auto", "auto"),
		TXPOWER_0_DBM(  "0", "0 mW (0 dBm)"),
		TXPOWER_4_DBM(  "4", "1 mW (1 dBm)"),
		TXPOWER_5_DBM(  "5", "3 mW (5 dBm)"),
		TXPOWER_7_DBM(  "7", "5 mW (7 dBm)"),
		TXPOWER_8_DBM(  "8", "6 mW (8 dBm)"),
		TXPOWER_9_DBM(  "9", "7 mW (9 dBm)"),
		TXPOWER_10_DBM("10", "10 mW (10 dBm)"),
		TXPOWER_11_DBM("11", "12 mW (11 dBm)"),
		TXPOWER_12_DBM("12", "15 mW (12 dBm)"),
		TXPOWER_13_DBM("13", "19 mW (13 dBm)"),
		TXPOWER_14_DBM("14", "25 mW (14 dBm)"),
		TXPOWER_16_DBM("16", "39 mW (16 dBm)"),
		TXPOWER_17_DBM("17", "50 mW (17 dBm)"),
		TXPOWER_18_DBM("18", "63 mW (18 dBm)"),
		TXPOWER_19_DBM("19", "79 mW (19 dBm)"),
		TXPOWER_20_DBM("20", "100 mW (20 dBm)"),
		TXPOWER_22_DBM("22", "158 mW (22 dBm)"),
		TXPOWER_24_DBM("24", "251 mW (24 dBm)"),
		TXPOWER_25_DBM("25", "316 mW (25 dBm)"),
		TXPOWER_26_DBM("26", "398 mW (26 dBm)"),
		TXPOWER_27_DBM("27", "501 mW (27 dBm)"),
		TXPOWER_29_DBM("29", "794 mW (29 dBm)"),
		TXPOWER_30_DBM("30", "1000 mW (30 dBm)"),
		TXPOWER_32_DBM("31", "1258 mW (32 dBm)");
		
		private String text;
		private String desc;
		WifiTxpowerEnum(String text, String desc) {
			this.text = text;
			this.desc = desc;
		}
		public String toString() {
			return this.text;
		}
		public String getDescription() {
			return this.desc;
		}
		public WifiTxpowerEnum valueof(String str) {
			return valueof(str.toUpperCase());
		}
		public static WifiTxpowerEnum fromString(String name) {
		    return getEnumFromString(WifiTxpowerEnum.class, name);
		}
		public static String[] stringValues() {
			return getStringValuesFromEnum(WifiTxpowerEnum.class);
		}
		public static String[] descriptionValues() {
			WifiTxpowerEnum vals[] = WifiTxpowerEnum.values();
			String strvals[] = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strvals[i] = vals[i].getDescription();
			}
			return strvals;
		}
	}
	
	public static enum WifiEncryptionSetupMethodEnum {
		AUTO("auto", "auto"), 
		IWCONFIG("iwconfig", "iwconfig"), 
		WPA_SUPPLICANT("wpa_supplicant", "wpa_supplicant");

		private String text;
		private String desc;
		WifiEncryptionSetupMethodEnum(String text, String desc) {
			this.text = text;
			this.desc = desc;
		}
		public String toString() {
			return this.text;
		}
		public String getDescription() {
			return this.desc;
		}
		public static WifiEncryptionSetupMethodEnum fromString(String name) {
		    return getEnumFromString(WifiEncryptionSetupMethodEnum.class, name);
		}
		public static String[] stringValues() {
			return getStringValuesFromEnum(WifiEncryptionSetupMethodEnum.class);
		}
		public static String[] descriptionValues() {
			WifiEncryptionSetupMethodEnum vals[] = WifiEncryptionSetupMethodEnum.values();
			String strvals[] = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strvals[i] = vals[i].getDescription();
			}
			return strvals;
		}
	}
		
	public static enum WifiChannelEnum {
		
		// US 801.11b/g channel range
		AUTO("auto", "auto"), 
		CHANNEL_1( "1",  "channel 1 (2412 MHz)"),
		CHANNEL_2( "2",  "channel 2 (2417 MHz)"),
		CHANNEL_3( "3",  "channel 3 (2422 MHz)"),
		CHANNEL_4( "4",  "channel 4 (2427 MHz)"),
		CHANNEL_5( "5",  "channel 5 (2432 MHz)"),
		CHANNEL_6( "6",  "channel 6 (2437 MHz)"),
		CHANNEL_7( "7",  "channel 7 (2442 MHz)"),
		CHANNEL_8( "8",  "channel 8 (2447 MHz)"),
		CHANNEL_9( "9",  "channel 9 (2452 MHz)"),
		CHANNEL_10("10", "channel 10 (2457 MHz)"),
		CHANNEL_11("11", "channel 11 (2462 MHz)");

		private String text;
		private String desc;
		WifiChannelEnum(String text, String desc) {
			this.text = text;
			this.desc = desc;
		}
		public String toString() {
			return this.text;
		}
		public String getDescription() {
			return this.desc;
		}
		public static WifiChannelEnum fromString(String name) {
		    return getEnumFromString(WifiChannelEnum.class, name);
		}
		public static String[] stringValues() {
			return getStringValuesFromEnum(WifiChannelEnum.class);
		}
		public static String[] descriptionValues() {
			WifiChannelEnum vals[] = WifiChannelEnum.values();
			String strvals[] = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strvals[i] = vals[i].getDescription();
			}
			return strvals;
		}
	}
	
	public static enum WifiEncryptionAlgorithmEnum {
		NONE("none", "none"), 
		WPA2("wpa2", "WPA2"),
		WEP( "wep",  "WEP");

		private String text;
		private String desc;
		WifiEncryptionAlgorithmEnum(String text, String desc) {
			this.text = text;
			this.desc = desc;
		}
		public String toString() {
			return this.text;
		}
		public String getDescription() {
			return this.desc;
		}
		public static WifiEncryptionAlgorithmEnum fromString(String name) {
		    return getEnumFromString(WifiEncryptionAlgorithmEnum.class, name);
		}
		public static String[] stringValues() {
			return getStringValuesFromEnum(WifiEncryptionAlgorithmEnum.class);
		}
		public static String[] descriptionValues() {
			WifiEncryptionAlgorithmEnum vals[] = WifiEncryptionAlgorithmEnum.values();
			String strvals[] = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strvals[i] = vals[i].getDescription();
			}
			return strvals;
		}
	}
	
	// defaults
	public static final String DEVICE_TYPE_KEY_DEFAULT = DeviceConfig.DEVICE_GENERIC;
	public static final AdhocModeEnum ADHOC_MODE_DEFAULT = AdhocModeEnum.WIFI;
	public static final String ROUTING_PROTOCOL_DEFAULT = OlsrProtocol.NAME;
	public static final String ROUTING_IGNORE_LIST_DEFAULT = "[]";
	public static final String WIFI_INTERFACE_DEFAULT = "eth0";
	public static final String WIFI_DRIVER_DEFAULT = WIFI_DRIVER_WEXT;
	public static final String WIFI_ESSID_DEFAULT = "AndroidAdhoc";
	public static final WifiChannelEnum WIFI_CHANNEL_DEFAULT = WifiChannelEnum.CHANNEL_1;
	public static final WifiEncryptionAlgorithmEnum WIFI_ENCRYPTION_ALGORITHM_DEFAULT = WifiEncryptionAlgorithmEnum.NONE;
	public static final String WIFI_ENCRYPTION_PASSWORD_DEFAULT = "abcdefghijklm";
	public static final WifiEncryptionSetupMethodEnum WIFI_ENCRYPTION_SETUP_METHOD_DEFAULT 
		= WifiEncryptionSetupMethodEnum.WPA_SUPPLICANT;
	public static final WifiTxpowerEnum WIFI_TXPOWER_DEFAULT = WifiTxpowerEnum.AUTO;
	public static final String IP_ADDRESS_DEFAULT = "192.168.1.100";
	public static final String DNS_SERVER_DEFAULT = "208.67.222.222"; // OpenDNS
	
	public static final boolean ADHOC_FIX_PERSIST_DEFAULT = false;
	public static final boolean ADHOC_FIX_ROUTE_DEFAULT = false;
	
	public static final boolean BLUETOOTH_KERNEL_SUPPORT_DEFAULT = false;
	public static final boolean BLUETOOTH_DISABLE_WIFI_DEFAULT = false;
	public static final boolean BLUETOOTH_DISCOVERABLE_DEFAULT = false;
	
	public static final String GATEWAY_INTERFACE_DEFAULT = GATEWAY_INTERFACE_NONE;
	
	public static final String USER_ID_DEFAULT = "Anonymous";
	
	public static final boolean SCREEN_ON_DEFAULT = false; // may not work on Nexus 7
	
	private static final long serialVersionUID = 1L;
		
	// private variables
	private TreeMap<String,String> map = null;
	
	// default constructor
	public ManetConfig() {
		this.map = createDefaultMap();
	}
	
	public ManetConfig(TreeMap<String,String> map) {
		this.map = createDefaultMap(); // ensure default settings
		this.map.putAll(map); // use specified settings
	}
	
	public TreeMap<String,String> toMap() {
		return map;
	}
	
	private TreeMap<String,String> createDefaultMap() {
		map = new TreeMap<String,String>(); // alphabetical order
		
		// set default values
		map.put(DEVICE_TYPE_KEY,			DEVICE_TYPE_KEY_DEFAULT);
		map.put(WIFI_INTERFACE_KEY,			WIFI_INTERFACE_DEFAULT);
		map.put(WIFI_DRIVER_KEY,			WIFI_DRIVER_DEFAULT);
			
		map.put(ADHOC_MODE_KEY, 			ADHOC_MODE_DEFAULT.toString());
		map.put(ROUTING_PROTOCOL_KEY, 		ROUTING_PROTOCOL_DEFAULT);
		map.put(ROUTING_IGNORE_LIST_KEY,    ROUTING_IGNORE_LIST_DEFAULT);
		map.put(WIFI_ENCRYPTION_ALGORITHM_KEY, WIFI_ENCRYPTION_ALGORITHM_DEFAULT.toString());
		map.put(WIFI_ENCRYPTION_PASSWORD_KEY, WIFI_ENCRYPTION_PASSWORD_DEFAULT);
		map.put(WIFI_ESSID_KEY,				WIFI_ESSID_DEFAULT);
		map.put(WIFI_ENCRYPTION_SETUP_METHOD_KEY, WIFI_ENCRYPTION_SETUP_METHOD_DEFAULT.toString());
		map.put(WIFI_CHANNEL_KEY, 			WIFI_CHANNEL_DEFAULT.toString());
		map.put(WIFI_TXPOWER_KEY,			WIFI_TXPOWER_DEFAULT.toString());
		
		setIpAddress(IP_ADDRESS_DEFAULT);
		
		map.put(DNS_SERVER_KEY, 			DNS_SERVER_DEFAULT);
		
		map.put(ADHOC_FIX_PERSIST_KEY, 		Boolean.toString(ADHOC_FIX_PERSIST_DEFAULT));
		map.put(ADHOC_FIX_ROUTE_KEY, 		Boolean.toString(ADHOC_FIX_ROUTE_DEFAULT));
		
		map.put(BLUETOOTH_KERNEL_SUPPORT_KEY, Boolean.toString(BLUETOOTH_KERNEL_SUPPORT_DEFAULT));
		map.put(BLUETOOTH_DISABLE_WIFI_KEY, Boolean.toString(BLUETOOTH_DISABLE_WIFI_DEFAULT));
		map.put(BLUETOOTH_DISCOVERABLE_KEY, Boolean.toString(BLUETOOTH_DISCOVERABLE_DEFAULT));
		
		map.put(USER_ID_KEY, 				USER_ID_DEFAULT);
		
		map.put(SCREEN_ON_KEY, 				Boolean.toString(SCREEN_ON_DEFAULT));
		
		map.put(GATEWAY_INTERFACE_KEY,		GATEWAY_INTERFACE_DEFAULT);
		
		return map;
	}
	
	
	// getters
	
	public String getDeviceType() {
		return map.get(DEVICE_TYPE_KEY);
	}
	
	public String getUserId() {
		return map.get(USER_ID_KEY);
	}
	
	public String getWifiInterface() {
		return map.get(WIFI_INTERFACE_KEY);
	}
	
	public WifiEncryptionAlgorithmEnum getWifiEncryptionAlgorithm() {
		return WifiEncryptionAlgorithmEnum.fromString(map.get(WIFI_ENCRYPTION_ALGORITHM_KEY));
	}
	
	public String getWifiDriver() {
		return map.get(WIFI_DRIVER_KEY);
	}
	
	public String getIpNetmask() {
		return map.get(IP_NETMASK_KEY);
	}
	
	public String getIpNetwork() {
		return map.get(IP_NETWORK_KEY);
	}
	
	public String getIpBroadcast() {
		return map.get(IP_NETWORK_KEY);
	}
	
	public String getIpGateway() {
		return map.get(IP_GATEWAY_KEY);
	}
	
	public AdhocModeEnum getAdhocMode() {
		return AdhocModeEnum.fromString(map.get(ADHOC_MODE_KEY));
	}
	
	public String getRoutingProtocol() {
		return map.get(ROUTING_PROTOCOL_KEY);
	}
	
	public List<String> getRoutingIgnoreList() {
		List<String> ignoreList = new ArrayList<String>();
		try {
			JSONArray array = new JSONArray(map.get(ROUTING_IGNORE_LIST_KEY));
			for (int i = 0 ; i < array.length(); i++){ 
				ignoreList.add(array.get(i).toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ignoreList;
	}
	
	public String getWifiSsid() {
		return map.get(WIFI_ESSID_KEY);
	}
	
	public WifiChannelEnum getWifiChannel() {
		return WifiChannelEnum.fromString(map.get(WIFI_CHANNEL_KEY));
	}
	
	public WifiTxpowerEnum getWifiTxpower() {
		return WifiTxpowerEnum.fromString(map.get(WIFI_TXPOWER_KEY));
	}
	
	public String getWifiEncryptionPassword() {
		return map.get(WIFI_ENCRYPTION_PASSWORD_KEY);
	}
	
	public WifiEncryptionSetupMethodEnum getWifiEncryptionSetupMethod() {
		return WifiEncryptionSetupMethodEnum.fromString(map.get(WIFI_ENCRYPTION_SETUP_METHOD_KEY));
	}
	
	public String getIpAddress() {
		return map.get(IP_ADDRESS_KEY);
	}
	
	public String getDnsServer() {
		return map.get(DNS_SERVER_KEY);
	}
	
	public String getGatewayInterface() {
		return map.get(GATEWAY_INTERFACE_KEY);
	}
	
	
	// query 
	
	public boolean isWifiEncryptionEnabled() {
		return getWifiEncryptionAlgorithm() != WifiEncryptionAlgorithmEnum.NONE;
	}
	
	public boolean isUsingBluetooth() {
		return getAdhocMode() == AdhocModeEnum.BLUETOOTH;
	}
	
	public boolean isUsingWifi() {
		return getAdhocMode() == AdhocModeEnum.WIFI;
	}
	
	public boolean isWifiDisabledWhenUsingBluetooth() {
		return Boolean.parseBoolean(map.get(BLUETOOTH_DISABLE_WIFI_KEY)); 
	}
	
	public boolean isBluetoothDiscoverableWhenInAdhocMode() {
		return Boolean.parseBoolean(map.get(BLUETOOTH_DISCOVERABLE_KEY)); 
	}
	
    public boolean isTransmitPowerSupported() {
    	// only supported for the nexusone 
    	if (getWifiDriver().equals(DeviceConfig.DRIVER_WEXT)) {
    		return true;
    	}
    	return false;
    }
    
    public boolean isBluetoothSupported() {
    	return Boolean.parseBoolean(map.get(BLUETOOTH_KERNEL_SUPPORT_KEY));
    }
    
	public boolean isScreenOnWhenInAdhocMode() {
		return Boolean.parseBoolean(map.get(SCREEN_ON_KEY));
	}
	
	
	// setters
    
    public void setUserId(String userId) {
    	map.put(USER_ID_KEY, userId);
    }
	
	public void setAdhocMode(AdhocModeEnum mode) {
		map.put(ADHOC_MODE_KEY, mode.toString());
	}
	
	public void setRoutingProtocol(String protocol) {
		map.put(ROUTING_PROTOCOL_KEY, protocol);
	}
	
	public void setRoutingIgnoreList(List<String> ignoreList) {
		JSONArray array = new JSONArray(ignoreList);
		map.put(ROUTING_IGNORE_LIST_KEY, array.toString());
	}
	
	public void setWifiSsid(String ssid) {
		map.put(WIFI_ESSID_KEY, ssid);
	}
	
	public void setWifiChannel(WifiChannelEnum channel) {
		map.put(WIFI_CHANNEL_KEY, channel.toString());
	}
	
	public void setWifiTxPower(WifiTxpowerEnum power) {
		map.put(WIFI_TXPOWER_KEY, power.toString());
	}
	
	public void setWifiInterface(String wifiInterface) {
		map.put(WIFI_INTERFACE_KEY, wifiInterface);
	}
	
	public void setWifiEncryptionPassword(String encPassword) {
		map.put(WIFI_ENCRYPTION_PASSWORD_KEY, encPassword);
	}
	
	public void setWifiEncryptionSetupMethod(WifiEncryptionSetupMethodEnum encSetupMethod) {
		map.put(WIFI_ENCRYPTION_SETUP_METHOD_KEY, encSetupMethod.toString());
	}
	
	public void setWifiEncryptionAlgorithm(WifiEncryptionAlgorithmEnum encAlgorithm) {
		map.put(WIFI_ENCRYPTION_ALGORITHM_KEY, encAlgorithm.toString());
	}
		
	public void setIpAddress(String addr) {
		// int numRoutingBits = 24; // TODO: support subnets other than /24
        String prefix = addr.substring(0, addr.lastIndexOf("."));
        
        map.put(ManetConfig.IP_ADDRESS_KEY, addr);
        map.put(ManetConfig.IP_NETWORK_KEY, prefix + ".0");
        map.put(ManetConfig.IP_GATEWAY_KEY, prefix + ".254");
        map.put(ManetConfig.IP_NETMASK_KEY, "255.255.255.0");
	}
	
	public void setDnsServer(String addr) {
        map.put(ManetConfig.DNS_SERVER_KEY, addr);
	}
	
	public void setDisableWifiWhenUsingBluetooth(boolean disable) {
		map.put(BLUETOOTH_DISABLE_WIFI_KEY, Boolean.toString(disable));
	}
	
	public void setBlutoothDiscoverableWhenInAdhocMode(boolean discoverable) {
		map.put(BLUETOOTH_DISCOVERABLE_KEY, Boolean.toString(discoverable));
	}
	
	public void setGatewayInterface(String gatewayInterface) {
		map.put(GATEWAY_INTERFACE_KEY, gatewayInterface);
	}
	
	public void setScreenOnWhenInAdhocMode(boolean screenOn) {
		map.put(SCREEN_ON_KEY, Boolean.toString(screenOn));
	}
}