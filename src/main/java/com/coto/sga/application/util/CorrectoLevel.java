package com.coto.sga.application.util;

import org.apache.log4j.Level;

@SuppressWarnings("serial")
public class CorrectoLevel extends Level {


	/**  
	 * Value of my trace level. This value is lesser than  
	 * {@link org.apache.log4j.Priority#DEBUG_INT}  
	 * and higher than {@link org.apache.log4j.Level#TRACE_INT}  
	 */   
	public static final int CORRECTO_INT = ERROR_INT; // DEBUG_INT - 10; 
	
	private static final String NOMBRE="CORRECTO";

	public static final Level TRACE = new CorrectoLevel(CORRECTO_INT,NOMBRE,11);//7  


	protected CorrectoLevel(int level, String levelStr, int syslogEquivalent){
		super(level, levelStr, syslogEquivalent);
	}
}