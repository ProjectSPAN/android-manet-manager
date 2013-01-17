/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
/**
 *  Portions of this code are copyright (c) 2009 Harald Mueller and Sofia Lemons.
 * 
 *  This program is free software; you can redistribute it and/or modify it under 
 *  the terms of the GNU General Public License as published by the Free Software 
 *  Foundation; either version 3 of the License, or (at your option) any later 
 *  version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with 
 *  this program; if not, see <http://www.gnu.org/licenses/>. 
 *  Use this application at your own risk.
 */
package org.span.service.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.span.service.CircularStringBuffer;
import org.span.service.routing.OlsrProtocol;
import org.span.service.routing.SimpleProactiveProtocol;


import android.util.Log;

public class CoreTask {

	public static final String TAG = "CoreTask";
	
	public static String DATA_FILE_PATH;
	
	// NOTE: Wait some time after setting radio mode for changes to take effect,
	// otherwise iwconfig might show "Managed" mode instead of "Ad-Hoc" mode.
	private static final int WAIT_TIME_MILLISEC = 2000;
	private static final int MAX_NUM_CHECKS = 3;
	
	private static final String FILESET_VERSION = "94";
	// private static final String defaultDNS1 = "208.67.220.220";
		
	public static void setPath(String path){
		DATA_FILE_PATH = path;
	}
	
    public static boolean chmod(String file, String mode) {
    	if (runCommand("chmod "+ mode + " " + file)) {
    		return true;
    	}
    	return false;
    }
    
    public static ArrayList<String> getNetworkInterfaces() {
    	ArrayList<String> interfaces = new ArrayList<String>();
    	
    	String output = runCommandGetOutput("netcfg");
    	System.out.println("getNetworkInterfaces(): " + output); // DEBUG
    	
    	String[] tokens = output.split("\n");
    	for(String token : tokens) {
    		interfaces.add(token.split(" ")[0]);
    	}

    	return interfaces;
    }
    
    public static boolean isNetworkInterfaceUp(String networkInterface) {
    	String output = runCommandGetOutput("netcfg");
    	
    	String[] tokens = output.split("\n");
    	for(String token : tokens) {
    		String[] subtokens = token.split("\\s+"); // any whitespace
    		if (subtokens[0].equals(networkInterface)) {
    			return subtokens[1].equals("UP");
    		}
    	}
    	
    	return false;
    }
    
    public static ArrayList<String> getRoutingProtocols() {
    	ArrayList<String> routingProtocols = new ArrayList<String>();
    	
    	routingProtocols.add(SimpleProactiveProtocol.NAME);
    	routingProtocols.add(OlsrProtocol.NAME);

    	return routingProtocols;
    }
    
    public static ArrayList<String> readLinesFromFile(String filename) {
    	String line = null;
    	BufferedReader br = null;
    	InputStream ins = null;
    	ArrayList<String> lines = new ArrayList<String>();
    	File file = new File(filename);
    	if (file.canRead() == false)
    		return lines;
    	try {
    		ins = new FileInputStream(file);
    		br = new BufferedReader(new InputStreamReader(ins), 8192);
    		while((line = br.readLine())!=null) {
    			lines.add(line.trim());
    		}
    	} catch (Exception e) {
    		Log.d(TAG, "Unexpected error - Here is what I know: "+e.getMessage());
    	}
    	finally {
    		try {
    			ins.close();
    			br.close();
    		} catch (Exception e) {
    			// Nothing.
    		}
    	}
    	return lines;
    }
    
    public static boolean writeLinesToFile(String filename, String lines) {
		OutputStream out = null;
		boolean returnStatus = false;
		Log.d(TAG, "Writing " + lines.length() + " bytes to file: " + filename);
		try {
			out = new FileOutputStream(filename);
        	out.write(lines.getBytes());
        	out.flush();
		} catch (Exception e) {
			Log.d(TAG, "Unexpected error - Here is what I know: "+e.getMessage());
		}
		finally {
        	try {
        		if (out != null)
        			out.close();
        		returnStatus = true;
			} catch (IOException e) {
				returnStatus = false;
			}
		}
		return returnStatus;
    }
    
