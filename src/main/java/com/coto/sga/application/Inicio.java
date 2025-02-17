package com.coto.sga.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.coto.sga.application.util.ConstantesLog;

public class Inicio {

	private static final Log logger = LogFactory.getLog(Inicio.class);	
	
	public static void main(String[] args) 
	{
		if (args.length==0)
		{
			logger.error("Se debe especificar el parametro Numero de Instancia.");
			return;
		}
		Long instanciaProceso=Long.valueOf(args[0]);
		
		MDC.put(ConstantesLog.MDC_INSTANCIA_PROCESO,instanciaProceso);
		MDC.put(ConstantesLog.MDC_NRO_SOLICITUD,"0");
		MDC.put(ConstantesLog.MDC_LEGAJO," ");
		MDC.put(ConstantesLog.MDC_APLICACION," ");
		
		logger.info("Inicio Aplicacion.");
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("applicationContext.xml");
 	    OperacionService operacionService=(OperacionService) springContext.getBean("operacionService");
		operacionService.procesarNovedades(instanciaProceso);
		logger.info("Fin de Aplicacion.");		

	}
}
