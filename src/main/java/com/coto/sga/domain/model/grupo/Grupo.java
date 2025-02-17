package com.coto.sga.domain.model.grupo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.usuario.UsuarioLDAP;

@Configurable("grupo")
public class Grupo {

	private List<UsuarioLDAP> usuarios;
	private String nombre;
	
	
	
	@Autowired
	@Transient
	private GrupoRepository grupoRepository;
	
	
	public Grupo() {
		usuarios=new ArrayList<UsuarioLDAP>();
	}
	
	public void crear() throws RepositoryException{
		grupoRepository.crear(this);
	}
	
	public void actualizar() throws RepositoryException{
		grupoRepository.actualizar(this);
	}
	
	public void eliminar() throws RepositoryException{
		grupoRepository.eliminar(this);
	}
	

	public void addUsuario(UsuarioLDAP usuario){
	    if (!usuarios.contains(usuario)){
	    	usuarios.add(usuario);	 
	        usuario.addGrupo( this );
	    }
	}

	public void removeUsuario(UsuarioLDAP usuario){
	    if (usuarios.contains(usuario)){
	    	usuarios.remove( usuario ); 
	    	usuario.removeGrupo( this );
	    }
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public final List<UsuarioLDAP> getUsuarios() {
		return usuarios;
	}

	public void setGrupoRepository(GrupoRepository grupoRepository) {
		this.grupoRepository = grupoRepository;
	}
	
	public boolean equals(Object arg0) {
		if (this==arg0) return true;		
		if (!(arg0 instanceof Grupo)) return false;
		Grupo grupo=(Grupo) arg0;
		return this.nombre.equalsIgnoreCase(grupo.nombre);
	}

	public void cargarPorNombre() throws Exception {
		Grupo grupo=grupoRepository.cargarPorNombre(nombre);
		this.copy(grupo);
	}
	
	public void copy(Grupo grupo){
		this.nombre=grupo.nombre;
		this.usuarios.addAll(grupo.usuarios);
	}
}
