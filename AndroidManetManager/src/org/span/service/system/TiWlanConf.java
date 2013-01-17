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
import java.util.Collections;
import java.util.Hashtable;

public class TiWlanConf {
    /*
     * Handle operations on the TiWlan.conf file.
     */
    public Hashtable<String,String> get() {
    	Hashtable<String,String> tiWlanConf = new Hashtable<String,String>();
    	ArrayList<String> lines = CoreTask.readLinesFromFile(CoreTask.DATA_FILE_PATH + "/conf/tiwlan.ini");

    	for (String line : lines) {
    		String[] pair = line.split("=");
    		if (pair[0] != null && pair[1] != null && pair[0].length() > 0 && pair[1].length() > 0) {
    			tiWlanConf.put(pair[0].trim(), pair[1].trim());
    		}
    	}
    	return tiWlanConf;
    }
 
    public synchronized boolean write(String name, String value) {
    	Hashtable<String, String> table = new Hashtable<String, String>();
    	table.put(name, value);
    	return write(table);
    }
    
    public synchronized boolean write(Hashtable<String,String> values) {
    	String filename = CoreTask.DATA_FILE_PATH + "/conf/tiwlan.ini";
    	ArrayList<String> valueNames = Collections.list(values.keys());

    	String fileString = "";
    	
    	ArrayList<String> inputLines = CoreTask.readLinesFromFile(filename);
    	for (String line : inputLines) {
    		for (String name : valueNames) {
        		if (line.contains(name)){
	    			line = name+" = "+values.get(name);
	    			break;
	    		}
    		}
    		line+="\n";
    		fileString += line;
    	}
    	return CoreTask.writeLinesToFile(filename, fileString); 	
    }
}