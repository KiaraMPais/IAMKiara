package com.coto.sga.domain.model.novedad;

import java.util.List;

import com.coto.sga.domain.model.exception.RepositoryException;

public interface NovedadRepository {

	public void crear(Novedad novedad) throws RepositoryException;
	
	public void actualizar(Novedad novedad) throws RepositoryException;
	
	public void eliminar(Novedad novedad) throws RepositoryException;
	
	public List<Novedad> obtenerNovedadesAprobadas(long numeroInstancia) throws RepositoryException;
	
}
