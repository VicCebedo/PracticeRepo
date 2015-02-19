package com.cebedo.pmsys.projectfile.dao;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.cebedo.pmsys.projectfile.model.ProjectFile;

@Repository
public class ProjectFileDAOImpl implements ProjectFileDAO {

	private static final Logger logger = LoggerFactory
			.getLogger(ProjectFileDAOImpl.class);
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void create(ProjectFile projectFile) {
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(projectFile);
		logger.info("[Create] Project File: " + projectFile);
	}

	@Override
	public ProjectFile getByID(long id) {
		Session session = this.sessionFactory.getCurrentSession();
		ProjectFile projectFile = (ProjectFile) session.createQuery(
				"from " + ProjectFile.CLASS_NAME + " where "
						+ ProjectFile.COLUMN_PRIMARY_KEY + "=" + id)
				.uniqueResult();
		logger.info("[Get by ID] Project File: " + projectFile);
		return projectFile;
	}

	@Override
	public void update(ProjectFile projectFile) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(projectFile);
		logger.info("[Update] Project File:" + projectFile);
	}

	@Override
	public void delete(long id) {
		Session session = this.sessionFactory.getCurrentSession();
		ProjectFile projectFile = getByID(id);
		if (projectFile != null) {
			session.delete(projectFile);
		}
		logger.info("[Delete] Project File: " + projectFile);
	}

	@SuppressWarnings("unchecked")
	public List<ProjectFile> list() {
		Session session = this.sessionFactory.getCurrentSession();
		List<ProjectFile> projectFileList = session.createQuery(
				"from " + ProjectFile.CLASS_NAME).list();
		for (ProjectFile projectFile : projectFileList) {
			logger.info("[List] Project File: " + projectFile);
		}
		return projectFileList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectFile> listWithAllCollections() {
		Session session = this.sessionFactory.getCurrentSession();
		List<ProjectFile> fileList = session.createQuery(
				"from " + ProjectFile.CLASS_NAME).list();
		for (ProjectFile file : fileList) {
			Hibernate.initialize(file.getProject());
			Hibernate.initialize(file.getUploader());
			logger.info("[List] Project File: " + file);
		}
		return fileList;
	}

	@Override
	public void updateDescription(long fileID, String description) {
		Session session = this.sessionFactory.getCurrentSession();

		String hql = "UPDATE " + ProjectFile.CLASS_NAME
				+ " SET description = :description"
				+ " WHERE id = :projectfile_id";

		Query query = session.createQuery(hql);
		query.setParameter("description", description);
		query.setParameter("projectfile_id", fileID);
		query.executeUpdate();
	}

}