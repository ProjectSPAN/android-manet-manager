/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.span.service.core.ManetServiceHelper;
import org.span.service.system.CoreTask;
import org.span.service.system.ManetConfig;


import android.util.Log;

public class SimpleProactiveProtocol extends RoutingProtocol {

	public static final String MSG_TAG = "ADHOC -> SimpleProactiveProtocol";
	
	public static final String NAME = "Simple Proactive Routing";
			
	private static final int MESSAGE_PORT = 5555;
	private static final int MAX_MESSAGE_LENGTH = 1024;				
		
	// demo values
	private static final int BROADCAST_WAIT_TIME_MILLISEC = 5000;
	private static final int ROUTE_GENERATION_WAIT_TIME_MILLISEC = 3000;
	private static final int EXPIRATION_TIME_MILLISEC = 20000;
	
	private static final int LISTEN_SOCKET_TIMEOUT_MILLISEC = 1000;
	private static final int PROCESSED_MESSAGES_MAX_SIZE = 200;
		
	private RoutingThread routingThread = null;
	private MessageBroadcastThread messageBroadcastThread = null;
	private MessageListenerThread messageListenerThread = null;
	
	private volatile Set<MessageProcessorThread> messageProcessorThreads = null;
		
	private volatile Map<String, Node> nodes = null;
	private volatile Map<String, Route> routes = null;
	
	private volatile Node myNode = null;
	private volatile Node gatewayNode = null;
	
	// circular buffer for keeping track of processed broadcast messages
	private volatile CircularFifoBuffer processedMsgs = null;
	
	// private volatile TimestampComparator timestampComparator = null;
	
	private volatile ManetConfig manetcfg = null;
	
	private volatile ManetServiceHelper helper = null;
		
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public boolean start(ManetConfig manetcfg) {
		try {
			this.manetcfg = manetcfg;
			helper = ManetServiceHelper.getInstance();
			error = null;
			
			nodes  = new HashMap<String, Node>();				
			routes = new HashMap<String, Route>();
			
			messageProcessorThreads = new HashSet<MessageProcessorThread>();
			
			// timestampComparator = new TimestampComparator();
						
			// add myself
			myNode = new Node(manetcfg.getIpAddress(), manetcfg.getUserId());
			nodes.put(myNode.addr, myNode);
			
			if (!manetcfg.getGatewayInterface().equals(ManetConfig.GATEWAY_INTERFACE_NONE)) {
				myNode.isGateway = true;
				myNode.dnsAddr = CoreTask.getProp("net.dns1");
			}
			
			processedMsgs = new CircularFifoBuffer(PROCESSED_MESSAGES_MAX_SIZE);
			
			routingThread = new RoutingThread();
			routingThread.start();
			
			messageBroadcastThread = new MessageBroadcastThread();
			messageBroadcastThread.start();			
			
			messageListenerThread = new MessageListenerThread();
			messageListenerThread.start();
						
		} catch (Exception e) {
			handleException(e);
			stop();
			return false;
		}
		
		return true;
    }
    
	@Override
    public boolean stop() {
		try {			
			if (messageBroadcastThread != null) {
				messageBroadcastThread.interrupt();
				// messageBroadcastThread.join();
			}
			
			if (messageListenerThread != null) {
				messageListenerThread.interrupt();
				// messageListenerThread.join();
			}

			if (routingThread != null) {
				routingThread.interrupt();
				// routingThread.join();
			}
			
			for (MessageProcessorThread messageProcessorThread : messageProcessorThreads) {
				messageProcessorThread.interrupt();
			}
			
			
			if (gatewayNode != null) {
				teardownGateway();
			}
			
			Map<String, Route> tmpRoutes = new HashMap<String, Route>(routes);
			for (String addr : tmpRoutes.keySet()) {
				teardownRoute(nodes.get(addr), routes.get(addr));
			}
			
			nodes.clear();
			routes.clear();
			
			routingThread = null;
			messageBroadcastThread = null;
			messageListenerThread = null;
			
		} catch (Exception e) {
			e.printStackTrace();
			error = e.getMessage();
			return false;
		}
		
		return true;
    }
	
	@Override
	public boolean isRunning() {
		if (routingThread == null || 
				messageBroadcastThread == null || 
				messageListenerThread == null) {
			return false;
		}
		return error == null;
	}

