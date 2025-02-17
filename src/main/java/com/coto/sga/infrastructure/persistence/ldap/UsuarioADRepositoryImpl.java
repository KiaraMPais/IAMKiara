package com.coto.sga.infrastructure.persistence.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.OperationNotSupportedException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.coto.sga.application.util.CorrectoLevel;
import com.coto.sga.application.util.Properties;
import com.coto.sga.domain.model.exception.RepositoryException;
import com.coto.sga.domain.model.grupo.Grupo;
import com.coto.sga.domain.model.usuario.UsuarioLDAP;
import com.coto.sga.domain.model.usuario.UsuarioLDAPRepository;
import com.coto.sga.infrastructure.persistence.exception.ClaveUsuarioException;
import com.coto.sga.infrastructure.persistence.exception.GrupoDuplicadoException;
import com.coto.sga.infrastructure.persistence.exception.GrupoNoExisteException;
import com.coto.sga.infrastructure.persistence.exception.UsuarioDuplicadoException;
import com.coto.sga.infrastructure.persistence.exception.UsuarioNoExisteException;

@Transactional(value="transactionManagerLDAP",rollbackFor=Exception.class,propagation=Propagation.REQUIRED)
public class UsuarioADRepositoryImpl extends LDAPRepository implements UsuarioLDAPRepository{
	
	private static final Logger logger = Logger.getLogger(UsuarioADRepositoryImpl.class);	
	private final static String ORG_TRASH=Properties.getString("organizacion_usuarios_eliminados");

	private final static String URL=Properties.getString("URL");
	private final static String KEY=Properties.getString("KEY");
	private final static String USERNAME=Properties.getString("USERNAME");
	private final static String USERPASSWORD=Properties.getString("USERPASSWORD");
	private final static String USERDOMAIN=Properties.getString("USERDOMAIN");	
	private final static String MAXSENDSIZE=Properties.getString("MAXSENDSIZE");
	private final static String MAXRECEIVESIZE=Properties.getString("MAXRECEIVESIZE");
	private final static String RETENTIONPOLICY=Properties.getString("RETENTIONPOLICY");
	
	@Override
	public void crear(UsuarioLDAP usuarioAD)throws RepositoryException{	  
		
		logger.info("Creando un Usuario: "+usuarioAD.getUser_oprid());
		if (existeUsuario(usuarioAD)) {
			throw new UsuarioDuplicadoException(usuarioAD);
		}
		DistinguishedName dn=getDnNuevo(usuarioAD);			
		DirContextOperations context=new DirContextAdapter(dn);
		logger.info("Ruta Relativa y el nombre del nuevo Usuario: "+dn.toString());
		try{
			UsuarioContextMapperAD.mapToContext(usuarioAD, context);
			this.ldapTemplate.bind(context);
			logger.info("Mapeo Exitoso en el AD");		
			for (Grupo grupo : usuarioAD.getGrupos())
				agregarUsuarioAGrupo(usuarioAD, grupo);
		}
		catch (NameAlreadyBoundException e){
			throw new UsuarioDuplicadoException(usuarioAD, e);
		} catch (RepositoryException e) {
			throw e;
		} catch (Throwable e) {
			throw new RepositoryException("Hubo un problema al crear el usuario [" + usuarioAD.getUser_oprid() + "]", e);
		}
	}
	
