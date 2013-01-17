/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service;

import java.lang.StringBuffer;

// BASED ON: http://stackoverflow.com/questions/7461448/fixed-length-stringbuffer-in-java
public class CircularStringBuffer {
	
	private static final int MAX_LENGTH = 10000;
	
	// NOTE: StringBuffer is a final class and cannot be extended
	private StringBuffer buffer = new StringBuffer();
	
	@Override
	public String toString() {
		return buffer.toString();
	}
	
	public int length() {
		return buffer.length();
	}
	
	public boolean isEmpty() {
		return buffer.length() == 0;
	}

	public CircularStringBuffer append(String s) {     
		if (buffer.length() + s.length() > MAX_LENGTH) {         
			buffer.delete(0, buffer.length() + s.length() - MAX_LENGTH);     
		}     
		buffer.append(s, Math.max(0, s.length() - MAX_LENGTH), s.length());
		return this;
	}
	
	public void clear() {
		buffer.delete(0, buffer.length());
	}
}
