package com.coto.sga.infrastructure.persistence.ldap;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.AbstractContextMapper;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.usuario.UsuarioLDAP;
import com.coto.sga.infrastructure.persistence.hibernate.NovedadRepositoryHibernateHelper;


public class UsuarioContextMapperADViejo extends AbstractContextMapper {
	private static final Logger logger = Logger.getLogger(UsuarioContextMapperADViejo.class);
	
    protected Object doMapFromContext(DirContextOperations context) {       
		UsuarioLDAP usuario=new UsuarioLDAP();
		logger.info("Ingresando a UsuarioContextMapperADAM.doMapFromContext Quiroz3");
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
		
		
		// Ruta relativa: TODO mal armado
		String[] orgs=ActiveDirectoryUtils.getInstancia().getOusFrom(new DistinguishedName(context.getNameInNamespace()));
		//ArrayUtils.reverse(orgs);
		
		StringBuffer rutaRelativa=new StringBuffer();
		for (int i=0;i<orgs.length-1;i++){			
			rutaRelativa.append(orgs[i]);
			rutaRelativa.append(UsuarioLDAP.SEPARADOR_RUTA_RELATIVA);
			rutaRelativa.append(orgs[orgs.length-1]);
		}
		
		usuario.setRutaRelativa(rutaRelativa.toString());
		

        return usuario;
    }
    
    protected static void mapToContextBasic(UsuarioLDAP usuario,DirContextOperations context) throws UnsupportedEncodingException, RepositoryException {
    	context.setAttributeValue("displayName", NovedadRepositoryHibernateHelper.getInstancia().reemplazarVariables(usuario.getNombreAMostrar(),usuario));
    	context.setAttributeValue("department", usuario.getDeptid());
    	context.setAttributeValue("title", usuario.getSga_puesto());
    	// 20120816
//    	context.setAttributeValue("description", usuario.getDescripcion());
    	
    	
//    	if (context.getStringAttributes("description")!=null){
//    		context.setAttributeValues("description", new String[]{usuario.getDescripcion()});
//    	}else{
//    		context.addAttributeValue("description",usuario.getDescripcion());
//    	}
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
		
		/* Este bloque se comenta para ADAM pero va en PRODUCCION Mail*/ 
		if (usuario.tieneMail()){
			context.setAttributeValue("homeMDB",usuario.getMail().getPath());
			context.setAttributeValue("mDBUseDefaults","TRUE");
			context.setAttributeValue("msExchHomeServerName",usuario.getMail().getServidor());
			context.setAttributeValue("mail",usuario.getMail().getEmailInterno());
			context.setAttributeValue("mailnickname",usuario.getMail().getMail());
		}
		
		//DIAL IN VPN.
//		context.setAttributeValue("msNPAllowDialin",usuario.isTieneVPN()?"TRUE":"FALSE");		
		
		//Mail con externo
//		if (usuario.tieneMail()){
//			context.setAttributeValue("homeMDB",usuario.getMail().getPath());
//			context.setAttributeValue("mDBUseDefaults","TRUE");
//			context.setAttributeValue("msExchHomeServerName",usuario.getMail().getServidor());
////			context.setAttributeValue("mail",usuario.getMail().getEmailInterno());//(sin SMTP) Primario REDCOTO
//			
//			if (Boolean.TRUE.equals(usuario.getMail().getMailInternoActivo())){
//				context.setAttributeValue("mail",usuario.getMail().getEmailInterno());
//			}else{
//				context.setAttributeValue("mail",usuario.getMail().getEmailExterno());			
//			}
//			
//			context.setAttributeValue("mailnickname",usuario.getUser_oprid());
//			List<String> proxyAddresses=new ArrayList<String>();//SMTP es primario.
//			if (Boolean.TRUE.equals(usuario.getMail().getMailInternoActivo())) proxyAddresses.add("SMTP:"+usuario.getMail().getEmailInterno());
//			if (Boolean.TRUE.equals(usuario.getMail().getMailExternoActivo())) proxyAddresses.add("smtp:"+usuario.getMail().getEmailExterno());			
//			context.setAttributeValues("proxyAddress",proxyAddresses.toArray());
//		}	
	}

}