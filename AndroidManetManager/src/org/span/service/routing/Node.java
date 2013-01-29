/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class Node implements Serializable {
	
	private static final long serialVersionUID = -430256718263829916L;

	// TODO
	// gps location
	// battery level
	// etc.
	
	public String addr = null;
	public Set<Edge> edges = null;
	
	public Timestamp expireTimestamp = null;
	
	public boolean isGateway = false;
	public String dnsAddr = null;
	
	public String userId = null;
		
	public Node(String addr, String userId) {
		this.addr = addr;
		this.userId = userId;
		edges = new UpdatableHashSet<Edge>();
	}
	
	public Node(String addr) {
		this(addr, null);
	}
	
	@Override
	public String toString() {
		return addr;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Node) {
			Node other = (Node)obj;
			return addr.equals(other.addr);
		} else {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return addr.hashCode();
	}
	
	// custom set that will update existing elements upon adding them;
	// normally sets will do nothing when attempting to add() an element that equals() an existing one
	private class UpdatableHashSet<E> extends HashSet<E> {
		
		private static final long serialVersionUID = 8477606970943344081L;

		@Override
		public boolean add(E element) {
			super.remove(element);
			return super.add(element);
		}
	}
}
