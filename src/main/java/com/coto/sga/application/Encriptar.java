package com.coto.sga.application;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;


public class Encriptar {
    public static void main(String[] args) {
    String claveEncriptada;	
        try {
        	claveEncriptada=encrypt("Trintinama1");//Clave de Producción
        	claveEncriptada=encrypt("sgasgatest");//Clave de Testing
        	System.out.println("Clave encriptada: "+claveEncriptada);   
        	
         	claveEncriptada=decrypt(claveEncriptada);
        	System.out.println("Clave desencriptada: "+claveEncriptada); 
         	claveEncriptada=decrypt("g/D91XfuknsjQurW8R1Ma+fmXv/W5lhQ");
        	System.out.println("Clave desencriptada2: "+claveEncriptada); 
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String cadena) { 
    	StandardPBEStringEncryptor s = new StandardPBEStringEncryptor(); 
    	s.setPassword("sgarenace24"); 
    	s.setAlgorithm("PBEWithMD5AndDES");
    	return s.encrypt(cadena); 
    	} 
    
    public static String decrypt(String cadena) { 
    	StandardPBEStringEncryptor s = new StandardPBEStringEncryptor(); 
    	s.setPassword("SGASGA100"); 
    	s.setAlgorithm("PBEWithMD5AndDES");
    	String devuelve = ""; 
    	try { 
    	devuelve = s.decrypt(cadena); 
    	} catch (Exception e) { 
    	} 
    	return devuelve; 
    	} 

}