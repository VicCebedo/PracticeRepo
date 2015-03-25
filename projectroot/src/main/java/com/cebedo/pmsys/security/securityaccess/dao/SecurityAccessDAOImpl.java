package com.cebedo.pmsys.security.securityaccess.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.cebedo.pmsys.common.QueryUtils;
import com.cebedo.pmsys.security.securityaccess.model.SecurityAccess;

@Repository
public class SecurityAccessDAOImpl implements SecurityAccessDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(SecurityAccessDAOImpl.class);
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void create(SecurityAccess securityAccess) {
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(securityAccess);
		logger.info("[Create] SecurityAccess: " + securityAccess);
	}

	@Override
	public SecurityAccess getByID(long id) {
		Session session = this.sessionFactory.getCurrentSession();
		SecurityAccess securityAccess = (SecurityAccess) session.createQuery(
				"from " + SecurityAccess.class.getName() + " where "
						+ SecurityAccess.COLUMN_PRIMARY_KEY + "=" + id)
				.uniqueResult();
		logger.info("[Get by ID] SecurityAccess: " + securityAccess);
		return securityAccess;
	}

	@Override
	public void update(SecurityAccess securityAccess) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(securityAccess);
		logger.info("[Update] SecurityAccess:" + securityAccess);
	}

	@Override
	public void delete(long id) {
		Session session = this.sessionFactory.getCurrentSession();
		SecurityAccess securityAccess = (SecurityAccess) session.load(
				SecurityAccess.class, new Long(id));
		if (securityAccess != null) {
			session.delete(securityAccess);
		}
		logger.info("[Delete] SecurityAccess: " + securityAccess);
	}

	@SuppressWarnings("unchecked")
	public List<SecurityAccess> list(Long companyID) {
		Session session = this.sessionFactory.getCurrentSession();
		List<SecurityAccess> securityAccessList = QueryUtils
				.getSelectQueryFilterCompany(session,
						SecurityAccess.class.getName(), companyID).list();
		for (SecurityAccess securityAccess : securityAccessList) {
			logger.info("[List] SecurityAccess: " + securityAccess);
		}
		return securityAccessList;
	}

}