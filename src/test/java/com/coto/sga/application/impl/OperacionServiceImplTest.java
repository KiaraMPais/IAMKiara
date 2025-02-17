package com.coto.sga.application.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.coto.sga.application.OperacionService;


@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"classpath:operacionServiceImplTest-context.xml"})
public class OperacionServiceImplTest extends /*AbstractSpringContextTests*/ AbstractJUnit4SpringContextTests/*AbstractTransactionalJUnit4SpringContextTests/*TestCase*/{

		
  
	@Autowired
	private OperacionService operacionService;
	
	
	
	public void setOperacionService(OperacionService operacionService) {
		this.operacionService = operacionService;
	}
	
//	protected void setUp() throws Exception {	
//		Resource resource = new ClassPathResource("OperacionServiceImplTest-context.xml");
//        BeanFactory factory = new XmlBeanFactory(resource);
//        System.out.println(factory.toString());
//        operacionService = (OperacionServiceImpl) factory.getBean("operacionService");				
//	}
	
	@Test
	public void testProcesarNovedades() throws Exception {
		
		
		
		try {
			
			operacionService.procesarNovedades(0L);
			
		} catch (Exception e) {			
			e.printStackTrace();
			throw e;
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