	@Override
	public void accesoAlWebServiceExchange(UsuarioLDAP usuarioAD)
			throws RepositoryException {
		try{		
			logger.info("Ingresando al WebService addMailBox"); 
            String userActiveDirectoryAccount=usuarioAD.getEmplid();
            String alias=usuarioAD.getMail().getMail();   
            String eMailPri=usuarioAD.getMail().getEmailExterno()!=null ? usuarioAD.getMail().getEmailExterno():"";	
			String eMailSec=usuarioAD.getMail().getEmailInterno()!=null ? usuarioAD.getMail().getEmailInterno():"";

            String maxsendSize=(usuarioAD.getMail().getMaxsendSize()!=null && !usuarioAD.getMail().getMaxsendSize().trim().isEmpty())?           		
            		usuarioAD.getMail().getMaxsendSize():MAXSENDSIZE;

			String maxReceiveSize= (usuarioAD.getMail().getMaxReceiveSize()!=null && !usuarioAD.getMail().getMaxReceiveSize().trim().isEmpty())? 
					usuarioAD.getMail().getMaxReceiveSize():MAXRECEIVESIZE;
					
			String retentionPolicy= (usuarioAD.getMail().getRetentionPolicy()!=null && !usuarioAD.getMail().getRetentionPolicy().trim().isEmpty())? 
					usuarioAD.getMail().getRetentionPolicy():RETENTIONPOLICY;
			mostrarDatos(usuarioAD);	            
			
			String userPassword=decrypt(USERPASSWORD);			
			
			String url=URL+"/"+"addMailBox";
			
			url=url+"?key="+KEY+"&userName="+USERNAME+"&userPassword="+userPassword+"&userDomain="+USERDOMAIN+
			"&userActiveDirectoryAccount="+userActiveDirectoryAccount+"&alias="+alias+"&eMailPri="+eMailPri+"&eMailSec="+eMailSec+
			"&maxsendSize="+maxsendSize+"&maxReceiveSize="+maxReceiveSize+"&retentionPolicy="+retentionPolicy;
			
		//	logger.info("Conectandose al WebService: "+url);			
			
			HttpClient httpClient = new DefaultHttpClient();        		     				
			//HttpGet msj = new HttpGet("http://w000sisda85/activedirectoryws/Service.asmx/addMailBox?key=123456789&userName=adtest&userPassword=20151703&userDomain=central&userActiveDirectoryAccount=606038");
			HttpGet httpGet = new HttpGet(url);
			HttpResponse respHttp = httpClient.execute(httpGet);
		//	logger.info("Conectado al WebService: "+URL);
		    String respString = EntityUtils.toString(respHttp.getEntity());	
		    mensajeWebService(respString);
		    
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Fallo el WebService. Razon: "+e.getMessage());
			System.out.println("Fallo!!!"+e.getMessage());
		}	
	}
	
	private void mostrarDatos(UsuarioLDAP usuarioAD) {
		logger.info("usuarioAD.getMail().getEmailExterno():"+usuarioAD.getMail().getEmailExterno());
	    logger.info("usuarioAD.getMail().getEmailInterno():"+usuarioAD.getMail().getEmailInterno());
        logger.info("usuarioAD.getMail().getMaxsendSize():"+usuarioAD.getMail().getMaxsendSize()+"; MAXSENDSIZE: "+MAXSENDSIZE); 
        logger.info("usuarioAD.getMail().getMaxReceiveSize():"+usuarioAD.getMail().getMaxReceiveSize()+"; MAXRECEIVESIZE: "+MAXRECEIVESIZE); 
        logger.info("usuarioAD.getMail().getRetentionPolicy():"+usuarioAD.getMail().getRetentionPolicy()+"; RETENTIONPOLICY: "+RETENTIONPOLICY);        			
	}
	
	private String decrypt(String cadena) { 
		StandardPBEStringEncryptor cadenaAEncriptar = new StandardPBEStringEncryptor();  
//		cadenaAEncriptar.setPassword("Memberoff1");
		cadenaAEncriptar.setPassword(System.getenv("SGA_ENCRYPTION_PASSWORD"));
		cadenaAEncriptar.setAlgorithm("PBEWithMD5AndDES"); 
		String devuelve = ""; 
		try { 
//logger.info("Cadena Encriptada: "+cadena);
			devuelve = cadenaAEncriptar.decrypt(cadena); 
//			logger.info("Cadena Desencriptada: "+devuelve);
		} catch (Exception e) { 
			logger.info("Fallo al desencriptar"+devuelve);
			e.printStackTrace();
		} 
		return devuelve; 
	} 
     
