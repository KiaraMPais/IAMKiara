package com.coto.sga.application;
 
 
public class SgaClient1 {
 
 
    public static void main(String[] args) {         
         String xml1 ="<?xml version=1.0 encoding=utf-8?>"
                      +"<exchange>"
            		  + " <userAccount>"
            		  + "  <user><![CDATA[606038]]></user>"
            		  + "</userAccount>"
            		  + "<ERROR>"
            		  + "  <CODIGO>1</CODIGO>"
            		  + "  <MENSAJE><![CDATA[Error de conexión al servidor remoto ntsxchcas1.redcoto.com.ar. Mensaje de error: The WS-Management service cannot process the request. This user is allowed a maximum number of 18 concurrent shells, which has been exceeded. Close existing shells or raise the quota for this user. Para obtener más información, consulte el tema de la Ayuda about_Remote_Troubleshooting.]]></MENSAJE>"
            		  + "</ERROR>"
            		  + " </exchange>";
 try{	
	  System.out.println(xml1);
	    int inicio = xml1.indexOf("<CODIGO>"); 
	    int fin = xml1.indexOf("</CODIGO>"); 
	    String codigo=xml1.substring(inicio, fin+9);
	    System.out.println(codigo);
	    int inicioCodigo = xml1.indexOf("<MENSAJE>"); 
	    int finCodigo = xml1.indexOf("</MENSAJE>"); 
	    String mensaje=xml1.substring(inicioCodigo, finCodigo+10);
	    System.out.println(mensaje);
	    char[] toCharArray = codigo.toCharArray();
	    int valor=0;
	    for (int i = 0; i < codigo.length(); i++) {
	    	char caracter = toCharArray[i];
	    	if (Character.isDigit(caracter)) {
	    		valor=Integer.valueOf(""+caracter);
	    	     break;
	    	}
	    }
	 System.out.println("valor "+valor);	 	 
	 
    }catch(Exception ex){
          ex.printStackTrace();
    }
}
}