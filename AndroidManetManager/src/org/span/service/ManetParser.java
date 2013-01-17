package org.span.service;

import java.util.ArrayList;

public class ManetParser {

	public static ArrayList<String> parseOLSR(String data){
		if (data==null){
			System.err.println("Data String is null!");
			return null;
		}
		ArrayList<String> edges = new ArrayList<String>();	
	    String lines[] = data.split("\\r?\\n");
	    int index=-1;

	    for (int i=0; i<lines.length; i++) {
	      if (lines[i].contains("Table: Topology") ) {
	        index = i+2;
	      }
	    }
		
	    while ( lines[index].length () > 16) {
	      int wsIndex = lines[index].indexOf(' ');
	      int dsIndex = lines[index].indexOf(' ', wsIndex+6);
	      String dst = lines[index].substring(0, wsIndex);
	      String src = lines[index].substring(wsIndex+5, dsIndex);
	      edges.add(src +">" + dst);
	      index++;
	    }
		return edges;
	}
	
	public static ArrayList<String> parseRoutingInfo(String data){
		if (data==null){
			System.err.println("Data String is null!");
			return null;
		}
		ArrayList<String> edges = new ArrayList<String>();	
	    String lines[] = data.split("\\r?\\n");
	    int index=-1;

	    for (int i=0; i<lines.length; i++) {
	    	//System.out.println(i + ": " + lines[i]);
	      if (lines[i].contains("Edges:") ) {
	        index = i+1;
	      }
	    }
		
	    while (index < lines.length) {
	    	if(lines[index].contains("none")) break;
	      int wsIndex = lines[index].indexOf(' ');
	      int dsIndex = lines[index].indexOf('-');
	      String src = lines[index].substring(wsIndex+1, dsIndex-1);
	      String dst = lines[index].substring(dsIndex+3, lines[index].length());
	      edges.add(src +">" + dst);
	      index++;
	    }
		return edges;
	}


}
