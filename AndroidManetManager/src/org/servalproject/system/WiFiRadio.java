/**
 * Copyright (C) 2011 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.system;

import java.io.IOException;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WiFiRadio {

	// WifiManager
	private WifiManager wifiManager = null;
	private WifiApControl wifiApManager = null;
	
	public WiFiRadio(Context context) {
		// init wifiManager
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiApManager = WifiApControl.getApControl(wifiManager);
	}

	private void waitForApState(int newState) throws IOException {
		for (int i = 0; i < 50; i++) {

			int state = wifiApManager.getWifiApState();
			if (state >= 10)
				state -= 10;

			if (state == newState)
				return;

			if (state == WifiApControl.WIFI_AP_STATE_FAILED
					|| state == WifiApControl.WIFI_AP_STATE_DISABLED)
				break;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		throw new IOException("Failed to control access point mode");
	}

	private void waitForApEnabled(boolean enabled) throws IOException {
		for (int i = 0; i < 50; i++) {
			if (enabled == this.wifiApManager.isWifiApEnabled())
				return;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		throw new IOException("Failed to control access point mode");
	}

	public void startAp(String ssid) throws IOException {
		int tries = 0;

		WifiConfiguration netConfig = new WifiConfiguration();
		netConfig.SSID = ssid;
		netConfig.allowedAuthAlgorithms
				.set(WifiConfiguration.AuthAlgorithm.OPEN);

		while (true) {
			try {
				if (this.wifiManager.isWifiEnabled())
					this.wifiManager.setWifiEnabled(false);

				if (!this.wifiApManager.setWifiApEnabled(netConfig, true))
					throw new IOException("Failed to control access point mode");

				waitForApState(WifiApControl.WIFI_AP_STATE_ENABLED);

				waitForApEnabled(true);

				break;
			} catch (IOException e) {
				if (++tries >= 5) {
					throw e;
				}
				Log.e("BatPhone", "Failed", e);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	public void stopAp() throws IOException {
		if (!this.wifiApManager.setWifiApEnabled(null, false))
			throw new IOException("Failed to control access point mode");
		waitForApState(WifiApControl.WIFI_AP_STATE_DISABLED);
		waitForApEnabled(false);
	}
}
