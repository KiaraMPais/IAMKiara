package com.coto.sga.domain.model.operacion;


public class OperacionActualizar extends OperacionUsuario{
	
	public void ejecutar() throws Exception{
		usuario.actualizar();
	}

}