	private void mensajeWebService(String respString) {
		int inicio = respString.indexOf("<CODIGO>"); 
		int fin = respString.indexOf("</CODIGO>"); 
		String codigo=respString.substring(inicio, fin+9);
   
		int inicioCodigo = respString.indexOf("<MENSAJE>"); 
		int finCodigo = respString.indexOf("</MENSAJE>"); 
		String mensaje=respString.substring(inicioCodigo, finCodigo+10);
  
		char[] toCharArray = codigo.toCharArray();
		int valor=0;
		for (int i = 0; i < codigo.length(); i++) {
			char caracter = toCharArray[i];
			if (Character.isDigit(caracter)) {
				valor=Integer.valueOf(""+caracter);
			     break;
			}
		}
		if (1==valor){
			logger.error("Devolvio un error el WebService: "+mensaje);
		}else {
			logger.log(CorrectoLevel.TRACE,"Finalizada correctamente. Email");
			logger.info("Fue exitoso lo que devolvio el WebService: "+mensaje);
		}
	}
	
	public void modificarClaveYEstado(UsuarioLDAP usuarioAD) throws RepositoryException {
		logger.info("Modificar la clave");
	    modificarClave(usuarioAD);  
		logger.info("Modificar la EstadoCuenta");
		modificarEstadoCuenta(usuarioAD);	
		logger.info("Fin de Modificar clave y EstadoCuenta");	
	}

	@Override
	public void actualizar(UsuarioLDAP usuarioAD) throws RepositoryException {
		logger.info("Ingresando a Actualizar");	
		try{
			
			DistinguishedName dn=getDnFrom(usuarioAD);
		    DirContextOperations context = ldapTemplate.lookupContext(dn);
			UsuarioContextMapperAD.mapToContextBasic(usuarioAD, context);
		    ldapTemplate.modifyAttributes(context);
		    
		    //Modifico la descripcion
		    ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description",usuarioAD.getDescripcion()));
			ldapTemplate.modifyAttributes(dn, new ModificationItem[]{item});		    		    

			UsuarioLDAP usuarioActual=obtenerUsuarioPorLegajo(usuarioAD);
			
		    // Actualizo los grupos
			for (Grupo grupo : usuarioActual.getGrupos()){
				// Los grupos que estan actualmente pero no en el nuevo los borro
				if (!usuarioAD.perteneceAGrupo(grupo)){
					borrarUsuarioDeGrupo(usuarioAD,grupo);				
				}
				// Los grupos que estan actualmente y ademas en el nuevo los deja. No hace nada.
			}
			
			for (Grupo grupo : usuarioAD.getGrupos()){
				// Los grupos que no estan actualmente pero si en el nuevo los agrega.
				if (!usuarioActual.perteneceAGrupo(grupo)){
					agregarUsuarioAGrupo(usuarioAD,grupo);
				}
			}
			
			//Reubico al usuario.			
			DistinguishedName dnCarpeta=new DistinguishedName();
			for (String ou : usuarioAD.getRutaRelativaSeparada()){
				dnCarpeta.add("ou",ou);
			}
			try{
				ldapTemplate.rename(dn.toString(),"cn="+getCnFrom(usuarioAD)+","+dnCarpeta.toString());
		    }catch (UncategorizedLdapException e) {//NameNotFoundException
		    	throw new RepositoryException("No existe la ruta ["+dnCarpeta.toString()+"].",e);
			}
			
		} catch (NameNotFoundException e){
			throw new UsuarioNoExisteException(usuarioAD,e);
		}catch (RepositoryException e) {
			throw e;
		} catch (Throwable e){
			throw new RepositoryException("Hubo un problema al actualizar el usuario [" + usuarioAD.getUser_oprid() + "]",e);
		}
	}	
	
