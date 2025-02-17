package com.coto.sga.domain.model.operacion;


public class OperacionAgregar extends OperacionUsuario{
	
	public void ejecutar() throws Exception{
		usuario.agregar();
	}

}