    public static boolean isNatEnabled() {
    	ArrayList<String> lines = readLinesFromFile("/proc/sys/net/ipv4/ip_forward");
    	return lines.contains("1");
    }
    
    public static String getKernelVersion() {
        ArrayList<String> lines = readLinesFromFile("/proc/version");
        String version = lines.get(0).split(" ")[2];
        Log.d(TAG, "Kernel version: " + version);
        return version;
    }
    
	/*
	 * This method checks if netfilter/iptables is supported by kernel
	 */
    public static boolean isNetfilterSupported() {
    	if ((new File("/proc/config.gz")).exists() == false) {
	    	if ((new File("/proc/net/netfilter")).exists() == false)
	    		return false;
	    	if ((new File("/proc/net/ip_tables_targets")).exists() == false) 
	    		return false;
    	}
    	else {
            if (!DeviceConfig.hasKernelFeature("CONFIG_NETFILTER=") || 
                !DeviceConfig.hasKernelFeature("CONFIG_IP_NF_IPTABLES=") ||
                !DeviceConfig.hasKernelFeature("CONFIG_NF_NAT"))
            return false;
    	}
    	return true;
    }
    
    private static synchronized Hashtable<String,String> getRunningProcesses() {
    	File procDir = new File("/proc");
    	FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                try {
                    Integer.parseInt(name);
                } catch (NumberFormatException ex) {
                    return false;
                }
                return true;
            }
        };
        File[] processes = procDir.listFiles(filter);
        
    	Hashtable<String,String> tmpRunningProcesses = new Hashtable<String,String>();
    	for (File process : processes) {
    		String cmdLine = "";
			ArrayList<String> cmdlineContent = readLinesFromFile(process.getAbsoluteFile()+"/cmdline");
			if (cmdlineContent != null && cmdlineContent.size() > 0) {
				cmdLine = cmdlineContent.get(0);
			}
    		// Adding to tmp-Hashtable
    		tmpRunningProcesses.put(process.getAbsoluteFile().toString(), cmdLine);
    	}
    	return tmpRunningProcesses;
    }
    
    private static HashSet<String> getPids(String processName) throws Exception {
    	
    	String pid = null;
    	Hashtable<String,String> tmpRunningProcesses = getRunningProcesses();
    	HashSet<String> pids = new HashSet<String>();
    	String cmdLine = null;
    	for (String fileName : tmpRunningProcesses.keySet()) {
    		cmdLine = tmpRunningProcesses.get(fileName);
    			
    		// Checking if processName matches
    		if (cmdLine.contains(processName)) { // equals() / contains()
    			pid = fileName.substring(fileName.lastIndexOf(File.separatorChar)+1);
    			pids.add(pid);
    		}
    	}
    	return pids;
    }
    
    public static boolean isProcessRunning(String processName) throws Exception {
    	return !getPids(processName).isEmpty();
    }
    
    // TODO
    public static boolean killProcess(String processName) throws Exception {
    	// runRootCommand("killall " + processName); // requires busybox
    	HashSet<String> pids = getPids(processName);
    	for (String pid : pids) {
    		runRootCommand("kill -9 " + pid);
    	}
    	return true;
    }

    public static boolean hasRootPermission() {
    	boolean rooted = true;
		try {
			File su = new File("/system/bin/su");
			if (su.exists() == false) {
				su = new File("/system/xbin/su");
				if (su.exists() == false) {
					rooted = false;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Can't obtain root - Here is what I know: "+e.getMessage());
			rooted = false;
		}
		return rooted;
    }
    
    public static boolean startAdhocMode(ManetConfig manetcfg) {
    	try {
			if (runRootCommand(DATA_FILE_PATH + "/bin/adhoc start 1")) {
				// wait for changes to take effect
	    		for (int numChecks = 0; numChecks < MAX_NUM_CHECKS; numChecks++) {
	    			Thread.sleep(WAIT_TIME_MILLISEC);
	    			if (isAdHocModeEnabled(manetcfg)) {
	    				return true;
	    			}
	    		} 
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    public static boolean stopAdhocMode(ManetConfig manetcfg) {
    	try {
			if (runRootCommand(DATA_FILE_PATH + "/bin/adhoc stop 1")) {
				// wait for changes to take effect
				for (int numChecks = 0; numChecks < MAX_NUM_CHECKS; numChecks++) {
	    			Thread.sleep(WAIT_TIME_MILLISEC);
	    			if (!isAdHocModeEnabled(manetcfg)) {
	    				return true;
	    			}
	    		} 
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    public static boolean isAdHocModeEnabled(ManetConfig manetcfg) {
    	String wifiInterface = manetcfg.getWifiInterface();
    	if (!isNetworkInterfaceUp(wifiInterface)) {
    		return false;
    	}
    	// iwconfig process will bring interface back up (oh, Heisenbug)
    	String command = CoreTask.DATA_FILE_PATH + "/bin/iwconfig " + wifiInterface;
    	String output = runRootCommandGetOutput(command);
    	Log.d(TAG, output); // DEBUG
    	boolean isAdhoc = output.contains("Mode:Ad-Hoc");
    	boolean isEssid = output.contains("ESSID:\"" + manetcfg.getWifiSsid() + "\"");
    	return isAdhoc && isEssid;
    }
        
    public static String getProp(String property) {
    	// return System.getProperty(property);
    	return runRootCommandGetOutput("getprop " + property);
    }
    
    public static void setProp(String property, String value) {
    	// System.setProperty(property, value);
    	runRootCommand("setprop " + property + " " + value);
    }
    
    public static long[] getDataTraffic(String device) {
    	// Returns traffic usage for all interfaces starting with 'device'.
    	long [] dataCount = new long[] {0, 0};
    	if (device == "")
    		return dataCount;
    	for (String line : readLinesFromFile("/proc/net/dev")) {
    		if (line.startsWith(device) == false)
    			continue;
    		line = line.replace(':', ' ');
    		String[] values = line.split(" +");
    		dataCount[0] += Long.parseLong(values[1]);
    		dataCount[1] += Long.parseLong(values[9]);
    	}
    	//Log.d(TAG, "Data rx: " + dataCount[0] + ", tx: " + dataCount[1]);
    	return dataCount;
    }

    
    public static synchronized void updateDnsmasqFilepath() {
    	String dnsmasqConf = DATA_FILE_PATH + "/conf/dnsmasq.conf";
    	String newDnsmasq = new String();
    	boolean writeconfig = false;
    	
    	ArrayList<String> lines = readLinesFromFile(dnsmasqConf);
    	
    	for (String line : lines) {
    		if (line.contains("dhcp-leasefile=") && !line.contains(DATA_FILE_PATH)){
    			line = "dhcp-leasefile=" + DATA_FILE_PATH + "/var/dnsmasq.leases";
    			writeconfig = true;
    		}
    		else if (line.contains("pid-file=") && !line.contains(DATA_FILE_PATH)){
    			line = "pid-file=" + DATA_FILE_PATH + "/var/dnsmasq.pid";
    			writeconfig = true;
    		}
    		newDnsmasq += line+"\n";
    	}

    	if (writeconfig == true)
    		writeLinesToFile(dnsmasqConf, newDnsmasq);
    }
    
    /*
    public static synchronized String[] getCurrentDns() {
    	// Getting dns-servers
    	String dns[] = new String[2];
    	dns[0] = getProp("net.dns1");
    	dns[1] = getProp("net.dns2");
    	if (dns[0] == null || dns[0].length() <= 0 || dns[0].equals("undefined")) {
    		dns[0] = defaultDNS1;
    	}
    	if (dns[1] == null || dns[1].length() <= 0 || dns[1].equals("undefined")) {
    		dns[1] = "";
    	}
    	return dns;
    }
    */
    
    /*
    public static synchronized String[] updateResolvConf() {
    	String resolvConf = DATA_FILE_PATH+"/conf/resolv.conf";
    	// Getting dns-servers
    	String dns[] = getCurrentDns();
    	String linesToWrite = new String();
    	linesToWrite = "nameserver "+dns[0]+"\n";
    	if (dns[1].length() > 0) {
    		linesToWrite += "nameserver "+dns[1]+"\n";
    	}
    	writeLinesToFile(resolvConf, linesToWrite);
    	return dns;
    }    
    */
    
    public static boolean filesetOutdated(){
    	boolean outdated = true;
    	
    	File inFile = new File(DATA_FILE_PATH + "/conf/adhoc.edify");
    	if (inFile.exists() == false) {
    		return false;
    	}
    	ArrayList<String> lines = readLinesFromFile(DATA_FILE_PATH + "/conf/adhoc.edify");

    	int linecount = 0;
    	for (String line : lines) {
    		if (line.contains("@Version")){
    			String instVersion = line.split("=")[1];
    			if (instVersion != null && FILESET_VERSION.equals(instVersion.trim()) == true) {
    				outdated = false;
    			}
    			break;
    		}
    		if (linecount++ > 2)
    			break;
    	}
    	return outdated;
    }

    
    public static long getModifiedDate(String filename) {
    	File file = new File(filename);
    	if (file.exists() == false) {
    		return -1;
    	}
    	return file.lastModified();
    }
    
    /*
    public static synchronized boolean writeLanConf(String lanconfString) {
    	boolean writesuccess = false;
    	
    	String filename = null;
    	ArrayList<String> inputLines = null;
    	String fileString = null;
    	
    	// Assemble gateway-string
    	String[] lanparts = lanconfString.split("\\.");
    	String gateway = lanparts[0]+"."+lanparts[1]+"."+lanparts[2]+".254";
    	
    	// Assemble dnsmasq dhcp-range
    	String iprange = lanparts[0]+"."+lanparts[1]+"."+lanparts[2]+".100,"+lanparts[0]+"."+lanparts[1]+"."+lanparts[2]+".105,12h";
    	
    	// Update bin/blue_up.sh
    	fileString = "";
    	filename = DATA_FILE_PATH + "/bin/blue-up.sh";
    	inputLines = readLinesFromFile(filename);   
    	for (String line : inputLines) {
    		if (line.contains("ifconfig bnep0") && line.endsWith("netmask 255.255.255.0 up >> $adhoclog 2>> $adhoclog")) {
    			line = reassembleLine(line, " ", "bnep0", gateway);
    		}    		
    		fileString += line+"\n";
    	}
    	writesuccess = writeLinesToFile(filename, fileString);
    	if (writesuccess == false) {
    		Log.e(TAG, "Unable to update bin/adhoc with new lan-configuration.");
    		return writesuccess;
    	}
    	
    	// Update conf/dnsmasq.conf
    	fileString = "";
    	filename = DATA_FILE_PATH + "/conf/dnsmasq.conf";
    	inputLines = readLinesFromFile(filename);   
    	for (String line : inputLines) {
    		
    		if (line.contains("dhcp-range")) {
    			line = "dhcp-range="+iprange;
    		}    		
    		fileString += line+"\n";
    	}
    	writesuccess = writeLinesToFile(filename, fileString);
    	if (writesuccess == false) {
    		Log.e(TAG, "Unable to update conf/dnsmasq.conf with new lan-configuration.");
    		return writesuccess;
    	}    	
    	return writesuccess;
    }
    */
    
    public static String reassembleLine(String source, String splitPattern, String prefix, String target) {
    	String returnString = new String();
    	String[] sourceparts = source.split(splitPattern);
    	boolean prefixmatch = false;
    	boolean prefixfound = false;
    	for (String part : sourceparts) {
    		if (prefixmatch) {
    			returnString += target+" ";
    			prefixmatch = false;
    		}
    		else {
    			returnString += part+" ";
    		}
    		if (prefixfound == false && part.trim().equals(prefix)) {
    			prefixmatch = true;
    			prefixfound = true;
    		}

    	}
    	return returnString;
    }
    
    private static String prepareRootCommandScript(String command) {
    	try{ 
			Log.d(TAG, "Root command ==> " + command);
			
			// create a dummy script so that the user doesn't have to constantly accept the SuperUser prompt			
			File scriptFile = new File(DATA_FILE_PATH + "/tmp/command.sh");
			scriptFile.delete(); // clear out old content

			scriptFile = new File(scriptFile.getAbsolutePath());
			scriptFile.getParentFile().mkdirs();
			scriptFile.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
			writer.append(command);
			writer.close();
			
			// set executable permissions
			chmod(scriptFile.getAbsolutePath(), "0755");
			
			return "su -c \"" + scriptFile.getAbsolutePath() + "\"";
	    		    	
    	} catch(Exception e) {
        	e.printStackTrace();
    		return null;
    	}
    }
    
    public static boolean runRootCommand(String command) {
		return runCommand(prepareRootCommandScript(command));
    }
    
    public static boolean runCommand(String command) {
		try {
	    	Process process = Runtime.getRuntime().exec(command);
	    	return process.waitFor() == 0;
		} catch (Exception e) {
			return false;
		}
    }
    
    public static String runRootCommandGetOutput(String command) {
    	return runCommandGetOutput(prepareRootCommandScript(command));
    }
	
	public static String runCommandGetOutput(String command) {
		String output = "";
    	try {
			Process process = Runtime.getRuntime().exec(command);
			
			// we must empty the output and error stream to end the process
			EmptyStreamThread emptyInputStreamThread = 
				new EmptyInputStreamThread(process.getInputStream());
			EmptyStreamThread emptyErrorStreamThread = 
				new EmptyErrorStreamThread(process.getErrorStream());
			emptyInputStreamThread.start();
			emptyErrorStreamThread.start();
			
			if (process.waitFor() == 0) {
				// System.out.println("Successfully executed: " + command);
				emptyInputStreamThread.join();
				emptyErrorStreamThread.join();
				output = emptyErrorStreamThread.getOutput(); // DEBUG
				output = emptyInputStreamThread.getOutput();
			// } else {
				// System.err.println("Failed to execute: " + command);
			}
			
			// close streams
			process.getOutputStream().close();
			process.getInputStream().close();
			process.getErrorStream().close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public static Process runRootCommandInBackground(String command) {
		return runCommandInBackground(prepareRootCommandScript(command));
	}
	
	public static Process runCommandInBackground(String command) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			
			// we must empty the output and error stream to end the process
			EmptyStreamThread emptyInputStreamThread = 
				new EmptyInputStreamThread(process.getInputStream());
			EmptyStreamThread emptyErrorStreamThread = 
				new EmptyErrorStreamThread(process.getErrorStream());
			emptyInputStreamThread.start();
			emptyErrorStreamThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return process;
	}
	
	private static abstract class EmptyStreamThread extends Thread {
		
		private InputStream istream = null;
		private CircularStringBuffer buff = new CircularStringBuffer();
		
		public EmptyStreamThread(InputStream istream) {
			this.istream = istream;
		}
		
		public String getOutput() {
			return buff.toString().trim();
		}
		
		protected abstract void handleLine(String line);
		
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
				String line = null;
				while ((line = reader.readLine()) != null) { 
					handleLine(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					istream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class EmptyInputStreamThread extends EmptyStreamThread {
		
		public EmptyInputStreamThread(InputStream istream) {
			super(istream);
		}
		
		protected void handleLine(String line) {
			super.buff.append(line).append("\n");
			// Log.d(TAG, "OUTPUT: " + line); // DEBUG
		}
	}
	
	private static class EmptyErrorStreamThread extends EmptyStreamThread {
		
		public EmptyErrorStreamThread(InputStream istream) {
			super(istream);
		}
		
		protected void handleLine(String line) {
			super.buff.append(line).append("\n");
			// Log.d(TAG, "ERROR: " + line); // DEBUG
		}
	}
}
