/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.util.HashSet;

import org.span.service.system.ManetConfig;



public abstract class RoutingProtocol {

	protected String error = null;
		
	public abstract String getName();
	
	public abstract boolean start(ManetConfig manetcfg);
	
	public abstract boolean stop();
	
	public abstract boolean isRunning();
	
	public abstract HashSet<Node> getPeers();
	
	public abstract String getInfo();
	
	public abstract String getError();
}