	@Override
	public HashSet<Node> getPeers() {
		// only return nodes to which we have a valid route
		HashSet<Node> peers = new HashSet<Node>();
		for (String addr : nodes.keySet()) {
			peers.add(nodes.get(addr));
		}
		return peers;
	}

	// TODO: print routes in half-viz format?
	// TODO: print "via" info for shared routes?
	@Override
	public String getInfo() {
		StringBuffer buff = new StringBuffer();
		
		synchronized(myNode) {

			// gateway
			buff.append("Gateway:\n");
			if (gatewayNode == null) {
				buff.append("none");
			} else {
				buff.append(gatewayNode.addr);
			}
			buff.append("\n\n");
			
			// one-hop peers
			buff.append("One-hop peers:\n");
			Set<String> oneHopNodes = getOneHopNodes();
			if (oneHopNodes.isEmpty()) {
				buff.append("none\n");
			} else {
				for (String addr : oneHopNodes) {
					buff.append("+ ");
					buff.append(addr);
					buff.append("\n");
				}
			}
			buff.append("\n");
			
			// multi-hop peers
			buff.append("Multi-hop peers:\n");
			Set<String> multiHopNodes = getMultiHopNodes();
			if (multiHopNodes.isEmpty()) {
				buff.append("none\n");
			} else {
				for (String addr : multiHopNodes) {
					buff.append("+ ");
					buff.append(addr);
					buff.append("\n");
				}
			}
			buff.append("\n");
			
			// routes
			buff.append("Routes:\n");
			StringBuffer routeBuff = new StringBuffer();
			Route route = null;
			for (String addr : routes.keySet()) {
				route = routes.get(addr);
				if (route != null) {
					routeBuff.append("+ ");
					routeBuff.append(myNode.addr);
					for (String hop : route.hops) {
						routeBuff.append(" -> ");
						routeBuff.append(hop);
					}
					routeBuff.append("\n");
				}
			}
			if (routeBuff.length() == 0) {
				routeBuff.append("none\n");
			}
			buff.append(routeBuff);
			buff.append("\n");
			
			// edges
			buff.append("Edges:\n");
			StringBuffer edgeBuff = new StringBuffer();
			Set<Edge> set = null;
			for (String fromAddr : nodes.keySet()) {
				set = nodes.get(fromAddr).edges;
				if (!set.isEmpty()) {
					for (Edge edge : set) {
						edgeBuff.append("+ ");
						edgeBuff.append(fromAddr);
						edgeBuff.append(" -> ");
						edgeBuff.append(edge.toAddr);
						edgeBuff.append("\n");
					}
				}
			}
			if (edgeBuff.length() == 0) {
				edgeBuff.append("none\n");
			}
			buff.append(edgeBuff);
			buff.append("\n");
		}
			
		return buff.toString();
	}
	
	@Override
	public String getError() {
		return this.error;
	}
	
	private void handleException(Exception e) {
		e.printStackTrace();
		error = e.getMessage();
		stop();
	}

	private Set<String> getOneHopNodes() {
		Set<String> oneHopNodes = new HashSet<String>();
		Route route = null;
		for (String addr : routes.keySet()) {
			route = routes.get(addr);
			if (route != null && route.hops.size() == 1) {
				oneHopNodes.add(addr);
			}
		}
		return oneHopNodes;
	}
	
	private Set<String> getMultiHopNodes() {
		Set<String> multiHopNodes = new HashSet<String>();
		Route route = null;
		for (String addr : routes.keySet()) {
			route = routes.get(addr);
			if (route != null && route.hops.size() > 1) {
				multiHopNodes.add(addr);
			}
		}
		return multiHopNodes;
	}
	
	private void setupGateway(Node node) {
		if (gatewayNode != null) {
			if (gatewayNode.addr.equals(node.addr)) {
				gatewayNode = node; // update expire time
				return;
			} else {
				teardownGateway();
			}
		}
		gatewayNode = node;
		
		/*
		// ip route add default via 192.168.11.100 dev eth0
		CoreTask.runRootCommand("ip route add default via " + node.addr + " dev " + manetcfg.getWifiInterface());
		*/
		CoreTask.setProp("net.dns1", node.dnsAddr);
		
		Log.d(MSG_TAG, "Setup gateway: " + gatewayNode.addr); // DEBUG
	}
	
