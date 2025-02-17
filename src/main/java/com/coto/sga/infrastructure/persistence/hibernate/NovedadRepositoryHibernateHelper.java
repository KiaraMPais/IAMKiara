package com.coto.sga.infrastructure.persistence.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.coto.sga.domain.model.exception.RepositoryException;

public class NovedadRepositoryHibernateHelper {

	private static final NovedadRepositoryHibernateHelper INSTANCIA = new NovedadRepositoryHibernateHelper();  
	public static NovedadRepositoryHibernateHelper getInstancia() { return INSTANCIA; }
	private NovedadRepositoryHibernateHelper() {}
	
	private static final String INDICADOR_VARIABLE=":";
	
	/**
	 * Reemplaza todas las variables que encuentra en la linea con el valor de la variable en
	 * datos. Acepta los valores de atributos heredados.
	 * No es Case Insensitive.
	 * 
	 * Datos no debe contener el sql ya que no es seguro (sql injection).
	 * 
	 */
	public String reemplazarVariables(String linea, Object datos) throws RepositoryException{		
		
		if (linea==null) return linea;
		
		if (linea.indexOf(INDICADOR_VARIABLE)==-1) return linea;// Si no hay posibles variables sale.
		
		try {
			String buscar=null;			
			List<Field> propiedades=new ArrayList<Field>();						
			propiedades.addAll(Arrays.asList(datos.getClass().getSuperclass().getDeclaredFields()));
			propiedades.addAll(Arrays.asList(datos.getClass().getDeclaredFields()));
						
			// Recorro todas las propiedades que existen en el objeto y busco si existen en variables.
			 for (Field propiedad : propiedades) {
				buscar=INDICADOR_VARIABLE+propiedad.getName().toLowerCase();
		
				Object resultado = invocarGet(datos, propiedad.getName());
				
				if (resultado!=null && resultado instanceof String){
					String lineaNueva=linea.replaceAll("(?i)"+buscar,resultado.toString());
					if (!lineaNueva.equals(linea)) return lineaNueva;
				}
			}
			 
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		return linea;
	}
	
	private Object invocarGet(Object datos, String nombreVariable) throws IllegalAccessException, InvocationTargetException {
		
		String nombreGet=null;
		if (nombreVariable.length()>1){					
			nombreGet=nombreVariable.substring(0,1).toUpperCase()+nombreVariable.substring(1).toLowerCase();					
		}
		
		Object resultado=null;
		//TODO recorrer recursivamente la jerarquia de clases para buscar el atributo.
		try{
			resultado=datos.getClass().getDeclaredMethod("get"+nombreGet,null).invoke(datos,null);					
		}catch (NoSuchMethodException e) {
			try {
				resultado=datos.getClass().getSuperclass().getDeclaredMethod("get"+nombreGet,null).invoke(datos,null);
			} catch (NoSuchMethodException e1) {
				resultado=null;				
			}
		}
		return resultado;
	}
	
	public String reemplazarVariable(String linea, String nombreVariable, Object datos){

		if (linea==null) return linea;
		
		if (linea.indexOf(INDICADOR_VARIABLE)==-1) return linea;// Si no hay posibles variables sale.
		
		try {
			String resultado = invocarGet(datos,nombreVariable).toString();
			resultado = reemplazarVariables(resultado,datos);
			return linea.replaceAll("(?i)"+INDICADOR_VARIABLE+nombreVariable.toLowerCase(),resultado);
		} catch (Exception e) {
			return linea;
		}
	}
}
