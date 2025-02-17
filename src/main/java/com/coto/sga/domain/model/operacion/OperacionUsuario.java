package com.coto.sga.domain.model.operacion;

import com.coto.sga.domain.model.usuario.Usuario;


/**
 * Representa una operacion con el usuario.
 */
public abstract class OperacionUsuario {
	
	protected Usuario usuario;
	
	public abstract void ejecutar() throws Exception;
	
	public boolean isCritica(){
		return usuario.isAplicacionCritica();
	}

	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
}
