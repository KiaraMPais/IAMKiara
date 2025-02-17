package com.coto.sga.infrastructure.persistence.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.directory.SearchControls;

import org.apache.log4j.Logger;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.OperationNotSupportedException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.NotFilter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.grupo.Grupo;
import com.coto.sga.domain.model.usuario.UsuarioLDAP;
import com.coto.sga.domain.model.usuario.UsuarioLDAPRepository;
import com.coto.sga.infrastructure.persistence.exception.ClaveUsuarioException;
import com.coto.sga.infrastructure.persistence.exception.UsuarioDuplicadoException;
import com.coto.sga.infrastructure.persistence.exception.UsuarioNoExisteException;

//@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
public class UsuarioADAMRepositoryImpl extends LDAPRepository implements UsuarioLDAPRepository{
	private static final Logger logger = Logger.getLogger(UsuarioContextMapperAD.class);		
	
	@Override
	@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)	
	public void crear(UsuarioLDAP usuarioAD) throws RepositoryException{
		logger.info("Ingresando a UsuarioADAMRepositoryImpl.crear");
		logger.info("usuarioAD.getUser_oprid()= "+usuarioAD.getUser_oprid());
		DirContextOperations context=new DirContextAdapter(getDnFrom(usuarioAD.getUser_oprid()));				
		try{
			
			UsuarioContextMapperAD.mapToContext(usuarioAD, context);
			ldapTemplate.bind(context);
			
			for (Grupo grupo : usuarioAD.getGrupos()) {
				agregarUsuarioAGrupo(usuarioAD,grupo);
			}
								
		} catch (NameAlreadyBoundException e){
			throw new UsuarioDuplicadoException(usuarioAD);			
		} catch (OperationNotSupportedException e) {
			throw new ClaveUsuarioException(usuarioAD);						
		} catch (Throwable e) {			
			throw new RepositoryException("Hubo un problema al crear el usuario:[" + usuarioAD.getUser_oprid() + "]");
		}
	}
	
	
	@Override
	@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
	public void actualizar(UsuarioLDAP usuarioAD) throws RepositoryException {
		
		try{
			
		    DirContextOperations context = ldapTemplate.lookupContext(getDnFrom(usuarioAD));	    
			UsuarioContextMapperAD.mapToContext(usuarioAD, context);		
		    ldapTemplate.modifyAttributes(context);
		
		    // Actualizo los grupos:	    
			// Obtengo el usuario actual con los grupos actuales.
			UsuarioLDAP usuarioActual=obtenerUsuarioPorNombreUsuario(usuarioAD.getUser_oprid());				
			//Actualiza el usuario en los grupos que contiene.		 
			for (Grupo grupo : usuarioActual.getGrupos()){
				if (usuarioAD.getGrupos().contains(grupo)){// Si el usuario esta actualmente en el grupo.
					agregarUsuarioAGrupo(usuarioAD,grupo);
				}else{
					borrarUsuarioDeGrupo(usuarioAD,grupo);// Si no esta actualmente en el usuario lo saca.
				}
			}
			
		} catch (NameNotFoundException e){
			throw new UsuarioNoExisteException(usuarioAD);			
		} catch (OperationNotSupportedException e) {
			throw new ClaveUsuarioException(usuarioAD);						
		} catch (Throwable e){
			throw new RepositoryException("Hubo un problema al actualizar el usuario:[" + usuarioAD.getUser_oprid() + "]");
		}
	}
	
	@Override
	@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
	public void eliminar(UsuarioLDAP usuarioAD) throws RepositoryException {
		try{
		
			ldapTemplate.unbind(getDnFrom(usuarioAD.getUser_oprid()));
			// TODO Chequear si saca los usuarios de los grupos.
			
		} catch (NameNotFoundException e){
			throw new UsuarioNoExisteException(usuarioAD);								
		} catch (Throwable e){
			throw new RepositoryException("Hubo un problema al eliminar el usuario:[" + usuarioAD.getUser_oprid() + "]");
		}
	}
	
	
	/**
	 * Agrega el usuario al grupo.
	 * 
	 * El usuario debe estar creado.
	 */
	public void agregarUsuarioAGrupo(UsuarioLDAP usuario,Grupo grupo) throws RepositoryException{
		try {
			DirContextAdapter dirContext = (DirContextAdapter) ldapTemplate.lookup(getDnFrom(usuario));			
			String dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);											
			DirContextOperations groupContextOperations = ldapTemplate.lookupContext(getDnFrom(grupo.getNombre()));
			String[] currentMembers = groupContextOperations.getStringAttributes(ConstantesAD.MEMBER_ATTR_NAME);
			List<String> dnUserFullList = new ArrayList<String>();
			if (currentMembers != null && currentMembers.length > 0) {
				dnUserFullList.addAll(Arrays.asList(currentMembers));
			}
			dnUserFullList.add(dnUserFull);
			groupContextOperations.setAttributeValues(ConstantesAD.MEMBER_ATTR_NAME, dnUserFullList.toArray(new String[dnUserFullList.size()]));
			ldapTemplate.modifyAttributes(groupContextOperations);			
		} catch (Throwable e) {
			throw new RepositoryException("Hubo un problema agregando el usuario:["+usuario.getUser_oprid()+"] al grupo:["+grupo.getNombre()+"]");
		}
	}		
	
	/**
	 * Borra el Usuario del Grupo.
	 * 
	 * El Usuario debe estar creado.
	 */
	public void borrarUsuarioDeGrupo(UsuarioLDAP usuario, Grupo grupo) throws RepositoryException {
		try {
			DirContextAdapter dirContext = (DirContextAdapter) ldapTemplate.lookup(getDnFrom(usuario));
			String dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);
			DirContextOperations groupContextOperations = ldapTemplate.lookupContext(getDnFrom(grupo.getNombre()));
			String[] currentMembers = groupContextOperations.getStringAttributes(ConstantesAD.MEMBER_ATTR_NAME);
			List<String> dnUserFullList = new ArrayList<String>();
			if (currentMembers != null && currentMembers.length > 0) {
				dnUserFullList.addAll(Arrays.asList(currentMembers));
			}
			dnUserFullList.remove(dnUserFull);
			groupContextOperations.setAttributeValues(ConstantesAD.MEMBER_ATTR_NAME, dnUserFullList.toArray(new String[dnUserFullList.size()]));
			ldapTemplate.modifyAttributes(groupContextOperations);
		} catch (Throwable e) {
			throw new RepositoryException("Hubo un problema borrando el usuario:["+usuario.getUser_oprid()+"] del grupo:["+grupo.getNombre()+"]");
		}
	}

	/**
	 * Habilita el Usuario.
	 */
	public void habilitarUsuario(String nombreUsuario) {
		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(nombreUsuario));					
		userContextOperations.setAttributeValue("msDS-UserAccountDisabled", "FALSE");
		ldapTemplate.modifyAttributes(userContextOperations);
	}

	/**
	 * Deshabilita el Usuario.
	 */
	public void deshabilitarUsuario(String nombreUsuario) {
		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(nombreUsuario));					
		userContextOperations.setAttributeValue("msDS-UserAccountDisabled", "TRUE");
		ldapTemplate.modifyAttributes(userContextOperations);
	}


	/**
	 * Autentifica el usuario. 
	 */
	public boolean login(String userName, String password) {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("cn", userName));
		return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, filter.toString(), password);
	}
	
	
	/**
	 * Carga los grupos desde el repositorio dentro del usuario. 
	 */
	private void cargarGruposEnUsuario(UsuarioLDAP usuario){		
		DirContextAdapter dirContext = (DirContextAdapter) ldapTemplate.lookup(getDnFrom(usuario.getUser_oprid()));
		String[] dnGrupos = dirContext.getStringAttributes("memberOf");
		Grupo grupo=null;
		for (String dnGrupo : dnGrupos) {
			grupo=(Grupo) ldapTemplate.lookup(dnGrupo,new GrupoContextMapper());			
			usuario.addGrupo(grupo);
		}
	}
	
	/**
	 * Obtiene el usuario por nombre de usuario, junto a sus grupos.
	 */
	public UsuarioLDAP obtenerUsuarioPorNombreUsuario(String nombreUsuario) {		
		UsuarioLDAP usuario=(UsuarioLDAP) ldapTemplate.lookup(nombreUsuario,new UsuarioContextMapperAD());
		cargarGruposEnUsuario(usuario);		
		return usuario;		
	}

	@SuppressWarnings("unchecked")
	public DistinguishedName getDnFrom(UsuarioLDAP usuario){		
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectClass", "person")).and(new EqualsFilter("company", usuario.getEmplid()));
		filter.and(new NotFilter(new EqualsFilter("ou", "TRASH")));


		List<String> dns=ldapTemplate.search(DistinguishedName.EMPTY_PATH,filter.encode(), searchControls,
					new ContextMapper(){
			public String mapFromContext(Object ctx){
				return ((DirContextAdapter) ctx).getStringAttribute("distinguishedName");
			}
		});
		
		for (String dn : dns) {
			if (!dn.contains("OU=TRASH")){
				return new DistinguishedName(dn);				
			}
		}
		return null;
		
//		String dn = (String) ldapTemplate.searchForObject("", filter.toString(),new ContextMapper(){
//			public String mapFromContext(Object ctx){
//				return ((DirContextAdapter) ctx).getStringAttribute("dn");
//			}
//		});
//		
//		return new DistinguishedName(dn);
		
//		if (dns.isEmpty()) return null;
		
		
//		DistinguishedName dn=new DistinguishedName(); //
//		dn.add(dns.get(0)); //
		
//		return new DistinguishedName(dns.get(0));		
	}


	@Override
	public void modificarClaveYEstado(UsuarioLDAP usuarioAD)
			throws RepositoryException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void accesoAlWebServiceExchange(UsuarioLDAP usuarioAD)
			throws RepositoryException {
		// TODO Auto-generated method stub
		
	}
	
	
}

