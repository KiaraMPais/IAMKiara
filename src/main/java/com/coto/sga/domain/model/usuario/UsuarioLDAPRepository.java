package com.coto.sga.domain.model.usuario;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.grupo.Grupo;

public interface UsuarioLDAPRepository {

	public void crear(UsuarioLDAP usuario) throws RepositoryException;
	
	public void modificarClaveYEstado(UsuarioLDAP usuarioAD) throws RepositoryException;
	
	public void accesoAlWebServiceExchange(UsuarioLDAP usuarioAD) throws RepositoryException;
	
	public void eliminar(UsuarioLDAP usuario) throws RepositoryException;
	
	public void actualizar(UsuarioLDAP usuario) throws RepositoryException;
	
	public void agregarUsuarioAGrupo(UsuarioLDAP usuario,Grupo grupo) throws RepositoryException;
	
	public void borrarUsuarioDeGrupo(UsuarioLDAP usuario, Grupo grupo) throws RepositoryException;
	
}
