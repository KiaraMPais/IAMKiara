package com.coto.sga.application.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.coto.sga.application.OperacionService;
import com.coto.sga.domain.model.novedad.Novedad;
import com.coto.sga.domain.model.novedad.NovedadRepository;

/**
 * Implementacion de OperacionService.
 */
public class OperacionServiceImpl implements OperacionService {


  private final Log logger = LogFactory.getLog(getClass());
  
  @Autowired
  private NovedadRepository novedadRepository;
  
  
  public OperacionServiceImpl() {
  }
  
  public OperacionServiceImpl(final NovedadRepository novedadRepository) {
	  this.novedadRepository=novedadRepository;
  }

  public void procesarNovedades(Long instanciaProceso){
	  try{
		  List<Novedad> novedades=novedadRepository.obtenerNovedadesAprobadas(instanciaProceso);
		  logger.info("Cantidad de novedades a procesarse: "+novedades.size());
		  
		  for (Novedad novedad : novedades) {
			  novedad.procesar();
		  }
		  
	  }catch (Exception e){
		  logger.error(e.getMessage(),e);		  
	  }
  }
  
  public NovedadRepository getNovedadRepository() {
	return novedadRepository;
  }
	
  public void setNovedadRepository(NovedadRepository novedadRepository) {
		this.novedadRepository = novedadRepository;
  }

}
