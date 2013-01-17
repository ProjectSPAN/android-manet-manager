/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service;

public interface LogObserver {

	// callback methods
	
	public void onLogUpdated(String content);
}
