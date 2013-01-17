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

import java.util.ArrayList;

import android.util.Log;

public class BluetoothConfig {
	
	public static final String TAG = "BluetoothConfig";
	
	private static final long serialVersionUID = 1L;
	
	private String ipGateway = null;
	
	public void set(String ipGateway) {
		this.ipGateway = ipGateway;
	}		
	
	public boolean write() {
		StringBuffer buffer = new StringBuffer();;
    	ArrayList<String> inputLines = CoreTask.readLinesFromFile(CoreTask.DATA_FILE_PATH + "/bin/blue-up.sh");   
    	for (String line : inputLines) {
    		if (line.contains("ifconfig bnep0") && line.endsWith("netmask 255.255.255.0 up >> $adhoclog 2>> $adhoclog")) {
    			line = CoreTask.reassembleLine(line, " ", "bnep0", ipGateway);
    		}    		
    		buffer.append(line+"\n");
    	}
    	if (CoreTask.writeLinesToFile(CoreTask.DATA_FILE_PATH + "/bin/blue-up.sh", buffer.toString()) == false) {
    		Log.e(TAG, "Unable to update bin/adhoc with new lan-configuration.");
    		return false;
    	}
    	return true;
	}
}