package com.coto.sga.infrastructure.persistence.ldap;

public class ObjError {
	int codigo;
	String mensaje;
	
	
	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	
	
	public ObjError(int cod, String msj)
	{
        this.codigo = cod;
        this.mensaje = msj;
	}
}
