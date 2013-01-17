/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dijkstra {

	public static final String MSG_TAG = "ADHOC -> Dijkstra";
	
	public static Route findShortestRoute(Node startNode, Node endNode, Map<String, Node> nodes) {
		
		// System.out.println("Dijkstra.findShortestRoute() nodes: " + nodes); // DEBUG
		
		// node queue
		Set<String> queue = new HashSet<String>();
		queue.addAll(nodes.keySet());
		
		// initialize
		Map<String, Integer> dist = new HashMap<String, Integer>(); // total distance (cost) from start node to another node
		Map<String, String> prev = new HashMap<String, String>(); // previous node (hop) routing backwards to start node
		Map<String, Timestamp> expire = new HashMap<String, Timestamp>();
		
		for (String addr : queue) {
			dist.put(addr, Integer.MAX_VALUE);
		}
		
		dist.put(startNode.addr, 0);
		
		while (!queue.isEmpty()) {
			
			String nearestNeighborAddr = findNearestNeighbor(queue, dist);
			if (nearestNeighborAddr == null) {
				return null;
			}
			
			if (endNode.addr.equals(nearestNeighborAddr)) {
				return createRoute(startNode, endNode, prev, expire);
			}
			
			queue.remove(nearestNeighborAddr);
			// System.out.println("Dijkstra.findShortestRoute() nearestNeighborAddr: " + nearestNeighborAddr); // DEBUG
			// System.out.println("Dijkstra.findShortestRoute() edges: " + nodes.get(nearestNeighborAddr).edges); // DEBUG
			
			// relax
			for (Edge edge : nodes.get(nearestNeighborAddr).edges) {
				// System.out.println("Dijkstra.findShortestRoute() edge: " + edge); // DEBUG
				
				// we may not know enough about the node yet
				if (!nodes.containsKey(edge.toAddr)) {
					continue;
				}
				
				int alt = dist.get(nearestNeighborAddr) + edge.cost;
				if (alt < dist.get(edge.toAddr)) { // TODO: problem
					dist.put(edge.toAddr, alt);
					prev.put(edge.toAddr, nearestNeighborAddr);
					expire.put(edge.toAddr, edge.expireTimestamp);
				}
			}
		}
		
		return null;
	}
	
	private static String findNearestNeighbor(Set<String> nodes, Map<String, Integer> dist) {
		int shortestCost = Integer.MAX_VALUE; // positive infinity
		String nearestNeighborAddr = null;
		
		for (String addr : dist.keySet()) {
			if (!nodes.contains(addr)) {
				continue;
			}
			if (dist.get(addr) < shortestCost) {
				shortestCost = dist.get(addr);
				nearestNeighborAddr = addr;
			}
		}
		
		return nearestNeighborAddr;
	}
	
	// don't include the starting node in the route
	private static Route createRoute(Node startNode, Node endNode, 
			Map<String, String> prev, Map<String, Timestamp> expire) {
		
		List<String> hops = new ArrayList<String>();
		
		Timestamp expireTimestamp = null;
		Timestamp edgeTimestamp = null;
		String addr = endNode.addr;
		
		while (!addr.equals(startNode.addr)) {
			hops.add(addr);
			edgeTimestamp = expire.get(addr);
			addr = prev.get(addr);
			
			if (edgeTimestamp != null) {
				if (expireTimestamp == null || 
					expireTimestamp.after(edgeTimestamp)) {
					expireTimestamp = edgeTimestamp;
				}
			}
		}
		
		// forward order
		Collections.reverse(hops);
		
		// create route		
		Route route = new Route(hops, expireTimestamp);

		return route;
	}
}
