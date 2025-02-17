package com.coto.sga.domain.model.novedad;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.coto.sga.application.util.ConstantesLog;
import com.coto.sga.application.util.CorrectoLevel;
import com.coto.sga.application.util.Properties;
import com.coto.sga.domain.model.operacion.OperacionUsuario;

/**
 * Representa una Novedad de un determinado usuario.
 */
@Configurable(value="novedad")
public class Novedad implements Serializable{		

	private static final long serialVersionUID = 1L;
	
	//TODO el manejo del log deberia ser en capa de aplicacion.
	private static final Logger logger = Logger.getLogger(Novedad.class);
	
	
	// private Usuario usuario;
	// private Comando comando; // Tiene varias operaciones a hacer,cada operacion tiene el usuario.
	private String legajo;
	private Long numeroSolicitud;
	private Date addDttm;
	private EstadoNovedad estado;
	private Date lastUpdateDttm;
	private String opridLastUpdate;
	private List<OperacionUsuario> operaciones;
	private String comando;
	private Long instanciaProceso;
	private String emailText;
	private String createIn;
	private String logonName;
	private String sgaMail;
	private String sgaMailExt;
	
	@Autowired
	@Transient
	private NovedadRepository novedadRepository;
		
	public static final EstadoNovedad APROBADO=EstadoNovedad.A;
	public static final EstadoNovedad PROCESANDO=EstadoNovedad.P;
	public static final EstadoNovedad ERROR=EstadoNovedad.R;
	private final static String USUARIO_UPDATE=Properties.getString("usuario_sga");
	
	
	public Novedad() {
		operaciones=new ArrayList<OperacionUsuario>();		
	}
	
	public void agregarOperacionUsuario(OperacionUsuario operacionUsuario){
		operaciones.add(operacionUsuario);
	}
	
	public Long getNumeroSolicitud() {
		return numeroSolicitud;
	}

	public void setNumeroSolicitud(Long numeroSolicitud) {
		this.numeroSolicitud = numeroSolicitud;
	}

	public void procesar() throws Exception{
		
		MDC.put(ConstantesLog.MDC_NRO_SOLICITUD,numeroSolicitud);
		MDC.put(ConstantesLog.MDC_LEGAJO,legajo);
		
		// Modifico el estado a procesada.
		estado=PROCESANDO;
		lastUpdateDttm=new Date();
		opridLastUpdate=USUARIO_UPDATE;
		novedadRepository.actualizar(this);
		boolean error=false;
		logger.info("Procesando la novedad con numero ["+numeroSolicitud+"] para el usuario ["+legajo+"]");

		// Proceso las operaciones.
		for (OperacionUsuario operacion : operaciones){
			
			MDC.put(ConstantesLog.MDC_APLICACION,operacion.getUsuario().getSga_appl_id());
			logger.info("Ejecutando la operacion ["+comando+"] para el usuario ["+operacion.getUsuario().getEmplid()+"] en la aplicacion ["+operacion.getUsuario().getSga_appl_id()+"].");
			
			try{
				logger.info("operacion:   "+operacion);
				operacion.ejecutar();
				logger.log(CorrectoLevel.TRACE,"Finalizada correctamente.");
			}catch (Exception e) {
				error=true;
				e.printStackTrace();
				logger.error( (e.getMessage() == null ? e.getClass().getName() : e.getMessage() ) + " para aplicacion "+operacion.getUsuario().getSga_appl_id() ,e);
				if (operacion.isCritica()) break;
			}
		}
		
		if (error){
			// Marco la novedad con error.
			estado=ERROR;
			lastUpdateDttm =new Date();
			opridLastUpdate=USUARIO_UPDATE;
			novedadRepository.actualizar(this);
		}
	}

	public Date getAddDttm() {
		return addDttm;
	}
	public void setAddDttm(Date addDttm) {
		this.addDttm = addDttm;
	}
	
	public EstadoNovedad getEstado() {
		return estado;
	}

	public void setEstado(EstadoNovedad estado) {
		this.estado = estado;
	}

	public Date getLastUpdateDttm() {
		return lastUpdateDttm;
	}
	
	public void setLastUpdateDttm(Date lastUpdateDttm) {
		this.lastUpdateDttm = lastUpdateDttm;
	}
	
	public String getOpridLastUpdate() {
		return opridLastUpdate;
	}
	
	public void setOpridLastUpdate(String opridLastUpdate) {
		this.opridLastUpdate = opridLastUpdate;
	}

	public String getLegajo() {
		return legajo;
	}

	public void setLegajo(String legajo) {
		this.legajo = legajo;
	}
	
	public void setNovedadRepository(NovedadRepository novedadRepository) {
		this.novedadRepository = novedadRepository;
	}
	
	public boolean equals(Object arg0) {
		Novedad nov=(Novedad) arg0;
		return legajo.equals(nov.legajo) && numeroSolicitud.equals(nov.numeroSolicitud);		 
	}
		
	public int hashCode() {	
		return super.hashCode();
	}
	
	public void setComando(String comando) {
		this.comando = comando;
	}
	
	public String getComando() {
		return comando;
	}
	
	public void setInstanciaProceso(Long instanciaProceso) {
		this.instanciaProceso = instanciaProceso;
	}
	
	public Long getInstanciaProceso() {
		return instanciaProceso;
	}

	public List<OperacionUsuario> getOperaciones() {
		return operaciones;
	}

	public void setOperaciones(List<OperacionUsuario> operaciones) {
		this.operaciones = operaciones;
	}

	public String getEmailText() {
		return emailText;
	}

	public void setEmailText(String emailText) {
		this.emailText = emailText;
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

	public String getSgaMail() {
		return sgaMail;
	}

	public void setSgaMail(String sgaMail) {
		this.sgaMail = sgaMail;
	}

	public String getSgaMailExt() {
		return sgaMailExt;
	}

	public void setSgaMailExt(String sgaMailExt) {
		this.sgaMailExt = sgaMailExt;
	}

	public NovedadRepository getNovedadRepository() {
		return novedadRepository;
	}

}