	private void teardownGateway() {
		/*
		if (gatewayNode != null) {
			// ip route del default via 192.168.11.100 dev eth0
			CoreTask.runRootCommand("ip route del default via " + gatewayNode.addr + " dev " + manetcfg.getWifiInterface());
		}
		*/
		
		Log.d(MSG_TAG, "Flushed gateway: " + gatewayNode.addr); // DEBUG
		gatewayNode = null;
	}
	
	private void setupRoute(Node node, Route newRoute) {
		// check for route equality; for lists check if they contain the same entries in the same order
		if (routes.get(node.addr) != null) {
			if (routes.get(node.addr).equals(newRoute)) {
				routes.put(node.addr, newRoute); // update expire time
				return; // same route
			} else {
				teardownRoute(node, routes.get(node.addr));
			}
		}
		routes.put(node.addr, newRoute);
		
		// get first hop
		String hopAddr = newRoute.hops.get(0);
		
		if (node.isGateway) {
			// TODO: Removing the default gateway routing table entry causes a temporary delay in receiving incoming packets
			// which in turn can cause all edges and routes to expire at once.
			
			// ip route add default via 192.168.11.100 dev eth0
			CoreTask.runRootCommand("ip route add default via " + hopAddr + " dev " + manetcfg.getWifiInterface());
		} else {
			// ip route add 192.168.11.100/32 via 192.168.11.100 dev eth0
			CoreTask.runRootCommand("ip route add " + node.addr + "/32 via " + hopAddr + " dev " + manetcfg.getWifiInterface());
		}
		
		Log.d(MSG_TAG, "Setup route: " + newRoute); // DEBUG
	}
	
	private void teardownRoute(Node node, Route oldRoute) {
		if (oldRoute != null) {
			// get first hop
			String hopAddr = oldRoute.hops.get(0);
			
			if (node.isGateway) {
				// ip route del default via 192.168.11.100 dev eth0
				CoreTask.runRootCommand("ip route del default via " + hopAddr + " dev " + manetcfg.getWifiInterface());
			} else {
				// ip route del 192.168.11.100/32 via 192.168.11.100 dev eth0
				CoreTask.runRootCommand("ip route del " + node.addr + "/32 via " + hopAddr + " dev " + manetcfg.getWifiInterface());
			}
			
			routes.remove(node.addr);
			
			Log.d(MSG_TAG, "Flushed route: " + oldRoute); // DEBUG
		}
	}
	
	private class MessageBroadcastThread extends Thread {
		
		private DatagramSocket broadcastSocket = null;
		
		public void run() {
			try {				
				// broadcast group
				broadcastSocket = new DatagramSocket();
				broadcastSocket.setBroadcast(true);
				
				while (!isInterrupted()) {
					
					byte[] bytes = null;
					
					synchronized(myNode) {
						// set expire time
						myNode.expireTimestamp = 
							new Timestamp(Calendar.getInstance().getTimeInMillis() + EXPIRATION_TIME_MILLISEC);
						
						// create hello message
						HelloMessage helloMsg = new HelloMessage(myNode);
						
						// serialize
						ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
					    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
					    objectOutputStream.writeObject(helloMsg);
					    objectOutputStream.flush();
					    bytes = byteOutputStream.toByteArray();
					    objectOutputStream.close();
					}
					
					DatagramPacket packet = 
						new DatagramPacket(bytes, bytes.length, InetAddress.getByName(manetcfg.getIpBroadcast()), MESSAGE_PORT);
										
					// broadcast
					broadcastSocket.send(packet);

					Log.d(MSG_TAG, "Broadcasted: " + myNode.addr + " (" + bytes.length + " bytes)"); // DEBUG
					// Log.d(MSG_TAG, "Current info: " + getInfo()); // DEBUG
					
					Thread.sleep(BROADCAST_WAIT_TIME_MILLISEC);
				}
			} catch (InterruptedException e) {
				// do nothing
			} catch (Exception e) {
				handleException(e);
			} finally {
				if (broadcastSocket != null) {
					broadcastSocket.close();
				}
			}
    	}
	}
	
	private class MessageListenerThread extends Thread {
	    		 
		private DatagramSocket listenSocket = null;
		
