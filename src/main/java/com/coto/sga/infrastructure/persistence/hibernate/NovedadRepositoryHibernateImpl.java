package com.coto.sga.infrastructure.persistence.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.grupo.Grupo;
import com.coto.sga.domain.model.mail.Mail;
import com.coto.sga.domain.model.novedad.Novedad;
import com.coto.sga.domain.model.novedad.NovedadRepository;
import com.coto.sga.domain.model.operacion.OperacionUsuario;
import com.coto.sga.domain.model.usuario.Usuario;
import com.coto.sga.domain.model.usuario.UsuarioBD;
import com.coto.sga.domain.model.usuario.UsuarioLDAP;
import com.coto.sga.infrastructure.factory.operacion.OperacionUsuarioFactory;

@Transactional(value="transactionManagerPS",rollbackFor=Exception.class)
public class NovedadRepositoryHibernateImpl extends HibernateRepository implements NovedadRepository{
	//TODO el manejo del log deberia ser en capa de aplicacion.
   private static final Logger logger = Logger.getLogger(Novedad.class);
		
	@Autowired
	private OperacionUsuarioFactory operacionUsuarioFactory;
	
	
	public void setOperacionUsuarioFactory(OperacionUsuarioFactory operacionUsuarioFactory) {
		this.operacionUsuarioFactory = operacionUsuarioFactory;
	}
	
	@Override
	public void actualizar(Novedad novedad) throws RepositoryException {
		getSession().update(novedad);		
	}

	@Override
	public void crear(Novedad novedad) throws RepositoryException {
		getSession().save(novedad);		
	}

	@Override
	public void eliminar(Novedad novedad) throws RepositoryException {
		getSession().delete(novedad);		
	}

	
	@SuppressWarnings("unchecked")
	@Override	
	public List<Novedad> obtenerNovedadesAprobadas(long numeroInstancia) throws RepositoryException {
		System.out.println("Voy a Buscar las novedades a la BD");	
		List<Novedad> novedades = getSession().createCriteria(Novedad.class)
	    .add( Restrictions.eq("estado", Novedad.APROBADO))
	    .add( Restrictions.eq("instanciaProceso",new Long(numeroInstancia)))
	    .list();
		logger.info("numeroInstancia:"+numeroInstancia);
		logger.info("novedades:"+novedades.size());
		
		OperacionUsuario operacion=null;
		for (Novedad novedad : novedades) {
			for (Usuario usuario: obtenerUsuarios(novedad)) {
				logger.info("usuario getEmplid:"+usuario.getEmplid());
				operacion=operacionUsuarioFactory.crearOperacionUsuario(novedad.getComando());
				logger.info("operacion:"+operacion);
				if (operacion==null) continue;
				operacion.setUsuario(usuario);
				novedad.agregarOperacionUsuario(operacion);
			}
		}

		return novedades;
	}
	
	/*
	 * Obtiene los usuarios LDAP y BD a partir de la novedad.
	 */
	private List<Usuario> obtenerUsuarios(Novedad novedad) throws RepositoryException{
		List<Usuario> usuarios=new ArrayList<Usuario>();			
		usuarios.addAll(obtenerUsuariosLDAP(novedad));
		usuarios.addAll(obtenerUsuariosBD(novedad));
		return usuarios;
	}

	@SuppressWarnings("unchecked")
	private List<UsuarioLDAP> obtenerUsuariosLDAP(Novedad novedad) throws RepositoryException{						
		SQLQuery q=(SQLQuery) getSession().getNamedQuery("getUsuariosLDAP").setResultTransformer(Transformers.aliasToBean(UsuarioLDAP.class));		
		q.setParameter("nroSolicitud",novedad.getNumeroSolicitud());
		q.setParameter("legajo",novedad.getLegajo());
		List<UsuarioLDAP> usuarios=q.list();
		
		
		List<Grupo> grupos=null;
		for (UsuarioLDAP usuario : usuarios) {
			
			// Cargo los grupos
			q=(SQLQuery) getSession().getNamedQuery("getGrupos").setResultTransformer(Transformers.aliasToBean(Grupo.class));
			q.setParameter("puesto",usuario.getSga_puesto());
			grupos=q.list();
			for (Grupo grupo : grupos) {
				//Reemplaza valor de nombre de grupo.
				grupo.setNombre(NovedadRepositoryHibernateHelper.getInstancia().reemplazarVariables(grupo.getNombre(),usuario));
				usuario.addGrupo(grupo);
			}
			
			//Cargo el mail
			q=(SQLQuery) getSession().getNamedQuery("getMail").setResultTransformer(Transformers.aliasToBean(Mail.class));			
			q.setParameter("nroSolicitud",novedad.getNumeroSolicitud());
			q.setParameter("legajo",novedad.getLegajo());
			usuario.setMail((Mail) q.uniqueResult());
		}

		return usuarios;
	}

	// Obtengo los usuariosBD asociados a la novedad.
	@SuppressWarnings({ "unchecked" })
	private List<UsuarioBD> obtenerUsuariosBD(Novedad novedad){
		
		SQLQuery q=(SQLQuery) getSession().getNamedQuery("getUsuariosBD").setResultTransformer(Transformers.aliasToBean(UsuarioBD.class));
		q.setParameter("nroSolicitud",novedad.getNumeroSolicitud());
		q.setParameter("legajo",novedad.getLegajo());
		List<UsuarioBD> usuarios=q.list();
		
		// Cargo las consultas
		for (UsuarioBD usuario : usuarios) {			
			q=(SQLQuery) getSession().getNamedQuery("getConsultasOrdenadas");
			q.setParameter("puesto",usuario.getSga_puesto());
			q.setParameter("comando",novedad.getComando());
			q.setParameter("perfil",usuario.getSga_perfil());
			q.setParameter("aplicacion",usuario.getSga_appl_id());
			usuario.setConsultas((List<String>) q.list());
		}
		
		return usuarios;		
	}

}
