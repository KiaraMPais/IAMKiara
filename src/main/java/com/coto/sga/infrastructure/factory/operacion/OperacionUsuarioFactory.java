package com.coto.sga.infrastructure.factory.operacion;

import com.coto.sga.domain.model.operacion.OperacionActualizar;
import com.coto.sga.domain.model.operacion.OperacionAgregar;
import com.coto.sga.domain.model.operacion.OperacionCrear;
import com.coto.sga.domain.model.operacion.OperacionEliminar;
import com.coto.sga.domain.model.operacion.OperacionSustraer;
import com.coto.sga.domain.model.operacion.OperacionUsuario;

/**
 * Factory para crear OperacionUsuario.
 */
public class OperacionUsuarioFactory {
	
	/**
	 * Recibe un tipo y devuelve la operacionUsuario correspondiente. 
	 * En caso de que el tipo no sea valido se devuelve NULL. 
	 */
	public OperacionUsuario crearOperacionUsuario(String commandType){
				
		if (commandType.equals("NEW")){
			return new OperacionCrear();
		}
		if (commandType.equals("DELETE")){
			return new OperacionEliminar();
		}
		if (commandType.equals("CHANGE")){
			return new OperacionActualizar();
		}
		if (commandType.equals("ADD")){
			return new OperacionAgregar();
		}
		if (commandType.equals("SUB")){
			return new OperacionSustraer();
		}
		
		return null;
	}
}