//public List<Grupo> getAllGrupos(UsuarioAD usuario) {
//	SearchControls controls = new SearchControls();
//	controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//	
//	AndFilter filter = new AndFilter();
//	filter.and(new EqualsFilter("objectclass", "group")).and(new EqualsFilter("member",usuario.getNombreUsuario()));
//	
//	return ldapTemplate.search("", "(objectclass=group)", controls, new UserContextMapper());
//}


/*
/**
 * Agrega el usuario a los grupos que contiene.
 * 
 * El usuario debe estar creado.  
 /*
private void agregarUsuarioAGrupo(UsuarioAD usuarioAD,Grupo grupo){
	try {
		DirContextAdapter dirContext = (DirContextAdapter) ldapTemplate.lookup(getDnFrom(usuarioAD.getNombreUsuario()));			
		String dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);			
		DirContextOperations groupContextOperations=null;
		
		for (Grupo grupo : usuarioAD.getGrupos()) {
			groupContextOperations = ldapTemplate.lookupContext(getDnFrom(grupo.getNombre()));
			String[] currentMembers = groupContextOperations.getStringAttributes(ConstantesAD.MEMBER_ATTR_NAME);
			List<String> dnUserFullList = new ArrayList<String>();
			if (currentMembers != null && currentMembers.length > 0) {
				dnUserFullList.addAll(Arrays.asList(currentMembers));
			}
			dnUserFullList.add(dnUserFull);
			groupContextOperations.setAttributeValues(ConstantesAD.MEMBER_ATTR_NAME, dnUserFullList.toArray(new String[dnUserFullList.size()]));
			ldapTemplate.modifyAttributes(groupContextOperations);
		}
	} catch (Throwable e) {
		//throw new LdapException("Problem adding user:[" + userName + "] to Group:[" + group + "]", e);
	}
}		


	public UserDetails getUserDetails(String userName) {
		DirContextAdapter dirContext = (DirContextAdapter) ldapTemplate.lookup(getDnFrom(userName));
		String dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String userAccountControlStr = dirContext.getStringAttribute(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME);
		String pwdLastSet = dirContext.getStringAttribute(ConstantesAD.PWD_LAST_SET_ATTR_NAME);
		String accountExpires = dirContext.getStringAttribute(ConstantesAD.ACCOUNT_EXPIRES_ATTR_NAME);
		String maxPwdAge = "-36288000000000";
		UserDetails userDetails = activeDirectoryUtils.getUserDetailsFrom(userName, dnUserFull, userAccountControlStr, pwdLastSet, accountExpires, maxPwdAge);
		return userDetails;
	}
	
		public List<UsuarioAD> getAllUsers() {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		return ldapTemplate.search("", "(objectclass=person)", controls, new UserContextMapper());
	}

	public UsuarioAD obtenerUsuarioPorNombreUsuario(String nombreUsuario) {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		//controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("cn", nombreUsuario));
		List<UsuarioAD> usuarios = ldapTemplate.search("", filter.toString(), controls, new UsuarioContextMapper());
		if (usuarios.isEmpty()) return null;
		UsuarioAD usuario = usuarios.get(0);
				
		// Cargo los grupos del usuario.
		DirContextAdapter dirContext = (DirContextAdapter) ldapTemplate.lookup(getDnFrom(usuario.getNombreUsuario()));
		String[] dnGrupos = dirContext.getStringAttributes("memberOf");
		Grupo grupo=null;
		for (String dnGrupo : dnGrupos) {
			filter = new AndFilter();
			filter.and(new EqualsFilter("objectclass", "group")).and(new EqualsFilter("cn", dnGrupo));
			grupo=(Grupo) ldapTemplate.search("", filter.toString(), controls, new GrupoContextMapper()).get(0);			
			usuario.addGrupo(grupo);
		}
		
		return usuario;
	}

*/
/*
 * Modifica la clave del usuario. 
 */
