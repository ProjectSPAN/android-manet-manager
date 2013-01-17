/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.io.Serializable;
import java.sql.Timestamp;

public class Edge implements Serializable, Comparable<Edge> {
	
	private static final long serialVersionUID = 2859504103725492935L;
	
	public String toAddr = null;
	public Timestamp expireTimestamp = null;
	public int cost = Integer.MAX_VALUE; // infinity
	
	public Edge(String toAddr, int cost, Timestamp expireTimestamp) {
		this.toAddr = toAddr;
		this.cost = cost;
		this.expireTimestamp = expireTimestamp;
	}

	@Override
	public int compareTo(Edge other) {
		return toAddr.compareTo(other.toAddr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Edge) {
			Edge other = (Edge)obj;
			return compareTo(other) == 0;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return toAddr.hashCode();
	}
	
	@Override
	public String toString() {
		return toAddr;
	}
}