    	public void run() {
    		
    		try {
    			listenSocket = new DatagramSocket(MESSAGE_PORT); // bind
				listenSocket.setSoTimeout(LISTEN_SOCKET_TIMEOUT_MILLISEC);

				byte[] buff = new byte[MAX_MESSAGE_LENGTH];
								
				while (!isInterrupted()) {
					DatagramPacket rxPacket = new DatagramPacket(buff, buff.length);
										
					try {
						listenSocket.receive(rxPacket); // blocks until timeout or socket closed
						
						// senderAddr = packet.getAddress().getHostAddress(); // this is the broadcast address!
						String senderAddr = ((InetSocketAddress)rxPacket.getSocketAddress()).getHostName().toString();
						
						ByteArrayInputStream byteInputStream = null;
						ObjectInputStream objectInputStream = null;
						HelloMessage helloMsg = null;
						
						try {
							// deserialize
							byteInputStream = new ByteArrayInputStream(rxPacket.getData());
							objectInputStream = new ObjectInputStream(byteInputStream);
							helloMsg = (HelloMessage)objectInputStream.readObject();
						} catch (EOFException e) {
							e.printStackTrace(); // sometimes this happens over an unreliable network
						} finally {
							objectInputStream.close();
						}
						
						if (helloMsg != null) {
							MessageProcessorThread messageProcessorThread = new MessageProcessorThread(senderAddr, helloMsg);
							messageProcessorThreads.add(messageProcessorThread);
							messageProcessorThread.start();
						}
						
						// Thread.yield(); // give other threads a chance to stop this one
					} catch (SocketTimeoutException e) {
						// do nothing; will occur on receive() timeout
					}
				} // while
			} catch (Exception e) {
				handleException(e);
    		} finally {
            	if (listenSocket != null) {
            		listenSocket.close(); // end receive() block
            	}
			}
    	}
    }
	
	private class MessageProcessorThread extends Thread {
		
		private String senderAddr = null;
		private HelloMessage helloMsg = null;
		
		private DatagramSocket rebroadcastSocket = null;
		
		public MessageProcessorThread(String senderAddr, HelloMessage helloMsg) {
			this.senderAddr = senderAddr;
			this.helloMsg = helloMsg;
		}
		
		public void run() {
						
			try {				
				int hash = helloMsg.hashCode();
				byte[] bytes = null;
				
				String content = "Received: " + hash + " (sender: " + senderAddr + 
						", node: " + helloMsg.node.addr + ", numHops: " + helloMsg.numHops + ")";
				helper.updateLog(content);
				Log.d(MSG_TAG, content); // DEBUG
				
				synchronized (myNode) {
					
					// ignore our own broadcasts; ignore repeat broadcasts; ignore messages from peers on the ignore list
					if (senderAddr.equals(myNode.addr) ||
							manetcfg.getRoutingIgnoreList().contains(senderAddr)) {
						return;
					}
				
					handleHelloMessage(senderAddr, helloMsg);
					
					// always handle messages about us to determine direct links
					if (!helloMsg.node.equals(myNode) && !processedMsgs.contains(hash)) {
						processedMsgs.add(hash);
						
						// rebroadcast
						helloMsg.numHops++;
						
						ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
					    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
					    objectOutputStream.writeObject(helloMsg);
					    objectOutputStream.flush();
					    bytes = byteOutputStream.toByteArray();
					    objectOutputStream.close();
					}
				}
				
				// rebroadcast
				if (bytes != null) {
					DatagramPacket txPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(manetcfg.getIpBroadcast()), MESSAGE_PORT);
					
	    			rebroadcastSocket = new DatagramSocket();
	    			rebroadcastSocket.setBroadcast(true);
	    			
					rebroadcastSocket.send(txPacket);
					
					// Log.d(MSG_TAG, "Rebroadcasted: " + helloMsg.hashCode() + " (node: " + helloMsg.node.addr + ", numHops: " + helloMsg.numHops + ")"); // DEBUG
				}
			} catch (Exception e) {
				handleException(e);
    		} finally {
            	if (rebroadcastSocket != null) {
            		rebroadcastSocket.close();
            	}
            	messageProcessorThreads.remove(this);
			}
		}
		
