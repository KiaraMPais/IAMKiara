package com.coto.sga.domain.model.exception;

public class ModelException extends Exception{


	private static final long serialVersionUID = 1L;
	
	public ModelException(){
		super();
	}

	public ModelException(String msg) {
		super (msg);
	}
	
}
