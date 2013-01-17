/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.io.Serializable;

public class HelloMessage implements Serializable {
	
	private static final long serialVersionUID = 862304425447243742L;
	
	public Node node = null;
	public int numHops = -1;
	
	public HelloMessage(Node node) {
		this.node = node;
		this.numHops = 1;
	}
	
	@Override
	public int hashCode() {
		// don't use number of hops to compare hello messages
		// node's expire timestamp will help ensure unique hashes between hello messages for the same node
		return node.addr.hashCode() + node.expireTimestamp.hashCode();
	}
	
	@Override
	public String toString() {
		return node.addr;
	}
}