/*
public void cambiarPassword(String nombreUsuario, String password){
	try {
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ConstantesAD.PASSWORD_ATTR_NAME, ActiveDirectoryUtils.getInstancia().encodePassword(password)));
		ldapTemplate.modifyAttributes(getDnFrom(nombreUsuario), new ModificationItem[]{item});
	} catch (OperationNotSupportedException e) {
		// throw new PasswordStrengthException("Password:[" + password + "] does not pass the strength password validation.", e);
	} catch (Throwable e) {
		// throw new LdapException("Problems changing password.", e);
	}
}*/

///////////////////////////////////////////


//package com.coto.sga.infrastructure.persistence.ldap;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import javax.naming.InvalidNameException;
//import javax.naming.directory.Attribute;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.BasicAttribute;
//import javax.naming.directory.BasicAttributes;
//import javax.naming.directory.DirContext;
//import javax.naming.directory.ModificationItem;
//import javax.naming.directory.SearchControls;
//import javax.naming.ldap.LdapContext;
//
//import org.springframework.ldap.NameAlreadyBoundException;
//import org.springframework.ldap.NameNotFoundException;
//import org.springframework.ldap.NamingException;
//import org.springframework.ldap.OperationNotSupportedException;
//import org.springframework.ldap.core.ContextMapper;
//import org.springframework.ldap.core.DirContextAdapter;
//import org.springframework.ldap.core.DirContextOperations;
//import org.springframework.ldap.core.DistinguishedName;
//import org.springframework.ldap.filter.AndFilter;
//import org.springframework.ldap.filter.EqualsFilter;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.coto.sga.application.util.Properties;
//import com.coto.sga.domain.model.exception.RepositoryException;
//import com.coto.sga.domain.model.grupo.Grupo;
//import com.coto.sga.domain.model.usuario.UsuarioLDAP;
//import com.coto.sga.domain.model.usuario.UsuarioLDAPRepository;
//import com.coto.sga.infrastructure.persistence.exception.ClaveUsuarioException;
//import com.coto.sga.infrastructure.persistence.exception.GrupoNoExisteException;
//import com.coto.sga.infrastructure.persistence.exception.UsuarioDuplicadoException;
//import com.coto.sga.infrastructure.persistence.exception.UsuarioNoExisteException;
//
//@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
//public class UsuarioADRepositoryImpl extends LDAPRepository implements UsuarioLDAPRepository{
//
//
//	private final static String ORG_TRASH=Properties.getString("organizacion_usuarios_eliminados");
//	
//	@Override
////	@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
//	public void crear(UsuarioLDAP usuarioAD) throws RepositoryException{
//		
//		DirContextOperations context=new DirContextAdapter(getDnNuevo(usuarioAD));
//		try{
//			
//			UsuarioContextMapperAD.mapToContext(usuarioAD, context);
//			ldapTemplate.bind(context);
//			modificarEstadoCuenta(usuarioAD);
//	        modificarClave(usuarioAD);
//			
//			for (Grupo grupo : usuarioAD.getGrupos()) {
//				agregarUsuarioAGrupo(usuarioAD,grupo);
//			}
//								
//		} catch (NameAlreadyBoundException e){
//			throw new UsuarioDuplicadoException(usuarioAD,e);
//		} catch (OperationNotSupportedException e) {
//			throw new ClaveUsuarioException(usuarioAD,e);						
//		} catch (Throwable e) {
//			throw new RepositoryException("Hubo un problema al crear el usuario [" + usuarioAD.getUser_oprid() + "]",e);
//		}
//	}
//	
//		
//	@Override
////	@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
//	public void actualizar(UsuarioLDAP usuarioAD) throws RepositoryException {
//		
//		try{
//			
//		    DirContextOperations context = ldapTemplate.lookupContext(getDnFrom(usuarioAD));
//			UsuarioContextMapperAD.mapToContext(usuarioAD, context);
//		    ldapTemplate.modifyAttributes(context);
//		    
////		    ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("title", usuarioAD.getSga_puesto()));
////			ldapTemplate.modifyAttributes(getDnFrom(usuarioAD), new ModificationItem[]{item});
//		
//			UsuarioLDAP usuarioActual=obtenerUsuarioPorLegajo(usuarioAD);
//			
//		    // Actualizo los grupos
//			for (Grupo grupo : usuarioActual.getGrupos()){// Borro todos los grupos actuales.				
//				borrarUsuarioDeGrupo(usuarioAD,grupo);// Si no esta actualmente en el usuario lo saca.
//			}
//			for (Grupo grupo : usuarioAD.getGrupos()){//Agrego todos los grupos nuevos.		
//				agregarUsuarioAGrupo(usuarioAD,grupo);
//			}
//			
//		} catch (NameNotFoundException e){
//			throw new UsuarioNoExisteException(usuarioAD,e);
//		} catch (OperationNotSupportedException e) {
//			throw new ClaveUsuarioException(usuarioAD,e);
//		}catch (RepositoryException e) {
//			throw e;
//		} catch (Throwable e){
//			throw new RepositoryException("Hubo un problema al actualizar el usuario [" + usuarioAD.getUser_oprid() + "]",e);
//		}
//	}
//	
//	// Add: Agrego los grupos del nuevo siempre y cuando no existan ya en el usuario actual. Agrego los grupos al usuario.
//	
//	// Sub: Saco los grupos del nuevo siempre y cuando no sean parte del puesto original del usuario. Saco los grupos al usuario.
//	
//	
//	@Override
////	@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
//	public void eliminar(UsuarioLDAP usuarioAD) throws RepositoryException {
//		try{
//		
//			ocultarYDeshabilitar(usuarioAD);
//			
//		}catch (RepositoryException e){
//			throw e;
//		} catch (Throwable e){
//			throw new RepositoryException("Hubo un problema al eliminar el usuario [" + usuarioAD.getUser_oprid() + "]",e);
//		}
//	}
//	
//	
//	/**
//	 * Agrega el usuario al grupo.
//	 * 
//	 * El usuario debe estar creado.
//	 */
//	public void agregarUsuarioAGrupo(UsuarioLDAP usuario,Grupo grupo) throws RepositoryException{
//		
//		String dnUserFull;
//		try{			
//			DirContextOperations dirContext = ldapTemplate.lookupContext(getDnFrom(usuario));			
//			dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);			
//		}catch (NamingException e) {
//			throw new UsuarioNoExisteException(usuario);
//		}
//					
//		try {
//			
//			DirContextOperations groupContextOperations = getDirContextOperationsFrom(grupo);
//			String[] currentMembers = groupContextOperations.getStringAttributes(ConstantesAD.MEMBER_ATTR_NAME);
//			List<String> dnUserFullList = new ArrayList<String>();
//			if (currentMembers != null && currentMembers.length > 0) {
//				dnUserFullList.addAll(Arrays.asList(currentMembers));
//			}
//			dnUserFullList.add(dnUserFull);
//			groupContextOperations.setAttributeValues(ConstantesAD.MEMBER_ATTR_NAME, dnUserFullList.toArray(new String[dnUserFullList.size()]));
//			ldapTemplate.modifyAttributes(groupContextOperations);
//			
//		}catch (NameNotFoundException e) {
//			throw new GrupoNoExisteException(grupo);
//		} catch (Throwable e) {
//			throw new RepositoryException("Hubo un problema agregando el usuario ["+usuario.getUser_oprid()+"] al grupo ["+grupo.getNombre()+"]",e);
//		}
//	}
//	
//
//	/**
//	 * Borra el Usuario del Grupo.
//	 * 
//	 * El Usuario debe estar creado.
//	 */
//	public void borrarUsuarioDeGrupo(UsuarioLDAP usuario, Grupo grupo) throws RepositoryException {
//		
//		String dnUserFull;
//		try {
//			DirContextOperations dirContext = ldapTemplate.lookupContext(getDnFrom(usuario));
//			dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);
//		}catch (NamingException e) {
//			throw new UsuarioNoExisteException(usuario);
//		}
//		
//		try{
//			DirContextOperations groupContextOperations = getDirContextOperationsFrom(grupo);
//			String[] currentMembers = groupContextOperations.getStringAttributes(ConstantesAD.MEMBER_ATTR_NAME);
//			List<String> dnUserFullList = new ArrayList<String>();
//			if (currentMembers != null && currentMembers.length > 0) {
//				dnUserFullList.addAll(Arrays.asList(currentMembers));
//			}
//			dnUserFullList.remove(dnUserFull);
//			groupContextOperations.setAttributeValues(ConstantesAD.MEMBER_ATTR_NAME, dnUserFullList.toArray(new String[dnUserFullList.size()]));
//			ldapTemplate.modifyAttributes(groupContextOperations);
//		}catch (NamingException e) {
//			throw new GrupoNoExisteException(grupo);
//		} catch (Throwable e) {
//			throw new RepositoryException("Hubo un problema borrando el usuario ["+usuario.getUser_oprid()+"] del grupo ["+grupo.getNombre()+"]",e);
//		}
//	}
//	
//	/**
//	 * Modifica el estado de la cuenta de usuario dependiendo de su estado actual.
//	 * El usuario debe estar creado en AD. 
//	 */
//	public void modificarEstadoCuenta(UsuarioLDAP usuario) throws RepositoryException{
//		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(usuario));
////		userContextOperations.setAttributeValue(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME, "" + ActiveDirectoryUtils.getInstancia().getUserAccountControl(usuario));
////		userContextOperations.setAttributeValue(ConstantesAD.MAX_PWD_AGE_ATTR_NAME, "" + ActiveDirectoryUtils.getInstancia().getMaxPwdSetValue(usuario.getDuracionClave()));//TODO property
//		userContextOperations.setAttributeValue(ConstantesAD.ACCOUNT_EXPIRES_ATTR_NAME, "" + ActiveDirectoryUtils.getInstancia().getAccountExpiredDateValue(usuario.getDuracionClave()));//TODO property
//		ldapTemplate.modifyAttributes(userContextOperations);
//	}
//	
//	/**
//	 * Modifica la clave del usuario con la clave asignada actualmente.
//	 * El usuario debe estar creado en AD.
//	 */
//	private void modificarClave(UsuarioLDAP usuario) throws RepositoryException{
//		try {
//			ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ConstantesAD.PASSWORD_ATTR_NAME, ActiveDirectoryUtils.getInstancia().encodePassword(usuario.getClave())));
//			ldapTemplate.modifyAttributes(getDnFrom(usuario), new ModificationItem[]{item});
//		} catch (OperationNotSupportedException e) {
//			 throw new RepositoryException("Hubo un problema a modificar la clave del usuario [" + usuario.getUser_oprid() + "]",e);
//		} catch (Throwable e) {
//			 throw new RepositoryException("Hubo un problema a modificar la clave del usuario [" + usuario.getUser_oprid() + "]",e);
//		}
//	}
//	
//	/**
//	 * Oculta e Inhabilita completamente el usuario. 
//	 */
//	private void ocultarYDeshabilitar(UsuarioLDAP usuario) throws RepositoryException{
//		DistinguishedName dn=getDnFrom(usuario);
//		if (dn==null) throw new UsuarioNoExisteException(usuario);
//		
//		List<ModificationItem> userAttributes = new ArrayList<ModificationItem>();
//		//userAttributes.add(new ModificationItem(LdapContext.REPLACE_ATTRIBUTE,new BasicAttribute(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME,ConstantesAD.USER_CONTROL_DISABLE_USER)));
//		//userAttributes.add(new ModificationItem(LdapContext.REPLACE_ATTRIBUTE,new BasicAttribute(ConstantesAD.HIDE_FROM_ADD_LISTS_ATTR_NAME,"TRUE")));
//
//		// Borro las direcciones proxy al usuario.
//		List<String> direccionesProxy=getDireccionesProxy(dn.toString());
//		for (String dir : direccionesProxy) {
//			userAttributes.add(new ModificationItem(LdapContext.REMOVE_ATTRIBUTE,new BasicAttribute(ConstantesAD.PROXY_ADDRESSES_ATTR_NAME,dir)));
//		}
//		
//		try{
//			ldapTemplate.modifyAttributes(dn,userAttributes.toArray(new ModificationItem[userAttributes.size()]));
//		}catch (NamingException e) {
//			throw new UsuarioNoExisteException(usuario);
//		}
//		
//		// Borro al usuario de sus grupos.
//		for (Grupo grupo : usuario.getGrupos()) {
//			borrarUsuarioDeGrupo(usuario,grupo);
//		}
//		
//		// Muevo el usuario a trash.
//		String dnCarpeta = "ou="+usuario.getUser_oprid().substring(0,1).toUpperCase()+",ou="+ORG_TRASH;
//		try{
//			
//			ldapTemplate.lookup(dnCarpeta);
//			
//		}catch (NameNotFoundException e) {// Creo la carpeta si no existe.			
//		    Attributes atributos = new BasicAttributes(true);
//		    Attribute objclass = new BasicAttribute(ConstantesAD.OBJECT_CLASS_ATTR_NAME);
//		    //objclass.add("top");
//		    objclass.add("organizationalUnit");
//		    atributos.put(objclass);
//			ldapTemplate.bind(dnCarpeta,null,atributos);
//		}
//					
//		ldapTemplate.rename(dn.toString(),"cn="+getCnFrom(usuario)+","+dnCarpeta);
//	}
//	
//	private List<String> getDireccionesProxy(String dnUsuario){
//		String[] atributo={ConstantesAD.PROXY_ADDRESSES_ATTR_NAME};
//		DirContextOperations dir=retrieveEntry(dnUsuario,atributo);
//		String[] dirProxies=dir.getStringAttributes(atributo[0]);
//		if (dirProxies==null) return new ArrayList<String>();
//		return Arrays.asList(dirProxies);
//	}
//		
//	/**
//	 * Carga los grupos desde el repositorio dentro del usuario. 
//	 */
//	private void cargarGruposEnUsuario(UsuarioLDAP usuario) throws RepositoryException{
//		DirContextOperations dirContext = ldapTemplate.lookupContext(getDnFrom(usuario));
//		String[] dnGrupos = dirContext.getStringAttributes(ConstantesAD.MEMBER_OF_ATTR_NAME);		
//		for (String dnGrupo : dnGrupos) {			
//			usuario.addGrupo(obtenerGrupoPorFullDn(dnGrupo));
//		}
//		//throw new RepositoryException("");//TODO sacar, es para ver si hace rollback
//	}
//	
//
//	/**
//	 * Obtiene el usuario por su legajo, junto a sus grupos.
//	 * El legajo en el usuario debe estar cargado, asi como la ruta relativa.
//	 */
//	private UsuarioLDAP obtenerUsuarioPorLegajo(UsuarioLDAP usuario) throws RepositoryException {				
//		UsuarioLDAP user=(UsuarioLDAP) ldapTemplate.lookup(getDnFrom(usuario),new UsuarioContextMapperAD());
//		user.setRutaRelativa(usuario.getRutaRelativa());
//		cargarGruposEnUsuario(user);
//		return user;		
//	}
//	
//	
//	/**
//	 * Obtiene el DN del usuario. Busca el DN por legajo.
//	 * EL usuario debe tener el legajo cargado (company) y la ruta relativa (ruta de organizacion). 
//	 */
//	@SuppressWarnings("unchecked")
//	public DistinguishedName getDnFrom(UsuarioLDAP usuario) throws UsuarioNoExisteException{
//		SearchControls searchControls = new SearchControls();
//		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//		
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "person")).and(new EqualsFilter("company", usuario.getEmplid()));		
//
//		List<String[]> cns=ldapTemplate.search(DistinguishedName.EMPTY_PATH,filter.encode(), searchControls,
//					new ContextMapper(){
//			public String[] mapFromContext(Object ctx){
//				return new String[]{((DirContextAdapter) ctx).getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME),((DirContextAdapter) ctx).getStringAttribute("cn")};				
//			}
//		});
//		
//		String cn=null;
//		for (String[] dnCn : cns) {
//			if (!dnCn[0].contains("OU="+ORG_TRASH)){
//				cn=dnCn[1];
//			}
//		}
//		if (cn==null) return null;
//		
//		DistinguishedName dn=new DistinguishedName();								
//		// Organizacion		
//		for (String ou : usuario.getRutaRelativaSeparada()){
//			dn.add("ou",ou);
//		}
//		// Contenedor
//		dn.add("cn",cn);
//		
//		return dn;
//	}
//	
//	
//	/**
//	 * Obtiene el CN del usuario. Busca el CN por legajo.
//	 */
//	@SuppressWarnings("unchecked")
//	private String getCnFrom(UsuarioLDAP usuario){		
//		SearchControls searchControls = new SearchControls();
//		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//		
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "person")).and(new EqualsFilter("company", usuario.getEmplid()));		
//
//		List<String[]> cns=ldapTemplate.search(DistinguishedName.EMPTY_PATH,filter.encode(), searchControls,
//					new ContextMapper(){
//			public String[] mapFromContext(Object ctx){
//				return new String[]{((DirContextAdapter) ctx).getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME),((DirContextAdapter) ctx).getStringAttribute("cn")};				
//			}
//		});
//			
//		for (String[] dnCn : cns) {
//			if (!dnCn[0].contains("OU="+ORG_TRASH)){
//				return dnCn[1];				
//			}
//		}
//		
//		return null;
//	}
//	
//	/*
//	 * Obtiene el DN a generarse del usuario. 
//	 */
//	private DistinguishedName getDnNuevo(UsuarioLDAP usuario){
//		DistinguishedName dn=new DistinguishedName();			
//				
//		// Organizacion		
//		for (String ou : usuario.getRutaRelativaSeparada()){
//			dn.add("ou",ou);
//		}		
//		// Contenedor
//		dn.add("cn",usuario.getUser_oprid());
//		
//		return dn;
//	}
//	
//
//	@SuppressWarnings({ "unchecked", "unchecked" })
//	private DirContextOperations getDirContextOperationsFrom(Grupo grupo) throws InvalidNameException{		
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "group")).and(new EqualsFilter("cn", grupo.getNombre()));
//
//		return (DirContextOperations) ldapTemplate.searchForObject(DistinguishedName.EMPTY_PATH,filter.encode(),
//				new ContextMapper(){
//					public DirContextOperations mapFromContext(Object ctx){
//						return (DirContextOperations) ctx;
//					}
//				});
//	}
//	
//	@SuppressWarnings({ "unchecked", "unchecked" })
//	private Grupo obtenerGrupoPorFullDn(String fullDn){
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "group")).and(new EqualsFilter(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME, fullDn));		
//		return (Grupo) ldapTemplate.searchForObject(DistinguishedName.EMPTY_PATH,filter.encode(),new GrupoContextMapper());	
//	}
//
//	/**
//	 * Habilita el Usuario.
//	 */	
//	private void habilitarUsuario(UsuarioLDAP usuario) throws RepositoryException{
//		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(usuario));
//		String userAccountControlStr = userContextOperations.getStringAttribute(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME);
//		int newUserAccountControl = Integer.parseInt(userAccountControlStr) & ~ConstantesAD.FLAG_TO_DISABLE_USER;
//		userContextOperations.setAttributeValue(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME, "" + newUserAccountControl);
//		ldapTemplate.modifyAttributes(userContextOperations);
//	}
//
//
//	/**
//	 * Deshabilita el Usuario.
//	 */
//	private void deshabilitarUsuario(UsuarioLDAP usuario) throws RepositoryException{
//		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(usuario));
//		String userAccountControlStr = userContextOperations.getStringAttribute(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME);
//		int newUserAccountControl = Integer.parseInt(userAccountControlStr) | ConstantesAD.FLAG_TO_DISABLE_USER;
//		userContextOperations.setAttributeValue(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME, "" + newUserAccountControl);
//		ldapTemplate.modifyAttributes(userContextOperations);
//	}
//	
//	
//	/**
//	 * Autentifica el usuario. 
//	 */
//	private boolean login(UsuarioLDAP usuario) {
//		AndFilter filter = new AndFilter();
//		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("company",usuario.getEmplid()));
//		return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, filter.toString(), usuario.getClave());
//	}
//	
//}
//
//
