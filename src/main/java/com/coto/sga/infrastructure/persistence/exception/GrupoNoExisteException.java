package com.coto.sga.infrastructure.persistence.exception;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.grupo.Grupo;

public class GrupoNoExisteException extends RepositoryException{

	
	private static final long serialVersionUID = 1L;
	
	private Grupo grupo;
	
	public GrupoNoExisteException(final Grupo grupo) {
		this.grupo=grupo;
	}
	
	public GrupoNoExisteException(final Grupo grupo,Throwable e) {
		super(e);
		this.grupo=grupo;
	}				
	
	public String getMessage() {
		return "El grupo:[" + grupo.getNombre() + "] no existe.";
	}	
}
