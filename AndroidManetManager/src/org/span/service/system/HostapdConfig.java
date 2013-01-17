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

import java.util.HashMap;

import android.util.Log;

public class HostapdConfig extends HashMap<String, String> {
	
	public static final String TAG = "HostapdConfig";

	private static final long serialVersionUID = 1L;
	
	public HashMap<String, String> read() {
		String filename = CoreTask.DATA_FILE_PATH + "/conf/hostapd.conf";
		this.clear();
		for (String line : CoreTask.readLinesFromFile(filename)) {
			if (line.startsWith("#"))
				continue;
			if (!line.contains("="))
				continue;
			String[] data = line.split("=");
			if (data.length > 1) {
				this.put(data[0], data[1]);
			} 
			else {
				this.put(data[0], "");
			}
		}
		return this;
	}
	
	public boolean write() {
		String lines = new String();
		for (String key : this.keySet()) {
			lines += key + "=" + this.get(key) + "\n";
		}
		if (CoreTask.writeLinesToFile(CoreTask.DATA_FILE_PATH + "/conf/hostapd.conf", lines) == false) {
    		Log.e(TAG, "Unable to update conf/hostapd.conf.");
    		return false;
    	}    	
    	return true;
	}
}