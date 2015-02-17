package com.cebedo.pmsys.task.dao;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.cebedo.pmsys.task.model.Task;

@Repository
public class TaskDAOImpl implements TaskDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskDAOImpl.class);
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void create(Task task) {
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(task);
		logger.info("[Create] Task: " + task);
	}

	@Override
	public Task getByID(long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Task task = (Task) session.createQuery(
				"from " + Task.CLASS_NAME + " where " + Task.COLUMN_PRIMARY_KEY
						+ "=" + id).uniqueResult();
		logger.info("[Get by ID] Task: " + task);
		return task;
	}

	@Override
	public void update(Task task) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(task);
		logger.info("[Update] Task:" + task);
	}

	@Override
	public void delete(long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Task task = getByID(id);
		if (task != null) {
			session.delete(task);
		}
		logger.info("[Delete] Task: " + task);
	}

	@SuppressWarnings("unchecked")
	public List<Task> list() {
		Session session = this.sessionFactory.getCurrentSession();
		Query query = session.createQuery("from " + Task.CLASS_NAME);
		List<Task> taskList = query.list();
		for (Task task : taskList) {
			logger.info("[List] Task: " + task);
		}
		return taskList;
	}

	@SuppressWarnings("unchecked")
	public List<Task> listWithAllCollections() {
		Session session = this.sessionFactory.getCurrentSession();
		Query query = session.createQuery("from " + Task.CLASS_NAME);
		List<Task> taskList = query.list();
		for (Task task : taskList) {
			Hibernate.initialize(task.getTeam());
			Hibernate.initialize(task.getProject());
			Hibernate.initialize(task.getStaff());
			logger.info("[List] Task: " + task);
		}
		return taskList;
	}

}