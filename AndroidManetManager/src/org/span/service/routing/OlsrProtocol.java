/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service.routing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.span.service.system.CoreTask;
import org.span.service.system.ManetConfig;


import android.util.Log;

public class OlsrProtocol extends RoutingProtocol {

	public static final String MSG_TAG = "ADHOC -> OlsrProtocol";
	
	public static final String NAME = "Optimized Link State Routing";
	
	private static final int WAIT_TIME_MILLISEC = 1000;
		
	// txtinfo plugin
	private static final String HOST = "localhost";
	private static final int PORT = 2006;
	
	private static final int CONNECT_WAIT = 25000;
		
	private static final String REQUEST_NEIGHS	 	= "/nei";
	private static final String REQUEST_LINKS	 	= "/lin";
	private static final String REQUEST_ROUTES	 	= "/rou";
	private static final String REQUEST_HNAS	 	= "/hna";
	private static final String REQUEST_MIDS	 	= "/mid";
	private static final String REQUEST_TOPOLOGY	= "/top";
	
	private static final String REQUEST_GATEWAY		= "/gat";
	private static final String REQUEST_CONFIG 		= "/con";
	private static final String REQUEST_INTERFACES 	= "/int";
	private static final String REQUEST_2HOP	 	= "/2ho"; // includes /nei info
	
	// includes /nei and /lin
	private final String REQUEST_NEIGHBORS = "/neighbors";
	
    // includes /nei, /lin, /rou, /hna, /mid, /top
	private final String REQUEST_ALL = "/all";
	
	// private final String REQUEST_DEFAULT = REQUEST_NEIGHBORS;
	private final String REQUEST_DEFAULT =
			REQUEST_LINKS + REQUEST_ROUTES + REQUEST_HNAS + REQUEST_MIDS + REQUEST_TOPOLOGY +
			REQUEST_GATEWAY + REQUEST_INTERFACES + REQUEST_2HOP;
	
	private Process olsrdProcess = null;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public boolean start(ManetConfig manetcfg) {
				
		try {
			// set interface
			File olsrdcfgTemplateFile = new File(CoreTask.DATA_FILE_PATH + "/conf/olsrd.conf.tpl");
			BufferedReader reader = new BufferedReader(new FileReader(olsrdcfgTemplateFile));
			StringBuffer buff = new StringBuffer();
			
			String line = null;
			while((line = reader.readLine()) != null) {
				buff.append(line).append("\n");
			}
			reader.close();
			
			String content = buff.toString();
			content = content.replaceAll("@" + ManetConfig.WIFI_INTERFACE_KEY + "@", manetcfg.getWifiInterface());
			
			// HNA4
			if (!manetcfg.getGatewayInterface().equals(ManetConfig.GATEWAY_INTERFACE_NONE)) {
				content = content.replaceAll("# " + "@" + ManetConfig.GATEWAY_INTERFACE_KEY + "@", "0.0.0.0 0.0.0.0");
			}
			
			File olsrdcfgFile = new File(CoreTask.DATA_FILE_PATH + "/conf/olsrd.conf");
			BufferedWriter writer = new BufferedWriter(new FileWriter(olsrdcfgFile));
			writer.write(content);
			writer.close();
			
			String command = CoreTask.DATA_FILE_PATH + "/bin/olsrd" +
					" -f " + CoreTask.DATA_FILE_PATH + "/conf/olsrd.conf" + 
					" -d 6" + 
					" -ignore " + CoreTask.DATA_FILE_PATH + "/conf/routing_ignore_list.conf";
			stop();
			
			// command = CoreTask.DATA_FILE_PATH + "/bin/olsrd -h"; // DEBUG
			
			olsrdProcess = CoreTask.runRootCommandInBackground(command);
			Thread.sleep(WAIT_TIME_MILLISEC); // wait for changes to take effect
	    	
		} catch(Exception e) {
			e.printStackTrace();
		}
		
    	return olsrdProcess != null;
    }
    
