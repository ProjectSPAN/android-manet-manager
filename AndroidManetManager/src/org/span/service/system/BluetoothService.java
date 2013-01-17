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

import android.app.Application;
import android.os.Build;

public abstract class BluetoothService {

	public abstract boolean startBluetooth();
	public abstract boolean stopBluetooth();
	public abstract boolean isBluetoothEnabled();
	public abstract void setApplication(Application application);
	
	private static BluetoothService bluetoothService;
	
	public static BluetoothService getInstance() {
	    if (bluetoothService == null) {
	        String className;

	        int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
	        if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
	            className = "org.span.service.system.BluetoothService_cupcake";
	        } else {
	            className = "org.span.service.system.BluetoothService_eclair";
	        }
	
	        try {
	            Class<? extends BluetoothService> clazz = Class.forName(className).asSubclass(BluetoothService.class);
	            bluetoothService = clazz.newInstance();
	        } catch (Exception e) {
	            throw new IllegalStateException(e);
	        }
	    }
	    return bluetoothService;
	}
}
