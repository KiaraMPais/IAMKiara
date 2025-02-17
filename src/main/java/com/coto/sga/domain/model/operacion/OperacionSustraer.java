package com.coto.sga.domain.model.operacion;


public class OperacionSustraer extends OperacionUsuario{
	
	public void ejecutar() throws Exception{
		usuario.sustraer();
	}

}
