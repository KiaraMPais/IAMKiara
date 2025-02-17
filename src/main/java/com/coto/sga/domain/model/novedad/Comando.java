package com.coto.sga.domain.model.novedad;

import java.util.ArrayList;
import java.util.List;

import com.coto.sga.domain.model.operacion.OperacionUsuario;

/**
 * Representa una accion a realizarse sobre un determinado usuario y en distintas aplicaciones.
 */
public class Comando {

	private String commandtype;
	
	private List<OperacionUsuario> operacionesUsuario;

	
	public Comando() {
		operacionesUsuario=new ArrayList<OperacionUsuario>();
	}
	
	public void ejecutar() throws Exception{
		for (OperacionUsuario operacion : operacionesUsuario) {
			operacion.ejecutar();
		}
	}

	public String getCommandtype() {
		return commandtype;
	}

	public void setCommandtype(String commandtype) {
		this.commandtype = commandtype;
	}
	
	public void agregarOperacionUsuario(OperacionUsuario operacionUsuario){
		operacionesUsuario.add(operacionUsuario);
	}
	
}