	/**
	 * Oculta e Inhabilita completamente el usuario.
	 */
	@Override
	public void eliminar(UsuarioLDAP usuarioAD) throws RepositoryException {
		logger.info("Ingresando a Eliminar UsuarioLDAP");	
		try{
			
			DistinguishedName dn=getDnFrom(usuarioAD);
			if (dn==null) throw new UsuarioNoExisteException(usuarioAD);
					
			// Borro al usuario de sus grupos.
			UsuarioLDAP usuarioActual=obtenerUsuarioPorLegajo(usuarioAD); 
			for (Grupo grupo : usuarioActual.getGrupos()){
				borrarUsuarioDeGrupo(usuarioAD,grupo);
			}			
			
			//Dehabilito al usuario y lo oculto de la lista de direcciones.
			List<ModificationItem> userAttributes = new ArrayList<ModificationItem>();
			userAttributes.add(new ModificationItem(LdapContext.REPLACE_ATTRIBUTE,new BasicAttribute(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME,""+ConstantesAD.USER_CONTROL_DISABLE_USER)));//AD
			userAttributes.add(new ModificationItem(LdapContext.REPLACE_ATTRIBUTE,new BasicAttribute(ConstantesAD.HIDE_FROM_ADD_LISTS_ATTR_NAME,"TRUE")));//AD
			userAttributes.add(new ModificationItem(LdapContext.REPLACE_ATTRIBUTE,new BasicAttribute(ConstantesAD.SHOWINADDRESSBOOK,null)));//AD
			
			// Borro las direcciones proxy al usuario.
			List<String> direccionesProxy=getDireccionesProxy(dn.toString());
			for (String dir : direccionesProxy) {
				userAttributes.add(new ModificationItem(LdapContext.REMOVE_ATTRIBUTE,new BasicAttribute(ConstantesAD.PROXY_ADDRESSES_ATTR_NAME,dir)));
			}		
			ldapTemplate.modifyAttributes(dn,userAttributes.toArray(new ModificationItem[userAttributes.size()]));		
		
			// Muevo el usuario a trash.
			String dnCarpeta = "ou="+usuarioAD.getUser_oprid().substring(0,1).toUpperCase()+",ou="+ORG_TRASH;
			try{
				
				ldapTemplate.lookup(dnCarpeta);
				
			}catch (NameNotFoundException e) {// Creo la carpeta si no existe.
			    Attributes atributos = new BasicAttributes(true);
			    Attribute objclass = new BasicAttribute(ConstantesAD.OBJECT_CLASS_ATTR_NAME);
			    objclass.add("top");
			    objclass.add("organizationalUnit");
			    atributos.put(objclass);
			    try{
			    	ldapTemplate.bind(dnCarpeta,null,atributos);
			    }catch (NameNotFoundException ex) {
			    	throw new RepositoryException("No existe la organizacion ["+ORG_TRASH+"].",ex);
				}
			}

			ldapTemplate.rename(dn.toString(),"cn="+usuarioAD.getUser_oprid()+","+dnCarpeta);	
	
			this.delMailBoxWebServiceExchange(usuarioAD);	
		}catch (RepositoryException e){
			throw e;
		} catch (Throwable e){
			throw new RepositoryException("Hubo un problema al eliminar el usuario [" + usuarioAD.getUser_oprid() + "]",e);
		}
	}
	
	private void delMailBoxWebServiceExchange(UsuarioLDAP usuarioAD)throws RepositoryException {
		try{		
			logger.info("Ingresando al WebService delMailBox"); 
            String userActiveDirectoryAccount=usuarioAD.getEmplid();

            String userPassword=decrypt(USERPASSWORD);
            
			String url=URL+"/"+"delMailBox";
			url=url+"?key="+KEY+"&userName="+USERNAME+"&userPassword="+userPassword+"&userDomain="+USERDOMAIN+
			"&userActiveDirectoryAccount="+userActiveDirectoryAccount;
			//logger.info("Conectandose al WebService: "+url);			
	
			HttpClient httpClient = new DefaultHttpClient(); 
			HttpGet msj = new HttpGet(url);
			HttpResponse respHttp = httpClient.execute(msj);		       
			//logger.info("Conectado al WebService: "+url);
		    String respString = EntityUtils.toString(respHttp.getEntity());	
		    mensajeWebService(respString);
		    
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Fallo el WebService");
			logger.error("Fallo!!!"+e.getMessage());
		}	
	}

	/**
	 * Agrega el usuario al grupo.
	 * 
	 * El usuario debe estar creado.
	 */
	public void agregarUsuarioAGrupo(UsuarioLDAP usuario,Grupo grupo) throws RepositoryException{
		
		String dnUserFull;
		try{			
			DirContextOperations dirContext = ldapTemplate.lookupContext(getDnFrom(usuario));			
			dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);			
		}catch (NamingException e) {
			throw new UsuarioNoExisteException(usuario);
		}
					
