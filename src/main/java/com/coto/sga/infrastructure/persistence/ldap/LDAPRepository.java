package com.coto.sga.infrastructure.persistence.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathAware;

public abstract class LDAPRepository implements BaseLdapPathAware{

	protected LdapTemplate ldapTemplate;
	
	protected DistinguishedName basePath;
	private static final Logger logger = Logger.getLogger(LDAPRepository.class);
	
	//private ActiveDirectoryUtils activeDirectoryUtils = new ActiveDirectoryUtils();// Inyectar un singleton
	
	protected DistinguishedName getDnFrom(String name) {
		logger.info("getDnFrom:  "+name);
		return ActiveDirectoryUtils.getInstancia().getDnFrom(name);
	}
	
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		logger.info("ldapTemplate:  "+ldapTemplate);
		this.ldapTemplate = ldapTemplate;
	}
	
	public void setBaseLdapPath(DistinguishedName basePath) {
		logger.info("basePath:  "+basePath);
		this.basePath = basePath;
	}
	
	protected DirContextOperations retrieveEntry(final String dn, final String[] attributesToRetrieve) {
		logger.info("attributesToRetrieve:  "+attributesToRetrieve);
		logger.info("dn:  "+dn);
		return (DirContextOperations) ldapTemplate.executeReadOnly(new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws NamingException {
				Attributes attrs = ctx.getAttributes(dn, attributesToRetrieve);
				return new DirContextAdapter(attrs, new DistinguishedName(dn),
						new DistinguishedName(ctx.getNameInNamespace()));
			}
		});
	}
}
