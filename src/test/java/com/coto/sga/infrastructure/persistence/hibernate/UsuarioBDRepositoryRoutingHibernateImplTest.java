package com.coto.sga.infrastructure.persistence.hibernate;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.coto.sga.domain.model.usuario.UsuarioBD;


//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"classpath:operacionServiceImplTest-context.xml"})
public class UsuarioBDRepositoryRoutingHibernateImplTest extends TestCase{


	//@Autowired
	//@Qualifier("repositorioUsuarioLDAP")
	private UsuarioBDRepositoryRoutingHibernateImpl repositorioUsuarioBD;
	
	
	public void setRepositorioUsuarioBD(
			UsuarioBDRepositoryRoutingHibernateImpl repositorioUsuarioBD) {
		this.repositorioUsuarioBD = repositorioUsuarioBD;
	}
	
	protected void setUp() throws Exception {	
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("applicationContext-test.xml");
		repositorioUsuarioBD = (UsuarioBDRepositoryRoutingHibernateImpl) springContext.getBean("usuarioBDRepository");	
	}
	
	
	@Test
	public void testEjecutarConsultas() throws Exception {
			
		try {
			
			List<String> consultas=new ArrayList<String>();
			UsuarioBD usuario=new UsuarioBD();
			consultas.add("update ps_sga_novedad set REQ_STATUS='F' where EMPLID='120868' and ID_SOLICITUD=1");			
			repositorioUsuarioBD.ejecutar(consultas,usuario,"PS");
			
//			consultas.add("update ps_sga_novedad set REQ_STATUS='F' where EMPLID='120868' and ID_SOLICITUD=1");			
//			repositorioUsuarioBD.ejecutar(consultas,usuario,"PS");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
