package com.coto.sga.infrastructure.persistence.ldap;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.coto.sga.domain.model.usuario.UsuarioLDAP;


//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"classpath:operacionServiceImplTest-context.xml"})
public class UsuarioADRepositoryImplTest extends TestCase{


	//@Autowired
	//@Qualifier("repositorioUsuarioLDAP")
	private UsuarioADRepositoryImpl repositorioUsuarioLDAP;
	
	
	public void setRepositorioUsuarioLDAP(
			UsuarioADRepositoryImpl repositorioUsuarioLDAP) {
		this.repositorioUsuarioLDAP = repositorioUsuarioLDAP;
	}
	
	protected void setUp() throws Exception {	
		Resource resource = new ClassPathResource("applicationContext-test.xml");
		BeanFactory factory = new XmlBeanFactory(resource);
		repositorioUsuarioLDAP = (UsuarioADRepositoryImpl) factory.getBean("usuarioLDAPRepository");	
	}
	
	
	@Test
	public void testGetDnFrom() throws Exception {
			
		try {
			
			UsuarioLDAP usuario=new UsuarioLDAP();
			usuario.setEmplid("1");			
			repositorioUsuarioLDAP.getDnFrom(usuario);
			
		} catch (Exception e) {			
			e.printStackTrace();
			throw e;
		}

	}
	
	
	@Test
	public void testActualizarUsuario() throws Exception {
			
		try {
			
			UsuarioLDAP usuario=new UsuarioLDAP();
			usuario.setEmplid("222225");
			usuario.setUser_oprid("cdrosas");
			usuario.setDeptid("102");
			usuario.setDominio("test.com.ar");
			usuario.setRutaRelativa("_Usuarios/tst_Sucursales/tst_Cajas");
			usuario.setSga_puesto("puesto");
			
			repositorioUsuarioLDAP.actualizar(usuario);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}
	

}
