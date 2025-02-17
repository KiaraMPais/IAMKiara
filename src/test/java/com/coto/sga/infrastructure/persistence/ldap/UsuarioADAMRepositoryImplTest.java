package com.coto.sga.infrastructure.persistence.ldap;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.DistinguishedName;

import com.coto.sga.domain.model.usuario.UsuarioLDAP;


//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"classpath:operacionServiceImplTest-context.xml"})
public class UsuarioADAMRepositoryImplTest extends TestCase{


	//@Autowired
	//@Qualifier("repositorioUsuarioLDAP")
	private UsuarioADAMRepositoryImpl repositorioUsuarioLDAP;
	
	
	public void setRepositorioUsuarioLDAP(
			UsuarioADAMRepositoryImpl repositorioUsuarioLDAP) {
		this.repositorioUsuarioLDAP = repositorioUsuarioLDAP;
	}
	
	protected void setUp() throws Exception {	
		Resource resource = new ClassPathResource("applicationContext-test.xml");
		BeanFactory factory = new XmlBeanFactory(resource);
		repositorioUsuarioLDAP = (UsuarioADAMRepositoryImpl) factory.getBean("usuarioLDAPRepository");	
	}
	
	
	@Test
	public void testGetDnFrom() throws Exception {
			
		try {
			
			UsuarioLDAP usuario=new UsuarioLDAP();
			usuario.setEmplid("1");
			DistinguishedName dn=new DistinguishedName("CN=as,DC=test,DC=com,DC=ar");
			assertTrue(dn.equals(repositorioUsuarioLDAP.getDnFrom(usuario)));
			
		} catch (Exception e) {			
			e.printStackTrace();
			throw e;
		}

	}

}
