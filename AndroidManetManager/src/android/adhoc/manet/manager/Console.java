/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package android.adhoc.manet.manager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.util.Log;

public class Console {
	
    private static final String TAG = "Console";
	
    private static CircularStringBuffer buffer = new CircularStringBuffer();
    
    private static ViewConsoleActivity consoleActivity = null;
    
    public static synchronized String getContent() {
    	String content = buffer.toString();
    	// content = content.substring(0, content.length()-1); // remove last newline char
    	// buffer.clear();
    	return content;
    }
    
	public static synchronized void out(final String line) {
    	Log.i(TAG, line);
    	updateMessageArea(line);
    }

    public static synchronized void err(final Exception e) {
    	try {
    		Writer result = new StringWriter();
	    	PrintWriter writer = new PrintWriter(result);
	    	e.printStackTrace(writer);
	    	err(result.toString());
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
	
    public static synchronized void err(final String line) {
    	Log.e(TAG, line);
    	updateMessageArea(line);
    }
    
    public static void setViewConsoleActivity(ViewConsoleActivity viewActivity) {
    	consoleActivity = viewActivity;
    }
    
    public static void clearViewConsoleActivity() {
    	consoleActivity = null;
    }
    
    private static void updateMessageArea(final String line) {
    	if (consoleActivity != null) {
    		consoleActivity.appendMessage(line);
    	}
    	buffer.append(line + "\n");
    }
}