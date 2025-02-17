package com.coto.sga.infrastructure.persistence.hibernate;

import java.util.Map;

import org.hibernate.SessionFactory;

public class SessionBeanFactory {

	private Map<String,SessionFactory> sessionsFactory;
	
		
	public SessionFactory getSessionFactory(String nombre){
		return (SessionFactory) sessionsFactory.get(nombre);
	}
		
	public void setSessionsFactory(Map<String, SessionFactory> sessionsFactory) {
		this.sessionsFactory = sessionsFactory;
	}
}