		private void handleHelloMessage(String senderAddr, HelloMessage helloMsg) {
			
			Node node = helloMsg.node;
			
			if (node.addr.equals(myNode.addr)) {
				
				// if a neighbor rebroadcasted our message, we can reach them directly
				if (helloMsg.numHops == 2) {
					Timestamp expireTimestamp =
						new Timestamp(Calendar.getInstance().getTimeInMillis() + EXPIRATION_TIME_MILLISEC);
					int cost = 1; // TODO: calculate cost based on GPS locality, etc.
						
					Edge edge = new Edge(senderAddr, cost, expireTimestamp);
					myNode.edges.add(edge); // from me to neighbor
					
					Log.d(MSG_TAG, "Added edge: " + myNode.addr + " -> " + senderAddr); // DEBUG
					
					/*
					// plan route
					Node senderNode = nodes.get(senderAddr);
					if (senderNode != null) {
						Route route = Dijkstra.findShortestRoute(myNode, senderNode, nodes); // may be null
						if (route != null) {
							setupRoute(senderNode, route);
						}
					} // else, delay planning route until sender node info received
					*/
				}
				
			} else {
				// insert or update existing node
				nodes.put(node.addr, node);
									
				// check if neighbor can reach me directly
				// eventually this edge will be replaced by an update from our neighbor
				// if (senderAddr.equals(node.addr)) {
					
					Timestamp expireTimestamp =
						new Timestamp(Calendar.getInstance().getTimeInMillis() + EXPIRATION_TIME_MILLISEC);
					int cost = 1; // TODO: calculate cost based on GPS locality, etc.
					
					Edge edge = new Edge(myNode.addr, cost, expireTimestamp);
					nodes.get(node.addr).edges.add(edge); // from neighbor to me
					
					Log.d(MSG_TAG, "Added edge: " + node.addr + " -> " + myNode.addr); // DEBUG
					
				// }
				
				/*
				// plan route
				Route route = Dijkstra.findShortestRoute(myNode, node, nodes); // may be null
				if (route != null) {
					setupRoute(node, route);
				}
				*/
				
				// setup gateway; TODO: handle multiple gateways
				if (node.isGateway) {
					setupGateway(node);
				}
			}
			
			// Log.d(MSG_TAG, "Handled: " + helloMsg.hashCode() + " (node: " + helloMsg.node.addr + ", numHops: " + helloMsg.numHops + ")"); // DEBUG
			// Log.d(MSG_TAG, "Current info: " + getInfo()); // DEBUG
		}
	}
	
	private class RoutingThread extends Thread {
		
		public void run() {
			try {
				while (!isInterrupted()) {
					
					synchronized (myNode) {
						Timestamp currTimestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
						
						// check if gateway expired
						if (gatewayNode != null) {
							if (gatewayNode.expireTimestamp.before(currTimestamp)) {
								teardownGateway();
							}
						}
						
						// check expired edges
						Set<Edge> set = null;
						Set<Edge> expiredEdges = null;
						for (String addr : nodes.keySet()) {
							set = nodes.get(addr).edges;
							if (!set.isEmpty()) {
								expiredEdges = new HashSet<Edge>();
								for (Edge edge : set) {
									if (edge.expireTimestamp.before(currTimestamp)) {
										Log.d(MSG_TAG, "Expired edge: " + addr + " -> " + edge.toAddr); // DEBUG
										expiredEdges.add(edge);
									}
								}
								set.removeAll(expiredEdges);
							}
						}
						
						// attempt to generate null or expired routes						
						Route oldRoute = null;
						Route newRoute = null;
						
						// plan routes (attempt to re-plan before expiring routes)
						for (String addr : nodes.keySet()) {
							if (!myNode.addr.equals(addr)) {
								newRoute = Dijkstra.findShortestRoute(myNode, nodes.get(addr), nodes); // may be null
								if (newRoute != null) {
									setupRoute(nodes.get(addr), newRoute);
								}
							}
						}
						
						// check expired routes
						Map<String, Route> expiredRoutes = new HashMap<String, Route>();
						for (String addr : routes.keySet()) {
							oldRoute = routes.get(addr);
							if (oldRoute.expireTimestamp.before(currTimestamp)) {
								expiredRoutes.put(addr, oldRoute);
							}
						}
						for (String addr : expiredRoutes.keySet()) {
							teardownRoute(nodes.get(addr), expiredRoutes.get(addr));
						}
					}
				
					Thread.sleep(ROUTE_GENERATION_WAIT_TIME_MILLISEC);
				}
			} catch (InterruptedException e) {
				// do nothing
			} catch (Exception e) {
				handleException(e);
			}
    	}
	}
	
	/*
	private class TimestampComparator implements Comparator<RoutingEntry> {

		@Override
		public int compare(RoutingEntry lhs, RoutingEntry rhs) {
			// sort such that the entry that will expire in the most distant future is returned first 
			return rhs.getTimestamp().compareTo(lhs.getTimestamp());
		}	
	}
	*/
}
