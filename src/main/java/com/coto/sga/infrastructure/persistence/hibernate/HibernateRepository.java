package com.coto.sga.infrastructure.persistence.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HibernateRepository {

	  private SessionFactory sessionFactory;

	  public void setSessionFactory(final SessionFactory sessionFactory) {
	    this.sessionFactory = sessionFactory;
	  }

	  protected Session getSession() {
	    return sessionFactory.getCurrentSession();
	  }	
}
