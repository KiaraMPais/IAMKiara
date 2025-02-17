package com.coto.sga.domain.model.grupo;

import com.coto.sga.domain.model.exception.RepositoryException;

public interface GrupoRepository {

	public void crear(Grupo grupo) throws RepositoryException;
	
	public void eliminar(Grupo grupo) throws RepositoryException;
	
	public void actualizar(Grupo grupo) throws RepositoryException;

	public Grupo cargarPorNombre(String nombre) throws RepositoryException;
	
}
