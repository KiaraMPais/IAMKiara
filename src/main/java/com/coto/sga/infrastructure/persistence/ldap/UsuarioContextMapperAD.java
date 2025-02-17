package com.coto.sga.infrastructure.persistence.ldap;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.AbstractContextMapper;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.usuario.UsuarioLDAP;
import com.coto.sga.infrastructure.persistence.hibernate.NovedadRepositoryHibernateHelper;


public class UsuarioContextMapperAD extends AbstractContextMapper {
	private static final Logger logger = Logger.getLogger(UsuarioContextMapperAD.class);		
/**
 * 	 protected Object doMapFromContext(DirContextOperations context) {..} Es llamado cuando se realiza una baja
 */
    protected Object doMapFromContext(DirContextOperations context) {       
		UsuarioLDAP usuario=new UsuarioLDAP();
		logger.info("Ingresando a UsuarioContextMapperAD.doMapFromContext");
		String[] nombres=context.getStringAttribute("givenName").split(" ");
		if (nombres.length>0) usuario.setName1(nombres[0]);
		if (nombres.length>1) usuario.setName2(nombres[1]); 

		String[] apellidos=context.getStringAttribute("sn").split(" ");
		if (apellidos.length>0) usuario.setLast_name(apellidos[0]);
		if (apellidos.length>1) usuario.setLast_name2(apellidos[1]);

		usuario.setUser_oprid(context.getStringAttribute("sAMAccountName"));   // ESTA PARTE SE SACA PARA ADAM PERO VA A PRODUCCION
		usuario.setDeptid(context.getStringAttribute("department"));
		usuario.setEmplid(context.getStringAttribute("company"));
		usuario.setSga_puesto(context.getStringAttribute("title"));		
		// Ruta relativa
		String[] orgs=ActiveDirectoryUtils.getInstancia().getOusFrom(new DistinguishedName(context.getNameInNamespace()));
		
		StringBuffer rutaRelativa=new StringBuffer();
		for (int i=0;i<orgs.length-1;i++){			
			rutaRelativa.append(orgs[i]);
			rutaRelativa.append(UsuarioLDAP.SEPARADOR_RUTA_RELATIVA);
			rutaRelativa.append(orgs[orgs.length-1]);
		}
		
		usuario.setRutaRelativa(rutaRelativa.toString());
		logger.info("givenName= "+usuario.getName1()+usuario.getName2());
		logger.info("sn= "+usuario.getLast_name()+" "+usuario.getLast_name2());
		logger.info("sAMAccountName= "+usuario.getUser_oprid());
		logger.info("department= "+usuario.getDeptid());
		logger.info("company= "+usuario.getEmplid());
		logger.info("title= "+usuario.getSga_puesto());
		logger.info("context.getNameInNamespace()= "+context.getNameInNamespace());
		logger.info("rutaRelativa= "+usuario.getRutaRelativa());
        return usuario;
    }
    
    protected static void mapToContextBasic(UsuarioLDAP usuario,DirContextOperations context) throws UnsupportedEncodingException, RepositoryException {
    	context.setAttributeValue("displayName", NovedadRepositoryHibernateHelper.getInstancia().reemplazarVariables(usuario.getNombreAMostrar(),usuario));
    	context.setAttributeValue("department", usuario.getDeptid());
    	context.setAttributeValue("title", usuario.getSga_puesto());
    	// 20120816
//    	context.setAttributeValue("description", usuario.getDescripcion());    	
    }
    
	protected static void mapToContext(UsuarioLDAP usuario,DirContextOperations context) throws UnsupportedEncodingException, RepositoryException {				
		mapToContextBasic(usuario,context);		
		context.setAttributeValues("objectclass", new String[] {"person", "user"});		
		// SE COMENTA PARA ADAM PERO VA A PRODUCCION
		 context.setAttributeValue("userPrincipalName", usuario.getNombrePrincipal());
		 context.setAttributeValue("sAMAccountName", usuario.getUser_oprid()); //AD		
		context.setAttributeValue("givenName",usuario.getNombres());
		context.setAttributeValue("sn", usuario.getApellidos());	
		context.setAttributeValue("company", usuario.getEmplid());
		context.setAttributeValue("description", usuario.getDescripcion());
		
		mostrarDatos(usuario);	
/**	Se comenta esto Para que de alta del email, |,Si no es asi No dara el alta,Se lo hara con un WebService
		if (usuario.tieneMail()){
			logger.info("El Empleado: "+usuario.getEmplid()+" Tiene email"); 
			context.setAttributeValue("homeMDB",usuario.getMail().getPath());	
			context.setAttributeValue("mDBUseDefaults","TRUE");
			context.setAttributeValue("msExchHomeServerName",usuario.getMail().getServidor());
			context.setAttributeValue("mail",usuario.getMail().getEmailInterno());
			context.setAttributeValue("mailnickname",usuario.getMail().getMail());
		    mostrarDatosConEmail(usuario);
		}*/	
	}
		
	private static void mostrarDatos(UsuarioLDAP usuario) {
			logger.info("userPrincipalName= "+usuario.getNombrePrincipal());
			logger.info("sAMAccountName= "+usuario.getUser_oprid());
			logger.info("givenName= "+usuario.getNombres());
			logger.info("sn= "+usuario.getApellidos());
			logger.info("company= "+usuario.getEmplid());
			logger.info("description= "+usuario.getDescripcion());			
		}
	
	private static void mostrarDatosConEmail(UsuarioLDAP usuario) {
		logger.info("homeMDB = "+usuario.getMail().getPath());
		logger.info("msExchHomeServerName= "+usuario.getMail().getServidor());
		logger.info("mail= "+usuario.getMail().getEmailInterno());
		logger.info("mailnickname= "+usuario.getMail().getMail());
		logger.info("clave= "+usuario.getClave());
	}
}