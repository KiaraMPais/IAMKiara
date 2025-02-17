package com.coto.sga.infrastructure.persistence.ldap;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.AbstractContextMapper;

import com.coto.sga.domain.model.usuario.UsuarioLDAP;


public class UsuarioContextMapperADAM extends AbstractContextMapper {
	private static final Logger logger = Logger.getLogger(UsuarioContextMapperADAM.class);	
	
    protected Object doMapFromContext(DirContextOperations context) {        	
		UsuarioLDAP usuario=new UsuarioLDAP();
		logger.info("Ingresando a UsuarioContextMapperADAM.doMapFromContext Quiroz2");
		usuario.setUser_oprid(context.getStringAttribute("cn"));
				
		String[] nombres=context.getStringAttribute("givenName").split(" ");
		if (nombres.length>0) usuario.setName1(nombres[0]);
		if (nombres.length>1) usuario.setName2(nombres[1]); 
		
//		String[] apellidos=context.getStringAttribute("sn").split(" ");
//		if (nombres.length>0) usuario.setLast_name(nombres[0]);
//		if (nombres.length>1) usuario.setLast_name2(nombres[1]);
		
		
		usuario.setDeptid(context.getStringAttribute("department"));
		usuario.setEmplid(context.getStringAttribute("company"));
		usuario.setSga_puesto(context.getStringAttribute("title"));
		//usuario.setEmail(context.getStringAttribute("userPrincipalName"));
		//usuario.setNombreAMostrar(context.getStringAttribute("displayName"));
		
        return usuario;
    }
    
	protected static void mapToContext(UsuarioLDAP usuario,DirContextOperations context) throws UnsupportedEncodingException {
										
	    context.setAttributeValues("objectclass", new String[] {"user"});
		context.setAttributeValue("userPrincipalName", usuario.getNombrePrincipal());		
		context.setAttributeValue("givenName",usuario.getNombres());
		context.setAttributeValue("sn", usuario.getApellidos());
		context.setAttributeValue("displayName", usuario.getNombreCompleto());
		context.setAttributeValue("department", usuario.getDeptid());
		context.setAttributeValue("company", usuario.getEmplid());
		context.setAttributeValue("title", usuario.getSga_puesto());
		context.setAttributeValue("description", usuario.getDescripcion());	
		context.setAttributeValue("unicodepwd", ActiveDirectoryUtils.getInstancia().encodePassword(usuario.getClave()));
		
		//userAttributes.put("pwdLastSet", new Integer(-1)); // Para AD? o va en useraccontrol
			    
	}
	
}



//protected Object doMapFromContext(DirContextOperations context) {       
//	UsuarioLDAP usuario=new UsuarioLDAP();
//    
//	String[] nombres=context.getStringAttribute("givenName").split(" ");
//	if (nombres.length>0) usuario.setName1(nombres[0]);
//	if (nombres.length>1) usuario.setName2(nombres[1]); 
//
//	String[] apellidos=context.getStringAttribute("sn").split(" ");
//	if (apellidos.length>0) usuario.setLast_name(apellidos[0]);
//	if (apellidos.length>1) usuario.setLast_name2(apellidos[1]);
//
//	//usuario.setUser_oprid(context.getStringAttribute("sAMAccountName"));
//	usuario.setDeptid(context.getStringAttribute("department"));
//	usuario.setEmplid(context.getStringAttribute("company"));
//	usuario.setSga_puesto(context.getStringAttribute("title"));
//	
//	
//	// Ruta relativa:
//	String[] orgs=ActiveDirectoryUtils.getInstancia().getOusFrom(new DistinguishedName(context.getNameInNamespace()));
//	//ArrayUtils.reverse(orgs);
//	
//	StringBuffer rutaRelativa=new StringBuffer();
//	for (int i=0;i<orgs.length-1;i++){			
//		rutaRelativa.append(orgs[i]);
//		rutaRelativa.append(UsuarioLDAP.SEPARADOR_RUTA_RELATIVA);
//	}
//	rutaRelativa.append(orgs[orgs.length-1]);
//	
//	usuario.setRutaRelativa(rutaRelativa.toString());
//	
//
//    return usuario;
//}
//
//protected static void mapToContext(UsuarioLDAP usuario,DirContextOperations context) throws UnsupportedEncodingException {
//			
//	context.setAttributeValues("objectclass", new String[] {"user"});
//	//context.setAttributeValues("objectclass", new String[] {"person", "user"});
//	context.setAttributeValue("userPrincipalName", usuario.getNombrePrincipal());
//	//context.setAttributeValue("sAMAccountName", usuario.getUser_oprid());
//	context.setAttributeValue("givenName",usuario.getNombres());
//	context.setAttributeValue("sn", usuario.getApellidos());
//	context.setAttributeValue("displayName", usuario.getNombreCompleto());
//	context.setAttributeValue("department", usuario.getDeptid());
//	context.setAttributeValue("company", usuario.getEmplid());
//	context.setAttributeValue("title", usuario.getSga_puesto());
//	context.setAttributeValue("description", usuario.getDescripcion());
	