package com.coto.sga.domain.model.usuario;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Representa un Usuario de un tipo de aplicacion Base de Datos.
 */
@SuppressWarnings("serial")
@Configurable("usuarioBD")
public class UsuarioBD extends Usuario{
		
	private List<String> consultas;
	private String sga_perfil;
	private String sga_dbase;
	
	@Autowired
	@Transient
	private UsuarioBDRepository usuarioBDRepository;
		

	public UsuarioBD(){			
		consultas=new ArrayList<String>();
	}
	

	private void ejecutar() throws Exception{
		usuarioBDRepository.ejecutar(consultas,this,this.sga_appl_id);
	}

	@Override
	public void crear() throws Exception{
		ejecutar();
	}
	
	@Override
	public void actualizar() throws Exception{
		ejecutar();
	}
	
	@Override
	public void eliminar() throws Exception{
		ejecutar();
	}
	
	@Override
	public void agregar() throws Exception {
		ejecutar();
	}

	@Override
	public void sustraer() throws Exception {
		ejecutar();
	}
	
	public void agregarConsulta(String consulta){
		consultas.add(consulta);
	}
		
	public final List<String> getConsultas() {
		return consultas;
	}
	
	public void setUsuarioBDRepository(UsuarioBDRepository usuarioBDRepository) {
		this.usuarioBDRepository = usuarioBDRepository;
	}
		
	public String getSga_perfil() {
		return sga_perfil;
	}
		
	public void setSga_perfil(String sga_perfil) {
		this.sga_perfil=sga_perfil;
	}
	
	public void setConsultas(List<String> consultas) {
		this.consultas = consultas;
	}

	public String getSga_dbase() {
		return sga_dbase;
	}
	
	public void setSga_dbase(String sga_dbase) {
		this.sga_dbase = sga_dbase;
	}

	@Override
	public boolean isAplicacionCritica() {		
		return false;
	}
}
