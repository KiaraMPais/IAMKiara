package com.coto.sga.application;

/**
 * Servicio que se encarda de procesar las novedades.
 */
public interface OperacionService {

	/**
	 * Obtiene las operaciones Usuario, las ejecuta y graba en el LOG los errores.
	 * Recibe como parametro la instancia del proceso a tomar.   
	 */
	public void procesarNovedades(Long instanciaProceso);
	
}
