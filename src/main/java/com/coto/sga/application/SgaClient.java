package com.coto.sga.application;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.infrastructure.persistence.ldap.ObjError;


public class SgaClient {
    public static void main(String[] args) {
        try {
        	modificarClaveYEstado();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

	public static void modificarClaveYEstado() throws RepositoryException {
		ObjError retorno;
		try{         
			// Preparo la url para enviar al ws
			HttpClient httpClient = new DefaultHttpClient();        		     				
/*HttpGet msj = new HttpGet("http://w000sisda85/activedirectoryws/Service.asmx/addMailBox?key=123456789&"
		+ "userName=adtest&userPassword=20151703&userDomain=central&userActiveDirectoryAccount=606038");
		*/
			String URL="http://testing16/sgaws/service.asmx";
			String url=URL+"/"+"addMailBox";
	HttpGet msj = new HttpGet(url+"?key=123456789&"
			+ "userName=adtest&userPassword=20151703&userDomain=central&userActiveDirectoryAccount=606038");
					
	        HttpResponse respHttp = httpClient.execute(msj);
	        String respString = EntityUtils.toString(respHttp.getEntity());		        
	        System.out.println(respString);
	       
	        System.out.println(respString);
		    int inicio = respString.indexOf("<CODIGO>"); 
		    int fin = respString.indexOf("</CODIGO>"); 
		    String codigo=respString.substring(inicio, fin+9);
		    System.out.println(codigo);
		    int inicioCodigo = respString.indexOf("<MENSAJE>"); 
		    int finCodigo = respString.indexOf("</MENSAJE>"); 
		    String mensaje=respString.substring(inicioCodigo, finCodigo+10);
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
		}
		catch (Exception ex)
		{
			 new ObjError(-1, ex.getMessage().toString());
		}
	}	

}