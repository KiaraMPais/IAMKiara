package com.coto.sga.domain.model.usuario;

import java.util.List;

import com.coto.sga.domain.model.exception.RepositoryException;

public interface UsuarioBDRepository {
	
	/**
	 * Ejecuta las consultas en la aplicacion dada, tomando los valores de las variables del objeto datos.
	 */
	public void ejecutar(List<String> consultas,Object datos, String idAplicacion) throws RepositoryException;
}
