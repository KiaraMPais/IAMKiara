package com.coto.sga.domain.model.usuario;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.coto.sga.application.util.ConstantesLog;
import com.coto.sga.domain.model.grupo.Grupo;
import com.coto.sga.domain.model.mail.Mail;

/**
 * Representa un usuario del tipo de aplicacion LDAP
 */
@SuppressWarnings("serial")
@Configurable("usuarioLDAP")
public class UsuarioLDAP extends Usuario{
			
	@Autowired
	@Transient
	private UsuarioLDAPRepository repositorioUsuarioLDAP;
	
	public static final String SEPARADOR_RUTA_RELATIVA="/";
	private static final Logger logger = Logger.getLogger(UsuarioLDAP.class);	
	
	private boolean tieneInternet;
	private Mail mail;
	private String clave;
	private boolean activo;
    private boolean claveExpirada;
    private String rutaRelativa;// Ruta relativa al basePath en donde se crea el usuario.
    private List<Grupo> grupos;// Grupos a los que pertenece
    private String dominio;
	private int duracionClave;// [dias]
	private String prefijoNombreAMostrar;
	private boolean tieneVPN;
	private String email_text;
	private String createIn;
	private String logonName;
	
	private String sga_mail;
	private String sga_mail_ext;
	

    public UsuarioLDAP() {
    	this.activo=true;
        this.claveExpirada=true;
        this.grupos = new ArrayList<Grupo>();
        this.sga_appl_id="AD";
        this.duracionClave=45;  
        this.tieneVPN=false;
	}
    
   
    /**
     * Asigno el DNI a la clave si esta no esta indicada.
     */
    @Override
    public void setCot_nro_doc(String cot_nro_doc) {
    	if (this.clave==null) this.clave=cot_nro_doc;
    	super.setCot_nro_doc(cot_nro_doc);
    }
    
    /**
     * Devuelve el nombre a mostrar (sin resolver las variables internas).
     */ 
    public String getNombreAMostrar(){
    	if (prefijoNombreAMostrar==null || prefijoNombreAMostrar.isEmpty() || prefijoNombreAMostrar.trim().length()==0){
    		return this.getNombreCompleto();
    	}
    	return prefijoNombreAMostrar+" "+this.getNombreCompleto();
    }
    
	public void setRepositorioUsuarioLDAP(
			UsuarioLDAPRepository repositorioUsuarioLDAP) {
		this.repositorioUsuarioLDAP = repositorioUsuarioLDAP;
	}
	
	public void crear() throws Exception{
		logger.info("repositorioUsuarioLDAP:   "+repositorioUsuarioLDAP);
				
		repositorioUsuarioLDAP.crear(this);
		repositorioUsuarioLDAP.modificarClaveYEstado(this);
		
		if (this.tieneMail()){
			MDC.put(ConstantesLog.MDC_APLICACION,"EMAIL");
			repositorioUsuarioLDAP.accesoAlWebServiceExchange(this);
			MDC.put(ConstantesLog.MDC_APLICACION,this.getSga_appl_id());
		}
		
	}
	
	public void actualizar() throws Exception{
		repositorioUsuarioLDAP.actualizar(this);
	}
	
	public void eliminar() throws Exception{
		repositorioUsuarioLDAP.eliminar(this);
	}		
	
	/**
	 * Agrega los grupos que contiene el usuario.
	 */
	public void agregar() throws Exception{
		for (Grupo grupo : grupos) {
			repositorioUsuarioLDAP.agregarUsuarioAGrupo(this, grupo);			
		}
	}
	
	/**
	 * Remueve los grupos que contiene el usuario.
	 */ 
	public void sustraer() throws Exception{
		for (Grupo grupo : grupos) {
			repositorioUsuarioLDAP.borrarUsuarioDeGrupo(this, grupo);			
		}
	}

	public void addGrupo(Grupo grupo){
		if (!grupos.contains(grupo)) {
			grupos.add(grupo);
			grupo.addUsuario(this);
		}
	}

	public void removeGrupo(Grupo grupo){
		if (grupos.contains(grupo)) {
			grupos.remove(grupo);
			grupo.removeUsuario(this);
		}
	}   
	
	public boolean perteneceAGrupo(Grupo grupo){
		return grupos.contains(grupo);
	}
	
	public String[] getRutaRelativaSeparada(){
		return rutaRelativa.split(SEPARADOR_RUTA_RELATIVA);
	}
	
	public String[] getDominioSeparado(){
		return dominio.split("\\.");
	}
	
	public String getNombrePrincipal(){
		if (user_oprid==null) return null;
		return user_oprid+"@"+dominio;
	}
	
	public String getDescripcion(){
		return sga_puesto;
	}

	public boolean equals(Object arg0) {
		if (this==arg0) return true;
		if (!(arg0 instanceof Usuario)) return false;
		UsuarioLDAP usuario=(UsuarioLDAP) arg0;
		return this.emplid.equals(usuario.emplid);
	}
	
    public boolean isClaveExpirada() {
    	return claveExpirada;
    }

    public void setClaveExpirada(boolean claveExpirada) {
    	this.claveExpirada = claveExpirada;
    }

    public final List<Grupo> getGrupos() {
		return grupos;
	}

	public String getClave() {
    	return clave;
    }

    public void setClave(String clave) {
    	this.clave = clave;
    }

	public Mail getMail() {
		return mail;
	}

	public void setMail(Mail mail) {
		if (mail!=null) mail.setUsuario(this);
		this.mail = mail;
	}

	public boolean isTieneInternet() {
		return tieneInternet;
	}
	
	public void setTieneInternet(boolean tieneInternet) {
		this.tieneInternet = tieneInternet;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}		
	
	public void setGrupos(List<Grupo> grupos) {
		this.grupos = grupos;
	}		
	
	public void setRutaRelativa(String rutaRelativa) {
		this.rutaRelativa = rutaRelativa;
	}
	
	public String getRutaRelativa() {
		return rutaRelativa;
	}

	public String getDominio() {
		return dominio;
	}

	public void setDominio(String dominio) {
		this.dominio = dominio;
	}

	public int getDuracionClave() {
		return duracionClave;
	}
	
	public void setDuracionClave(int duracionClave) {
		this.duracionClave = duracionClave;
	}
	
	public String getPrefijoNombreAMostrar() {
		return prefijoNombreAMostrar;
	}
	
	public void setPrefijoNombreAMostrar(String prefijoNombreAMostrar) {
		this.prefijoNombreAMostrar = prefijoNombreAMostrar;
	}
	
	public boolean tieneMail(){
		if (mail==null) return false;
		return Boolean.TRUE.equals(mail.getMailInternoActivo()) || Boolean.TRUE.equals(mail.getMailExternoActivo());		
	}
	
	public void setTieneVPN(boolean tieneVPN) {
		this.tieneVPN = tieneVPN;
	}
	
	public boolean isTieneVPN(){
		return this.tieneVPN;
	}

	@Override
	public boolean isAplicacionCritica() {		
		return true;
	}
	
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
	
}