package com.coto.sga.infrastructure.persistence.exception;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.usuario.UsuarioLDAP;

public class ClaveUsuarioException extends RepositoryException{

	
	private static final long serialVersionUID = 1L;
	
	private UsuarioLDAP usuario;
	
	public ClaveUsuarioException(final UsuarioLDAP usuario) {
		this.usuario=usuario;
	}
	
	public ClaveUsuarioException(final UsuarioLDAP usuario,Throwable e){
		super(e);
		this.usuario=usuario;
	}
	
	public String getMessage() {
		return "La clave del usuario:[" + usuario.getUser_oprid() + "] no es lo suficientemente fuerte.";
	}	
}
