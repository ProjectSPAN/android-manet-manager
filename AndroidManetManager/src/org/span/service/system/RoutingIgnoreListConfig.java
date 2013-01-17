/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.system;

import java.util.HashMap;
import java.util.List;

import android.util.Log;

public class RoutingIgnoreListConfig extends HashMap<String, String> {

	public static final String TAG = "RoutingIgnoreListsConfig";
	
	private static final long serialVersionUID = 1L;
	
	private List<String> routingIgnoreList = null;
	
	public void set(List<String> routingIgnoreList) {
		this.routingIgnoreList = routingIgnoreList;
	}
	
	public boolean write() {
    	StringBuffer buffer = new StringBuffer();;
    	for (String addr : routingIgnoreList) {  		
    		buffer.append(addr+"\n");
    	}
    	if (CoreTask.writeLinesToFile(CoreTask.DATA_FILE_PATH + "/conf/routing_ignore_list.conf", buffer.toString()) == false) {
    		Log.e(TAG, "Unable to update conf/routing_ignore_list.conf.");
    		return false;
    	}    	
    	return true;
	}
}