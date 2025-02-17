package com.coto.sga.infrastructure.persistence.ldap;

import java.io.UnsupportedEncodingException;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.grupo.Grupo;
import com.coto.sga.domain.model.grupo.GrupoRepository;


public class GrupoRepositoryImpl extends LDAPRepository implements GrupoRepository{
		

	@Override
	public void crear(Grupo grupo) throws RepositoryException {
		
		try {
			
			DirContextOperations context=new DirContextAdapter(getDnFrom(grupo.getNombre()));				
			GrupoContextMapper.mapToContext(grupo, context);
			ldapTemplate.bind(context);
			
		} catch (UnsupportedEncodingException e) {
			throw new RepositoryException(e.getMessage());
		}
	}
	
	@Override
	public void actualizar(Grupo grupo) throws RepositoryException {
		
		try{

			DirContextOperations context = ldapTemplate.lookupContext(getDnFrom(grupo.getNombre()));	    
			GrupoContextMapper.mapToContext(grupo, context);
			ldapTemplate.modifyAttributes(context);

		} catch (UnsupportedEncodingException e) {
			throw new RepositoryException(e.getMessage());
		}
	}
	
	@Override
	public void eliminar(Grupo grupo) throws RepositoryException {
		ldapTemplate.unbind(getDnFrom(grupo.getNombre()));
	}
	
	
	@Override
	public Grupo cargarPorNombre(String nombre){				
		return (Grupo) ldapTemplate.lookup(getDnFrom(nombre), new GrupoContextMapper());
	}

}
