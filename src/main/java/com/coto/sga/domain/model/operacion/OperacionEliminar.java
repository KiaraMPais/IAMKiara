package com.coto.sga.domain.model.operacion;

public class OperacionEliminar extends OperacionUsuario{

	public void ejecutar() throws Exception{
		usuario.eliminar();
	}
	
}
