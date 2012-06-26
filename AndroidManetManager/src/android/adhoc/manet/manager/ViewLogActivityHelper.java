/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package android.adhoc.manet.manager;

import android.adhoc.manet.CircularStringBuffer;
import android.adhoc.manet.LogObserver;

// helper class to help maintain state between activity instances
public class ViewLogActivityHelper implements LogObserver {
	
	private ViewLogActivity activity = null;
	
	public boolean messageScrollLock = true;
	public CircularStringBuffer buff = new CircularStringBuffer();
	
	// singleton
	private static ViewLogActivityHelper instance = null;
	
	private ViewLogActivityHelper() {}
	
	public static void setApplication(ManetManagerApp app) {
		if (instance == null) {
			instance = new ViewLogActivityHelper();
		}
        app.manet.registerLogger(instance);
	}
	
	public static ViewLogActivityHelper getInstance(ViewLogActivity activity) {
		if (instance == null) {
			instance = new ViewLogActivityHelper();
		}
		
		// can only be associated with one activity at a time
		instance.activity = activity;
		
		return instance;
	}
	
	// callback method
	@Override
	public void onLogUpdated(String content) {
		if (activity != null) {
			activity.appendMessage(content);
		}
	}
}
