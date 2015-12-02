package com.cebedo.pmsys.dao.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.LazyCollection;
import org.springframework.stereotype.Repository;

import com.cebedo.pmsys.dao.CompanyDAO;
import com.cebedo.pmsys.helper.AuthHelper;
import com.cebedo.pmsys.helper.DAOHelper;
import com.cebedo.pmsys.model.AuditLog;
import com.cebedo.pmsys.model.Company;

@Repository
public class CompanyDAOImpl implements CompanyDAO {

    private AuthHelper authHelper = new AuthHelper();
    private DAOHelper daoHelper = new DAOHelper();
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
	this.sessionFactory = sessionFactory;
    }

    @Override
    public void create(Company company) {
	Session session = this.sessionFactory.getCurrentSession();
	session.persist(company);
    }

    /**
     * Load all lazy collections.<br>
     * Reference:
     * http://stackoverflow.com/questions/24327353/initialize-all-lazy-loaded-
     * collections-in-hibernate
     * 
     * @param tClass
     * @param entity
     */
    private <T> void forceLoadLazyCollections(Class<T> tClass, T entity) {
	if (entity == null) {
	    return;
	}
	for (Field field : tClass.getDeclaredFields()) {
	    LazyCollection annotation = field.getAnnotation(LazyCollection.class);
	    if (annotation != null) {
		try {
		    field.setAccessible(true);
		    Hibernate.initialize(field.get(entity));
		} catch (IllegalAccessException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    @Override
    public Company getByIDWithLazyCollections(long id) {
	Session session = this.sessionFactory.getCurrentSession();
	Criteria criteria = this.daoHelper.criteriaGetObjByID(session, Company.class,
		Company.PROPERTY_ID, id);
	Company company = (Company) criteria.uniqueResult();
	forceLoadLazyCollections(Company.class, company);
	return company;
    }

    @Override
    public Company getByID(long id) {
	Session session = this.sessionFactory.getCurrentSession();
	Criteria criteria = this.daoHelper.criteriaGetObjByID(session, Company.class,
		Company.PROPERTY_ID, id);
	Company company = (Company) criteria.uniqueResult();
	return company;
    }

    @Override
    public void update(Company company) {
	Session session = this.sessionFactory.getCurrentSession();
	session.update(company);
    }

    @Override
    public void delete(long id) {
	Session session = this.sessionFactory.getCurrentSession();
	Company company = getByID(id);
	if (company != null) {
	    session.delete(company);
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Company> list(Long companyID) {
	Session session = this.sessionFactory.getCurrentSession();
	List<Company> companyList = this.daoHelper
		.getSelectQueryFilterCompany(session, Company.class.getName(), companyID).list();
	return companyList;
    }

    @Override
    public long getCompanyIDByObjID(String objTable, String objKeyCol, long objID) {
	Session session = this.sessionFactory.getCurrentSession();
	String qStr = "SELECT " + Company.COLUMN_PRIMARY_KEY + " FROM " + objTable + " WHERE "
		+ objKeyCol + " =:" + objKeyCol + " LIMIT 1";
	SQLQuery query = session.createSQLQuery(qStr);
	query.setParameter(objKeyCol, objID);
	String resultStr = query.uniqueResult().toString();
	return Long.parseLong(resultStr);
    }

    @Override
    public Company getCompanyByObjID(String objTable, String objKeyCol, long objID) {
	long companyID = getCompanyIDByObjID(objTable, objKeyCol, objID);
	return getByID(companyID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AuditLog> logs(Long companyID) {
	if (!this.authHelper.isCompanyAdmin() && !this.authHelper.isSuperAdmin()) {
	    return new ArrayList<AuditLog>();
	}
	Session session = this.sessionFactory.getCurrentSession();
	List<AuditLog> logs = this.daoHelper
		.getSelectQueryFilterCompany(session, AuditLog.class.getName(), companyID).list();
	return (List<AuditLog>) logs;
    }

}
