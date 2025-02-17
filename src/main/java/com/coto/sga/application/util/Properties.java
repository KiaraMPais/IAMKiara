package com.coto.sga.application.util;

import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Properties{
	
	private static final String BUNDLE_NAME = "parametros";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Properties(){}
	
	
	public static String getString(String key){
		try{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e){
			return null;
		}
	}
	
	public static List<String> getStringList(String key){
		try{			 			 
			return Arrays.asList(RESOURCE_BUNDLE.getStringArray(key));
		}
		catch (MissingResourceException e){
			return null;
		}
	}
	
}

