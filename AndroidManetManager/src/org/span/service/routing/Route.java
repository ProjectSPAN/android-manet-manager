/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.sql.Timestamp;
import java.util.List;

public class Route {
	
	public List<String> hops = null;
	public Timestamp expireTimestamp = null;
	
	public Route(List<String> hops, Timestamp expireTimestamp) {
		this.hops = hops;
		this.expireTimestamp = expireTimestamp;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Route) {
			Route otherRoute = (Route)obj;
			return hops.equals(otherRoute.hops);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return hops.toString();
	}
}
