package com.coto.sga.infrastructure.persistence.exception;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.grupo.Grupo;

public class GrupoDuplicadoException extends RepositoryException{

	
	private static final long serialVersionUID = 1L;
	
	private Grupo grupo;
	
	public GrupoDuplicadoException(final Grupo grupo) {
		this.grupo=grupo;
	}
	
	public GrupoDuplicadoException(final Grupo grupo,Throwable e) {
		super(e);
		this.grupo=grupo;
	}				
	
	public String getMessage() {
		return "El Grupo ["+grupo.getNombre()+"] no es unico.";
	}	
}