	@Override
    public boolean stop() {
    	try {
	    	if (olsrdProcess != null) {
	    		// TODO: we need to read the process output before destroying it
	    		// olsrdProcess.destroy(); // Note: this will not kill the process
	    		olsrdProcess = null;
	    	}
    		// check for olsrd process external to this app.
	    	CoreTask.killProcess("olsrd");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return true;
    }
	
	@Override
	public boolean isRunning() {
		try {
			return CoreTask.isProcessRunning(CoreTask.DATA_FILE_PATH + "/bin/olsrd");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public HashSet<Node> getPeers() {

		Set<String> peerAddresses = new HashSet<String>();
		
		try {
			// use route table because multi-hop peers won't be listed as links or neighbors
		    InfoThread infoThread = new InfoThread(REQUEST_ROUTES);
		    infoThread.start();
		    infoThread.join();
	
		    String error = infoThread.getError();
		    if(error == null) {
		    	String routeStr = infoThread.getInfo();
		    	routeStr = routeStr.substring(routeStr.indexOf("Table: Routes"));
		    	String[] routes = routeStr.split("\n");
		    	String peerAddress = null;
		    	String mask = null;
		    	String tokens[] = null;
		    	for (int i = 2; i < routes.length; i++) { // skip table name and table field names
		    		tokens = routes[i].split("\\s+")[0].split("/");
		    		peerAddress = tokens[0];
		    		mask = tokens[1];
		    		// ignore subnet routes
		    		if (mask.equals("32")) {
		    			peerAddresses.add(peerAddress);
		    		}
		    	}
		    }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// TODO: reimplement OLSR and store peer info in node form
		HashSet<Node> peers = new HashSet<Node>();
		for (String addr : peerAddresses) {
			peers.add(new Node(addr, null)); // no UID info
		}
		
		return peers;
	}
	
	/*
	@Override
	public Set<String> getBroadcastAddresses() {
		
		Set<String> broadcastAddresses = new HashSet<String>();
		
		try {
		    InfoThread infoThread = new InfoThread(REQUEST_INTERFACES);
		    infoThread.start();
		    infoThread.join();
	
		    String error = infoThread.getError();
		    if(error == null) {
		    	String interfaceStr = infoThread.getInfo();
		    	interfaceStr = interfaceStr.substring(interfaceStr.indexOf("Table: Interfaces"));
		    	String[] interfaces = interfaceStr.split("\n");
		    	String[] fields = null, subnetMaskOctets = null, subnetAddressOctets = null;
		    	String broadcastAddress = "";
		    	for (int i = 2; i < interfaces.length; i++) { // skip table name and table field names
		    		fields = interfaces[i].split("\\s+");
		    		subnetMaskOctets    = fields[5].split("\\.");
		    		subnetAddressOctets = fields[6].split("\\.");
		    		
		    		// int subnet = 0;
		    		for(int j = 0; j < subnetMaskOctets.length; j++) {
		    			if (subnetMaskOctets[j].equals("255")) {
		    				// subnet += 8;
		    				broadcastAddress += subnetAddressOctets[j];
		    			} else {
		    				broadcastAddress += "0";
		    			}
		    			broadcastAddress += ".";
		    		}
		    		
		    		broadcastAddress = broadcastAddress.substring(0, broadcastAddress.length()-1); // remove trailing '.'
		    		broadcastAddresses.add(broadcastAddress);
		    	}
		    }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return broadcastAddresses;
	}
	*/
	
	@Override
	public String getInfo() {
		if (!isRunning()) {
			return null;
		}
		
		String retval = null;
		try {
		    InfoThread infoThread = new InfoThread(REQUEST_DEFAULT);
		    infoThread.start();
		    infoThread.join();
	
		    String error = infoThread.getError();
		    if(error != null) {
		    	this.error = error;
		    } else {
		    	retval = infoThread.getInfo();
		    }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return retval;
	}
	
	@Override
	public String getError() {
		return this.error;
	}
	
	private class InfoThread extends Thread {
		
		private String request = null;
		private String error = null;
		private String info  = null;
		
		public InfoThread(String request) {
			this.request = request;
		}
		
		public String getError() {
			return error;
		}
		
		public String getInfo() {
			return info;
		}
		
	    public void run() {
	    	InetSocketAddress destination = null;
	       
	        try {
	        	// txtinfo plugin
	            destination = new InetSocketAddress(HOST, PORT);

            	Socket s = new Socket();
  	            //s.setSoTimeout(10000);
                s.connect(destination, CONNECT_WAIT);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                // String request = "/neighbours";
                out.write(request);
                out.flush();

                String line = null;
                StringBuffer responseBuff = new StringBuffer();
				while ((line = in.readLine()) != null) {
					responseBuff.append(line);
					responseBuff.append("\n");
				}
				
				info = responseBuff.toString().replace("\t", "     ");
				
	        } catch (Exception ex) {
				error = "Could net get routing info.";
	        	ex.printStackTrace();
	        }
	    }
	}
}
