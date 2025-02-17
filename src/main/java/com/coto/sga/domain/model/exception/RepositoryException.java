package com.coto.sga.domain.model.exception;

/**
 * Representa una Excepcion de un Repositorio.
 */
public class RepositoryException extends Exception{


	private static final long serialVersionUID = 1L;
	
	public RepositoryException(){
		super();
	}

	public RepositoryException(String msg) {
		super (msg);
	}
	
	public RepositoryException(String msg,Throwable e){
		super(msg,e);
	}
	
	public RepositoryException(Throwable e){
		super(e);
	}
	
}