		try {
			
			DirContextOperations groupContextOperations = getDirContextOperationsFrom(grupo);
			String[] currentMembers = groupContextOperations.getStringAttributes(ConstantesAD.MEMBER_ATTR_NAME);
			List<String> dnUserFullList = new ArrayList<String>();
			if (currentMembers != null && currentMembers.length > 0) {
				dnUserFullList.addAll(Arrays.asList(currentMembers));
			}
			dnUserFullList.add(dnUserFull);
			groupContextOperations.setAttributeValues(ConstantesAD.MEMBER_ATTR_NAME, dnUserFullList.toArray(new String[dnUserFullList.size()]));
			ldapTemplate.modifyAttributes(groupContextOperations);
					
		}catch (RepositoryException e) {
			throw e;		
		} catch (Throwable e) {
			throw new RepositoryException("Hubo un problema agregando el usuario ["+usuario.getUser_oprid()+"] al grupo ["+grupo.getNombre()+"]",e);
		}
	}
	

	/**
	 * Borra el Usuario del Grupo.
	 * 
	 * El Usuario debe estar creado.
	 */
	public void borrarUsuarioDeGrupo(UsuarioLDAP usuario, Grupo grupo) throws RepositoryException {
		
		String dnUserFull;
		try {
			DirContextOperations dirContext = ldapTemplate.lookupContext(getDnFrom(usuario));
			dnUserFull = dirContext.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME);
		}catch (NamingException e) {
			throw new UsuarioNoExisteException(usuario);
		}
		
		try{
			DirContextOperations groupContextOperations = getDirContextOperationsFrom(grupo);
			String[] currentMembers = groupContextOperations.getStringAttributes(ConstantesAD.MEMBER_ATTR_NAME);
			List<String> dnUserFullList = new ArrayList<String>();
			if (currentMembers != null && currentMembers.length > 0) {
				dnUserFullList.addAll(Arrays.asList(currentMembers));
			}
			dnUserFullList.remove(dnUserFull);
			groupContextOperations.setAttributeValues(ConstantesAD.MEMBER_ATTR_NAME, dnUserFullList.toArray(new String[dnUserFullList.size()]));
			ldapTemplate.modifyAttributes(groupContextOperations);
		
		}catch (RepositoryException e) {
			throw e;
		} catch (Throwable e) {
			throw new RepositoryException("Hubo un problema borrando el usuario ["+usuario.getUser_oprid()+"] del grupo ["+grupo.getNombre()+"]",e);
		}
	}
	
	/**
	 * Modifica el estado de la cuenta de usuario dependiendo de su estado actual.
	 * El usuario debe estar creado en AD. 
	 */
	
	public void modificarEstadoCuenta(UsuarioLDAP usuario) throws RepositoryException{
		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(usuario));		
	//	userContextOperations.setAttributeValue(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME, ""+ConstantesAD.USER_CONTROL_NORMAL_USER);//AD
		userContextOperations.setAttributeValue(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME, "" + ActiveDirectoryUtils.getInstancia().getUserAccountControl(usuario));
	    userContextOperations.setAttributeValue(ConstantesAD.PWD_LAST_SET_ATTR_NAME, "0");		
		ldapTemplate.modifyAttributes(userContextOperations);		
	}
	
	/**
	 * Modifica la clave del usuario con la clave asignada actualmente.
	 * El usuario debe estar creado en AD.
	 */
	private void modificarClave(UsuarioLDAP usuario) throws RepositoryException{
		try {
			ModificationItem item = 
				new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(ConstantesAD.PASSWORD_ATTR_NAME, 
				ActiveDirectoryUtils.getInstancia().encodePassword(usuario.getClave())));
	
			ldapTemplate.modifyAttributes(getDnFrom(usuario), new ModificationItem[]{item});
		} catch (OperationNotSupportedException e) {
			throw new ClaveUsuarioException(usuario,e);
		} catch (Throwable e) {
			 throw new RepositoryException("Hubo un problema a modificar la clave del usuario [" + usuario.getUser_oprid() + "]",e);
		}
	}
 
 
	private List<String> getDireccionesProxy(String dnUsuario){
		String[] atributo={ConstantesAD.PROXY_ADDRESSES_ATTR_NAME};
		DirContextOperations dir=retrieveEntry(dnUsuario,atributo);
		String[] dirProxies=dir.getStringAttributes(atributo[0]);
		if (dirProxies==null) return new ArrayList<String>();
		return Arrays.asList(dirProxies);
	}
		
	/**
	 * Carga los grupos desde el repositorio dentro del usuario. 
	 */
	private void cargarGruposEnUsuario(UsuarioLDAP usuario) throws RepositoryException{
		DirContextOperations dirContext = ldapTemplate.lookupContext(getDnFrom(usuario));
		String[] dnGrupos = dirContext.getStringAttributes(ConstantesAD.MEMBER_OF_ATTR_NAME);	
		if (dnGrupos!=null){
			for (String dnGrupo : dnGrupos) {			
				usuario.addGrupo(obtenerGrupoPorFullDn(dnGrupo));
			}
		}
	}
	

	/**
	 * Obtiene el usuario por su legajo, junto a sus grupos.
	 * El legajo en el usuario debe estar cargado.
	 */
	private UsuarioLDAP obtenerUsuarioPorLegajo(UsuarioLDAP usuario) throws RepositoryException {				
		UsuarioLDAP user=(UsuarioLDAP) ldapTemplate.lookup(getDnFrom(usuario),new UsuarioContextMapperAD());
		cargarGruposEnUsuario(user);
		return user;		
	}
		
	
	/**
	 * Obtiene el DN del usuario. Busca el DN por legajo.
	 * EL usuario debe tener el legajo cargado (company). 
	 * Devuelve NULL si no existe.
	 */		
	public DistinguishedName getDnFrom(UsuarioLDAP usuario) throws UsuarioNoExisteException{
		List<DirContextAdapter> cns=getDirContextAdaptersFrom(usuario);	
		DirContextAdapter context=null;
		for (DirContextAdapter ctx:cns){
			logger.info("getDnFrom del for : "+ctx.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME));
			if (!ctx.getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME).contains("OU="+ORG_TRASH)){
				context=ctx;
				break;
			}
		}		
		if (context==null) return null;
		
		String fullDn=context.getDn().toString();
		logger.info("fullDn: "+fullDn);
		logger.info("basePath: "+basePath.toString());
		String rdn=fullDn.replace(basePath.toString(),"");
		logger.info("obtener el relativeDn: "+rdn);
		return new DistinguishedName(rdn);
	}
	
	/**
	 * Obtiene el CN del usuario. Busca el CN por legajo.
	 */
	@SuppressWarnings("unchecked")
	private String getCnFrom(UsuarioLDAP usuario){		
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		AndFilter filter = new AndFilter();
	//	filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "person")).and(new EqualsFilter("company", usuario.getEmplid()));		
		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "user")).and(new EqualsFilter("SAMAccountName",usuario.getUser_oprid()));
		List<String[]> cns=ldapTemplate.search(DistinguishedName.EMPTY_PATH,filter.encode(), searchControls,
					new ContextMapper(){
			public String[] mapFromContext(Object ctx){
				return new String[]{((DirContextAdapter) ctx).getStringAttribute(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME),((DirContextAdapter) ctx).getStringAttribute("cn")};				
			}
		});
			
		for (String[] dnCn : cns) {
			if (!dnCn[0].contains("OU="+ORG_TRASH)){
				return dnCn[1];				
			}
		}
		
		return null;
	}
	
	/*
	 * Obtiene el DN a generarse del usuario. 
	 */
	private DistinguishedName getDnNuevo(UsuarioLDAP usuario){
		DistinguishedName dn=new DistinguishedName();			
		// 20120816 		
		String cn = "";
		String ouTem="";
		try {
			 // cn = NovedadRepositoryHibernateHelper.getInstancia().reemplazarVariables(usuario.getNombreAMostrar(),usuario);
			 cn = usuario.getEmplid()+"."+usuario.getNombreCompleto();
			 logger.info("cn= "+cn);
		} catch (Exception e) {
			e.printStackTrace();
		}			
		// Organizacion		
		for (String ou : usuario.getRutaRelativaSeparada()){
			 ouTem=ouTem+ou;
			dn.add("ou",ou);
		}
		logger.info("El usuario Tiene la Ruta Relativa: "+ouTem);
		// Contenedor
		dn.add("cn",cn);
		return dn;
	}
	
	public boolean existeUsuario(UsuarioLDAP usuario){
		return !getDirContextAdaptersFrom(usuario).isEmpty();
	}

	/**
	 * Obtiene la lista de dirContextAdapter de todos los usuarios con el mismo legajo,
	 * incluyendo los usuarios ocultos (en la carpeta Trash).
	 */
	@SuppressWarnings("unchecked")
	private List<DirContextAdapter> getDirContextAdaptersFrom(UsuarioLDAP usuario) {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		logger.info("DistinguishedName.EMPTY_PATH: "+DistinguishedName.EMPTY_PATH.toString());
		logger.info("Company: "+usuario.getEmplid());
		logger.info("Nombre de Usuario de Red: "+usuario.getUser_oprid());
		AndFilter filter = new AndFilter();
	//	filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "person")).and(new EqualsFilter("company", usuario.getEmplid()));		
		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "user")).and(new EqualsFilter("SAMAccountName",usuario.getUser_oprid()));
		
		List<DirContextAdapter> cns=ldapTemplate.search(DistinguishedName.EMPTY_PATH,filter.encode(), searchControls,
					new ContextMapper(){
			public DirContextAdapter mapFromContext(Object ctx){
				return (DirContextAdapter) ctx;				
			}
		});
		return cns;
	}
	
	private DirContextOperations getDirContextOperationsFrom(Grupo grupo) throws RepositoryException{

		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "group")).and(new EqualsFilter("cn", grupo.getNombre()));
		
		try{
		
			return (DirContextOperations) ldapTemplate.searchForObject(DistinguishedName.EMPTY_PATH,filter.encode(),
				new ContextMapper(){
					public DirContextOperations mapFromContext(Object ctx){
						return (DirContextOperations) ctx;
					}
				});
			
		}catch (NameNotFoundException e) {
			throw new GrupoNoExisteException(grupo,e);
		}catch (EmptyResultDataAccessException e) {
			throw new GrupoNoExisteException(grupo,e);
		}catch (IncorrectResultSizeDataAccessException e) {
			throw new GrupoDuplicadoException(grupo,e);
		}
	}
		
	private Grupo obtenerGrupoPorFullDn(String fullDn){
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(ConstantesAD.OBJECT_CLASS_ATTR_NAME, "group")).and(new EqualsFilter(ConstantesAD.DISTINGUISHED_NAME_ATTR_NAME, fullDn));		
		return (Grupo) ldapTemplate.searchForObject(DistinguishedName.EMPTY_PATH,filter.encode(),new GrupoContextMapper());	
	}

	/**
	 * Habilita el Usuario.
	 */	
	@SuppressWarnings("unused")
	private void habilitarUsuario(UsuarioLDAP usuario) throws RepositoryException{
		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(usuario));
		String userAccountControlStr = userContextOperations.getStringAttribute(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME);
		int newUserAccountControl = Integer.parseInt(userAccountControlStr) & ~ConstantesAD.FLAG_TO_DISABLE_USER;
		userContextOperations.setAttributeValue(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME, "" + newUserAccountControl);
		ldapTemplate.modifyAttributes(userContextOperations);
	}
	/**
	 * Deshabilita el Usuario.
	 */
	@SuppressWarnings("unused")
	private void deshabilitarUsuario(UsuarioLDAP usuario) throws RepositoryException{
		DirContextOperations userContextOperations = ldapTemplate.lookupContext(getDnFrom(usuario));
		String userAccountControlStr = userContextOperations.getStringAttribute(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME);
		int newUserAccountControl = Integer.parseInt(userAccountControlStr) | ConstantesAD.FLAG_TO_DISABLE_USER;
		userContextOperations.setAttributeValue(ConstantesAD.USER_ACCOUNT_CONTROL_ATTR_NAME, "" + newUserAccountControl);
		ldapTemplate.modifyAttributes(userContextOperations);
	}
	
	
	/**
	 * Autentifica el usuario. 
	 */
	@SuppressWarnings("unused")
	private boolean login(UsuarioLDAP usuario) {
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("company",usuario.getEmplid()));
		return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, filter.toString(), usuario.getClave());
	}
	
}