package com.coto.sga.infrastructure.persistence.hibernate;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.usuario.UsuarioBDRepository;

/**
 * Hibernate implementation of UsuarioBDRepository.
 */
public class UsuarioBDRepositoryRoutingHibernateImpl extends HibernateRoutingRepository implements UsuarioBDRepository{
		
	public void ejecutar(String consulta,Object datos, String idAplicacion) throws RepositoryException{		
		SQLQuery q = (SQLQuery) getSession(idAplicacion).createSQLQuery(consulta.toLowerCase());		
		q.setProperties(datos);
		q.executeUpdate();
	}
	
	//TODO refactorizar con transacciones dinamicas.
	public void ejecutar(List<String> consultas,Object datos, String idAplicacion) throws RepositoryException{

		Session session=getSession(idAplicacion);
		Transaction tx=null;
		int numeroConsulta=0;
		
		try{
				tx=session.beginTransaction();
				
				for (String consulta : consultas){
					numeroConsulta++;
					reemplazarNombreTabla(consulta,datos);
					SQLQuery q = (SQLQuery) session.createSQLQuery(consulta.replaceAll("\\r\\n|\\r|\\n"," "));
					q.setProperties(datos);//Case sensitive
					q.executeUpdate();
				}
				
				tx.commit();
				
		}catch (Exception e) {
			if (tx!=null) tx.rollback();
			if (numeroConsulta==0) throw new RepositoryException("Hubo un error al conectarse a la aplicacion [" +idAplicacion+ "].",e);
			throw new RepositoryException("Hubo un error al ejecutar el Script numero " + numeroConsulta + ".",e);				
		}finally{
			if (session!=null){
				session.flush();
				session.close();
			}
		}
	}
	
	public void reemplazarNombreTabla(String consulta,Object datos){
		consulta=NovedadRepositoryHibernateHelper.getInstancia().reemplazarVariable(consulta,"sga_dbase",datos);
	}
}
