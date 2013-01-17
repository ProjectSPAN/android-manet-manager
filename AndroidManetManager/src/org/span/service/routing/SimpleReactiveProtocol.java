/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.util.HashSet;
import java.util.Set;

import org.span.service.system.ManetConfig;



public class SimpleReactiveProtocol extends RoutingProtocol {

	public static final String MSG_TAG = "ADHOC -> SimpleReactiveProtocol";
	
	public static final String NAME = "Simple Reactive Routing";
	
	private ManetConfig manetcfg = null;
		
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public boolean start(ManetConfig manetcfg) {
		this.manetcfg = manetcfg;
		return true;
    }
    
	@Override
    public boolean stop() {
		return true;
    }
	
	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public HashSet<Node> getPeers() {
		return null;
	}

	@Override
	public String getInfo() {
		return null;
	}
	
	@Override
	public String getError() {
		return this.error;
	}
}
