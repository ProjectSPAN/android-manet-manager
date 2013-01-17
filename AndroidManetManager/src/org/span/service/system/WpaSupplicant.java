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

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

/*
 * A class to handle the wpa supplicant config file.
 */
public class WpaSupplicant {
	
	public boolean exists() {
		File file = new File(CoreTask.DATA_FILE_PATH + "/conf/wpa_supplicant.conf");
		return (file.exists() && file.canRead());
	}
	
    public boolean remove() {
    	File file = new File(CoreTask.DATA_FILE_PATH + "/conf/wpa_supplicant.conf");
    	if (file.exists()) {
	    	return file.delete();
    	}
    	return false;
    }

    public Hashtable<String,String> get() {
    	File inFile = new File(CoreTask.DATA_FILE_PATH + "/conf/wpa_supplicant.conf");
    	if (inFile.exists() == false) {
    		return null;
    	}
    	Hashtable<String,String> SuppConf = new Hashtable<String,String>();
    	ArrayList<String> lines = CoreTask.readLinesFromFile(CoreTask.DATA_FILE_PATH + "/conf/wpa_supplicant.conf");

    	for (String line : lines) {
    		if (line.contains("=")) {
	    		String[] pair = line.split("=");
	    		if (pair[0] != null && pair[1] != null && pair[0].length() > 0 && pair[1].length() > 0) {
	    			SuppConf.put(pair[0].trim(), pair[1].trim());
	    		}
    		}
    	}
    	return SuppConf;
    }   
    
    public synchronized boolean write(Hashtable<String,String> values) {
    	String filename = CoreTask.DATA_FILE_PATH + "/conf/wpa_supplicant.conf";
    	String fileString = "";
    	
    	ArrayList<String>inputLines = CoreTask.readLinesFromFile(filename);
    	for (String line : inputLines) {
    		if (line.contains("=")) {
    			String key = line.split("=")[0];
    			if (values.containsKey(key)) {
    				line = key+"="+values.get(key);
    			}
    		}
    		line+="\n";
    		fileString += line;
    	}
    	if (CoreTask.writeLinesToFile(filename, fileString)) {
    		CoreTask.chmod(filename, "0644");
    		return true;
    	}
    	return false;
    }
}