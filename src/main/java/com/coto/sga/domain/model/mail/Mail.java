package com.coto.sga.domain.model.mail;

import com.coto.sga.domain.model.usuario.UsuarioLDAP;

public class Mail {

	private String path;
	private String servidor;
	private Boolean mailInternoActivo;
	private Boolean mailExternoActivo;
	private String mail;
	private String maxsendSize;
	private String maxReceiveSize;
	private String retentionPolicy;
	private UsuarioLDAP usuario;
		
	public Mail(){}
		
	public String getEmailExterno() {
		if (!mailExternoActivo) return null;		
		return mail+"@coto.com.ar"; //piden que tenga el mismo usuario,usuario.getUser_oprid()+"@coto.com.ar";
	}
	
	public String getEmailInterno() {
		if (!mailInternoActivo) return null;
		return mail+"@"+usuario.getDominio();
	}

	public String getServidor() {
		return servidor;
	}

	public void setServidor(String servidor) {
		this.servidor = servidor;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public UsuarioLDAP getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioLDAP usuario) {
		this.usuario = usuario;
	}

	public Boolean getMailInternoActivo() {
		return mailInternoActivo;
	}

	public void setMailInternoActivo(Boolean mailInternoActivo) {
		this.mailInternoActivo = mailInternoActivo;
	}

	public Boolean getMailExternoActivo() {
		return mailExternoActivo;
	}

	public void setMailExternoActivo(Boolean mailExternoActivo) {
		this.mailExternoActivo = mailExternoActivo;
	}
	
	public String getMail() {
		return mail;
	}
	
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getMaxsendSize() {
		return maxsendSize;
	}

	public void setMaxsendSize(String maxsendSize) {
		this.maxsendSize = maxsendSize;
	}

	public String getMaxReceiveSize() {
		return maxReceiveSize;
	}

	public void setMaxReceiveSize(String maxReceiveSize) {
		this.maxReceiveSize = maxReceiveSize;
	}
		
	public String getRetentionPolicy() {
		return retentionPolicy;
	}

	public void setRetentionPolicy(String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}

	public boolean equals(Object arg0) {
		if (this==arg0) return true;
		if (!(arg0 instanceof Mail)) return false;
		Mail mail=(Mail) arg0;
		return this.mail.equals(mail.mail);
	}
}
