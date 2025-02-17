package com.coto.sga.infrastructure.persistence.ldap;

import junit.framework.TestCase;

import org.junit.Test;


public class ActiveDirectoryUtilsTest extends TestCase{


	private ActiveDirectoryUtils activeDirectoryUtils;
	
	@Override
	protected void setUp() throws Exception {
		activeDirectoryUtils=ActiveDirectoryUtils.getInstancia();
	}
	
	@Test
	public void testGetDnFrom() throws Exception {
			
		try {
			
			String valor=activeDirectoryUtils.getAccountExpiredDateValue(0);
			System.out.println(valor);
			//assertTrue(dn.equals(repositorioUsuarioLDAP.getDnFrom(usuario)));
			
			System.out.println(activeDirectoryUtils.getDateTimeFrom("129621366655509594"));
			
		} catch (Exception e) {			
			e.printStackTrace();
			throw e;
		}

	}		

}
