package com.cebedo.pmsys.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cebedo.pmsys.dao.CompanyDAO;
import com.cebedo.pmsys.dao.ProjectDAO;
import com.cebedo.pmsys.dao.TeamDAO;
import com.cebedo.pmsys.helper.AuthHelper;
import com.cebedo.pmsys.model.Company;
import com.cebedo.pmsys.model.Project;
import com.cebedo.pmsys.model.Team;
import com.cebedo.pmsys.model.assignment.TeamAssignment;
import com.cebedo.pmsys.token.AuthenticationToken;
import com.cebedo.pmsys.wrapper.TeamWrapper;

@Service
public class TeamServiceImpl implements TeamService {

	private AuthHelper authHelper = new AuthHelper();
	private TeamDAO teamDAO;
	private ProjectDAO projectDAO;
	private CompanyDAO companyDAO;

	public void setCompanyDAO(CompanyDAO companyDAO) {
		this.companyDAO = companyDAO;
	}

	public void setProjectDAO(ProjectDAO projectDAO) {
		this.projectDAO = projectDAO;
	}

	public void setTeamDAO(TeamDAO teamDAO) {
		this.teamDAO = teamDAO;
	}

	@Override
	@Transactional
	public void create(Team team) {
		AuthenticationToken auth = this.authHelper.getAuth();
		Company authCompany = auth.getCompany();
		team.setCompany(authCompany);
		this.teamDAO.create(team);
	}

	@Override
	@Transactional
	public Team getByID(long id) {
		Team team = this.teamDAO.getByID(id);
		if (this.authHelper.isActionAuthorized(team)) {
			return team;
		}
		return new Team();
	}

	@Override
	@Transactional
	public void update(Team team) {
		Company company = this.companyDAO.getCompanyByObjID(Team.TABLE_NAME,
				Team.COLUMN_PRIMARY_KEY, team.getId());
		team.setCompany(company);
		if (this.authHelper.isActionAuthorized(team)) {
			this.teamDAO.update(team);
		}
	}

	@Override
	@Transactional
	public void delete(long id) {
		Team team = this.teamDAO.getByID(id);
		if (this.authHelper.isActionAuthorized(team)) {
			this.teamDAO.delete(id);
		}
	}

	@Override
	@Transactional
	public List<Team> list() {
		AuthenticationToken token = this.authHelper.getAuth();
		if (token.isSuperAdmin()) {
			return this.teamDAO.list(null);
		}
		return this.teamDAO.list(token.getCompany().getId());
	}

	@CacheEvict(value = Project.OBJECT_NAME + ":getByIDWithAllCollections", key = "#projectID")
	@Override
	@Transactional
	public void assignProjectTeam(long projectID, long teamID) {
		Project project = this.projectDAO.getByID(projectID);
		Team team = this.teamDAO.getByID(teamID);
		if (this.authHelper.isActionAuthorized(project)
				&& this.authHelper.isActionAuthorized(team)) {
			TeamAssignment assignment = new TeamAssignment();
			assignment.setProjectID(projectID);
			assignment.setTeamID(teamID);
			this.teamDAO.assignProjectTeam(assignment);
		}
	}

	@CacheEvict(value = Project.OBJECT_NAME + ":getByIDWithAllCollections", key = "#projectID")
	@Override
	@Transactional
	public void unassignProjectTeam(long projectID, long teamID) {
		Project project = this.projectDAO.getByID(projectID);
		Team team = this.teamDAO.getByID(teamID);
		if (this.authHelper.isActionAuthorized(project)
				&& this.authHelper.isActionAuthorized(team)) {
			this.teamDAO.unassignProjectTeam(projectID, teamID);
		}
	}

	@CacheEvict(value = Project.OBJECT_NAME + ":getByIDWithAllCollections", key = "#projectID")
	@Override
	@Transactional
	public void unassignAllProjectTeams(long projectID) {
		Project project = this.projectDAO.getByID(projectID);
		if (this.authHelper.isActionAuthorized(project)) {
			this.teamDAO.unassignAllProjectTeams(projectID);
		}
	}

	@Override
	@Transactional
	public Team getWithAllCollectionsByID(long id) {
		Team team = this.teamDAO.getWithAllCollectionsByID(id);
		if (this.authHelper.isActionAuthorized(team)) {
			return team;
		}
		return new Team();
	}

	@Override
	@Transactional
	public void unassignAllMembers(long teamID) {
		Team team = this.teamDAO.getByID(teamID);
		if (this.authHelper.isActionAuthorized(team)) {
			this.teamDAO.unassignAllMembers(teamID);
		}
	}

	@Override
	@Transactional
	public void unassignAllTeamsFromProject(long teamID) {
		Team team = this.teamDAO.getByID(teamID);
		if (this.authHelper.isActionAuthorized(team)) {
			this.teamDAO.unassignAllTeamsFromProject(teamID);
		}
	}

	@Override
	@Transactional
	public List<Team> list(Long companyID) {
		return this.teamDAO.list(companyID);
	}

	@Override
	@Transactional
	public List<Team> listUnassignedInProject(Long companyID, Project project) {
		if (this.authHelper.isActionAuthorized(project)) {
			List<Team> companyTeamList = this.teamDAO.list(companyID);
			List<TeamWrapper> wrappedTeamList = TeamWrapper
					.wrap(companyTeamList);
			List<TeamWrapper> wrappedAssignedList = TeamWrapper.wrap(project
					.getAssignedTeams());
			wrappedTeamList.removeAll(wrappedAssignedList);
			return TeamWrapper.unwrap(wrappedTeamList);
		}
		return new ArrayList<Team>();
	}

	@Override
	@Transactional
	public List<Team> listWithTasks() {
		AuthenticationToken token = this.authHelper.getAuth();
		if (token.isSuperAdmin()) {
			return this.teamDAO.listWithTasks(null);
		}
		return this.teamDAO.listWithTasks(token.getCompany().getId());
	}

	@Override
	@Transactional
	public String getNameByID(long teamID) {
		return this.teamDAO.getNameByID(teamID);
	}

}