package com.coto.sga.domain.model.usuario;

import java.io.Serializable;



/**
 * Representa a un Usuario de un tipo de aplicacion (LDAP,BD).
 */
public abstract class Usuario implements Serializable{
			
	private static final long serialVersionUID = 1L;	
	
	// Los nombres de los atributos deben coincidir con las columnas de la Base de Datos
	protected String emplid;// Legajo
	protected String name1;// Primer nombre
	protected String name2;// Segundo nombre
	protected String last_name;// Primer apellido
	protected String last_name2;// Segundo apellido
	protected String cot_cuit;// CUIT
	protected String cot_nro_doc;// DNI
	protected String deptid;// Departamento
	protected String sga_puesto;// Puesto	
	protected String sga_appl_id;// Aplicacion
	protected String user_oprid; // Nombre de Usuario de Red
	// Campos creados el 20120816 segun DF 9824
	protected String email_text;
	protected String createIn;   // Carpeta AD
	protected String logonName;   // Logon
	
	protected String sga_mail;
	protected String sga_mail_ext;
	
	
	public abstract void crear() throws Exception;
	public abstract void actualizar() throws Exception;
	public abstract void eliminar() throws Exception;
	public abstract void agregar() throws Exception;
	public abstract void sustraer() throws Exception;
	
	/**
	 * Indica si la aplicacion a la cual pertenece el usuario es critica. 
	 */
	public abstract boolean isAplicacionCritica();
	
	
	public String getNombres(){
		if (name2==null || name2.isEmpty() || name2.trim().length()==0) return name1;
		return name1+" "+name2;
	}
	
	public String getApellidos(){
		if (last_name2==null || last_name2.isEmpty() || last_name2.trim().length()==0 ) return last_name;
		return last_name+" "+last_name2;
	}
	
	public String getNombreCompleto(){
		if (getNombres()==null) return getApellidos();
		if (getApellidos()==null) return getNombres();
		return getApellidos()+" "+getNombres();
	}		
	
	public String getEmplid() {
		return emplid;
	}
	
	public void setEmplid(String emplid) {
		this.emplid = emplid;
	}
	
	public String getName1() {
		return name1;
	}
	
	public void setName1(String name1) {
		this.name1 = name1;
	}
	
	public String getName2() {
		return name2;
	}
	
	public void setName2(String name2) {
		this.name2 = name2;
	}
	
	public String getLast_name() {
		return last_name;
	}
	
	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}
	
	public String getLast_name2() {
		return last_name2;
	}
	
	public void setLast_name2(String last_name2) {
		this.last_name2 = last_name2;
	}
	
	public String getCot_cuit() {
		return cot_cuit;
	}
	
	public String getSga_appl_id() {
		return sga_appl_id;
	}
	
	public void setSga_appl_id(String sga_appl_id) {
		this.sga_appl_id = sga_appl_id;
	}
	
	public String getSga_puesto() {
		return sga_puesto;
	}
	
	public void setSga_puesto(String sga_puesto) {
		this.sga_puesto = sga_puesto;
	}
	
	public void setCot_cuit(String cot_cuit) {
		this.cot_cuit = cot_cuit;
	}
	
	public String getCot_nro_doc() {
		return cot_nro_doc;
	}
	
	public void setCot_nro_doc(String cot_nro_doc) {
		this.cot_nro_doc = cot_nro_doc;
	}
	
	public String getDeptid() {
		return deptid;
	}
	
	public void setDeptid(String deptid) {
		this.deptid = deptid;
	}
	
	public String getUser_oprid() {
		return user_oprid;
	}
	
	public void setUser_oprid(String user_oprid) {
		this.user_oprid = user_oprid;
	}
	
	/** Campos nuevos segun DF 9824 */
	
	public String getEmail_text() {
		return email_text;
	}
	public void setEmail_text(String email_text) {
		this.email_text = email_text;
	}
	public String getCreateIn() {
		return createIn;
	}
	public void setCreateIn(String createIn) {
		this.createIn = createIn;
	}
	public String getLogonName() {
		return logonName;
	}
	public void setLogonName(String logonName) {
		this.logonName = logonName;
	}
	
	/** Campos nuevos segun DF 9824 */
	
	/** Campos nuevos segun DF 116094 */
	public String getSga_mail() {
		return sga_mail;
	}

	public void setSga_mail(String sga_mail) {
		this.sga_mail = sga_mail;
	}

	public String getSga_mail_ext() {
		return sga_mail_ext;
	}

	public void setSga_mail_ext(String sga_mail_ext) {
		this.sga_mail_ext = sga_mail_ext;
	}
	/** Campos nuevos segun DF 116094 */
	
}
