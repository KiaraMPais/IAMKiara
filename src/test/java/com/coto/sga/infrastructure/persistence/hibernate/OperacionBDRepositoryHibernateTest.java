package com.coto.sga.infrastructure.persistence.hibernate;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration //Busca el archivo OperacionBDRepositoryHibernateTest-context.xml
//@ContextConfiguration(locations={"classpath:context-infrastructure-persistence.xml"})
@TransactionConfiguration(transactionManager="transactionManager1")//defaultRollback=true.
@Transactional
public class OperacionBDRepositoryHibernateTest extends AbstractTransactionalJUnit4SpringContextTests{

	
	//private OperacionBDRepository operacionBDRepository;

	//@Autowired
	//private OperacionBDRepository operacionBDRepository;
  
	
	private class TipoBase{
		private Long id;
		private String descripcion;
		
		public void setDescripcion(String descripcion) {
			this.descripcion = descripcion;
		}
		
		public String getDescripcion() {
			return descripcion;
		}

		public void setId(Long id) {
			this.id = id;
		}
		
		public Long getId() {
			return id;
		}
	}
	
	
//	protected void setUp() throws Exception {
//	
//		Resource resource = new ClassPathResource("context-infrastructure-persistence-2.xml");
//        BeanFactory factory = new XmlBeanFactory(resource);
//        System.out.println(factory.toString());
//        operacionBDRepository = (OperacionBDRepositoryHibernate) factory.getBean("hibernateRoutingRepository");		
//		
//	}
	
	@Test
	public void testEjecutar() {
		
		String consulta="insert into CDV_TIPOBASE VALUES(:id,:descripcion,'N',sysdate)";
		
		TipoBase tipo=new TipoBase();
		tipo.setDescripcion("nueva");
		tipo.setId(Long.valueOf(3));
		
		try {
			
			//operacionBDRepository.ejecutar(consulta, tipo,"SF");
			
		} catch (Exception e) {			
			e.printStackTrace();
		}

	}

	/*
	public void testFindByCargoIdUnknownId() {
		assertNull(cargoRepository.find(new TrackingId("UNKNOWN")));
	}	

*/

/*	public void testFindAll() {
		List<Cargo> all = cargoRepository.findAll();
		assertNotNull(all);
		assertEquals(6, all.size());
	}

	public void testNextTrackingId() {
		TrackingId trackingId = cargoRepository.nextTrackingId();
		assertNotNull(trackingId);

		TrackingId trackingId2 = cargoRepository.nextTrackingId();
		assertNotNull(trackingId2);
		assertFalse(trackingId.equals(trackingId2));
	}*/

}