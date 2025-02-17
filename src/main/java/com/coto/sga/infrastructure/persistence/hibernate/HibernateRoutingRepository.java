package com.coto.sga.infrastructure.persistence.hibernate;

import org.hibernate.Session;

public class HibernateRoutingRepository {
	
	private SessionBeanFactory sessions;

					
	protected Session getSession(String session) {					
//		return sessions.getSessionFactory(session).getCurrentSession();
		return sessions.getSessionFactory(session).openSession();
	}
	
	public void setSessions(SessionBeanFactory sessions) {
		this.sessions = sessions;
	}

}
