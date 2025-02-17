package com.coto.sga.domain.model.operacion;

public class OperacionCrear extends OperacionUsuario{
				
	public void ejecutar() throws Exception{		
		usuario.crear();		
	}

}
