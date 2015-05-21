package com.cebedo.pmsys.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cebedo.pmsys.bean.FieldAssignmentBean;
import com.cebedo.pmsys.bean.MultipartBean;
import com.cebedo.pmsys.bean.StaffAssignmentBean;
import com.cebedo.pmsys.bean.TaskGanttBean;
import com.cebedo.pmsys.bean.TeamAssignmentBean;
import com.cebedo.pmsys.constants.SystemConstants;
import com.cebedo.pmsys.helper.AuthHelper;
import com.cebedo.pmsys.model.Field;
import com.cebedo.pmsys.model.Milestone;
import com.cebedo.pmsys.model.Photo;
import com.cebedo.pmsys.model.Project;
import com.cebedo.pmsys.model.ProjectFile;
import com.cebedo.pmsys.model.SecurityRole;
import com.cebedo.pmsys.model.Staff;
import com.cebedo.pmsys.model.Task;
import com.cebedo.pmsys.model.Team;
import com.cebedo.pmsys.model.assignment.FieldAssignment;
import com.cebedo.pmsys.model.assignment.ManagerAssignment;
import com.cebedo.pmsys.service.FieldService;
import com.cebedo.pmsys.service.PayrollService;
import com.cebedo.pmsys.service.PhotoService;
import com.cebedo.pmsys.service.ProjectFileService;
import com.cebedo.pmsys.service.ProjectService;
import com.cebedo.pmsys.service.StaffService;
import com.cebedo.pmsys.service.TeamService;
import com.cebedo.pmsys.token.AuthenticationToken;
import com.cebedo.pmsys.ui.AlertBoxFactory;
import com.google.gson.Gson;

@Controller
@SessionAttributes(value = { Project.OBJECT_NAME, ProjectController.ATTR_FIELD,
	"old" + ProjectController.ATTR_FIELD,
	ProjectController.ATTR_PROJECT_FILE }, types = { Project.class,
	FieldAssignmentBean.class, ProjectFile.class })
@RequestMapping(Project.OBJECT_NAME)
public class ProjectController {

    public static final String ATTR_LIST = "projectList";
    public static final String ATTR_PROJECT = Project.OBJECT_NAME;
    public static final String ATTR_PAYROLL_MAP_TEAM = "teamPayrollMap";
    public static final String ATTR_PAYROLL_MAP_MANAGER = "managerPayrollMap";
    public static final String ATTR_FIELD = Field.OBJECT_NAME;
    public static final String ATTR_PHOTO = Photo.OBJECT_NAME;
    public static final String ATTR_STAFF = Staff.OBJECT_NAME;
    public static final String ATTR_TASK = Task.OBJECT_NAME;
    public static final String ATTR_PROJECT_FILE = ProjectFile.OBJECT_NAME;
    public static final String ATTR_STAFF_POSITION = "staffPosition";
    public static final String ATTR_TEAM_ASSIGNMENT = "teamAssignment";
    public static final String ATTR_GANTT_JSON = "ganttJSON";

    public static final String PARAM_FILE = "file";

    public static final String JSP_LIST = Project.OBJECT_NAME + "/projectList";
    public static final String JSP_EDIT = Project.OBJECT_NAME + "/projectEdit";
    public static final String JSP_EDIT_FIELD = "assignedFieldEdit";

    private AuthHelper authHelper = new AuthHelper();
    private ProjectService projectService;
    private StaffService staffService;
    private TeamService teamService;
    private FieldService fieldService;
    private PhotoService photoService;
    private ProjectFileService projectFileService;
    private PayrollService payrollService;

    @Autowired(required = true)
    @Qualifier(value = "payrollService")
    public void setPayrollService(PayrollService s) {
	this.payrollService = s;
    }

    @Autowired(required = true)
    @Qualifier(value = "projectFileService")
    public void setProjectFileService(ProjectFileService ps) {
	this.projectFileService = ps;
    }

    @Autowired(required = true)
    @Qualifier(value = "photoService")
    public void setPhotoService(PhotoService ps) {
	this.photoService = ps;
    }

    @Autowired(required = true)
    @Qualifier(value = "fieldService")
    public void setFieldService(FieldService s) {
	this.fieldService = s;
    }

    @Autowired(required = true)
    @Qualifier(value = "teamService")
    public void setTeamService(TeamService s) {
	this.teamService = s;
    }

    @Autowired(required = true)
    @Qualifier(value = "staffService")
    public void setStaffService(StaffService s) {
	this.staffService = s;
    }

    @Autowired(required = true)
    @Qualifier(value = "projectService")
    public void setProjectService(ProjectService s) {
	this.projectService = s;
    }

    @RequestMapping(value = { SystemConstants.REQUEST_ROOT,
	    SystemConstants.REQUEST_LIST }, method = RequestMethod.GET)
    public String listProjects(Model model) {
	model.addAttribute(ATTR_LIST, this.projectService.listWithTasks());
	model.addAttribute(SystemConstants.ATTR_ACTION,
		SystemConstants.ACTION_LIST);
	return JSP_LIST;
    }

    /**
     * Unassign all staff from a project.
     * 
     * @param projectID
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_UNASSIGN + "/"
	    + Staff.OBJECT_NAME + "/" + SystemConstants.ALL, method = RequestMethod.GET)
    public String unassignAllProjectManagers(HttpSession session,
	    SessionStatus status, RedirectAttributes redirectAttrs) {
	Project project = (Project) session.getAttribute(ATTR_PROJECT);

	// Error handling if staff was not set properly.
	if (project == null) {
	    AlertBoxFactory alertFactory = AlertBoxFactory.FAILED;
	    alertFactory
		    .setMessage("Error occured when you tried to <b>unassign all staff</b>. Please try again.");
	    redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		    alertFactory.generateHTML());
	    status.setComplete();
	    return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME
		    + "/" + SystemConstants.REQUEST_LIST;
	}

	long projectID = project.getId();
	this.staffService.unassignAllProjectManagers(projectID);

	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>unassigned all</b> managers.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Unassign a staff from a project.
     * 
     * @param projectID
     * @param staffID
     * @param position
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_UNASSIGN + "/"
	    + Staff.OBJECT_NAME + "/{" + Staff.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String unassignProjectManager(HttpSession session,
	    SessionStatus status,
	    @PathVariable(Staff.OBJECT_NAME) long staffID,
	    RedirectAttributes redirectAttrs) {

	Project project = (Project) session.getAttribute(ATTR_PROJECT);

	// Error handling if staff was not set properly.
	if (project == null) {
	    AlertBoxFactory alertFactory = AlertBoxFactory.FAILED;
	    alertFactory
		    .setMessage("Error occured when you tried to <b>unassign</b> a staff. Please try again.");
	    redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		    alertFactory.generateHTML());
	    status.setComplete();
	    return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME
		    + "/" + SystemConstants.REQUEST_LIST;
	}
	long projectID = project.getId();
	String staffName = this.staffService.getNameByID(staffID);
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>unassigned " + staffName
		+ "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	this.staffService.unassignProjectManager(projectID, staffID);
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Assign a staff to a project.
     * 
     * @param projectID
     * @param staffID
     * @param staffAssignment
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_ASSIGN + "/"
	    + Staff.OBJECT_NAME, method = RequestMethod.POST)
    public String assignProjectManager(
	    HttpSession session,
	    SessionStatus status,
	    @ModelAttribute(ATTR_STAFF_POSITION) StaffAssignmentBean staffAssignment,
	    RedirectAttributes redirectAttrs) {

	Project project = (Project) session.getAttribute(ATTR_PROJECT);

	// Error handling if staff was not set properly.
	if (project == null) {
	    AlertBoxFactory alertFactory = AlertBoxFactory.FAILED;
	    alertFactory
		    .setMessage("Error occured when you tried to <b>assign</b> a staff. Please try again.");
	    redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		    alertFactory.generateHTML());
	    status.setComplete();
	    return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME
		    + "/" + SystemConstants.REQUEST_LIST;
	}

	long staffID = staffAssignment.getStaffID();
	String position = staffAssignment.getPosition();

	// Fetch staff name, construct ui notifs.
	String staffName = this.staffService.getNameByID(staffID);
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>assigned " + staffName
		+ "</b> as <b>" + position + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Do service, clear session.
	// Then redirect.
	this.staffService.assignProjectManager(project.getId(), staffID,
		position);
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + project.getId();
    }

    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_CREATE, method = RequestMethod.POST)
    public String create(@ModelAttribute(ATTR_PROJECT) Project project,
	    RedirectAttributes redirectAttrs, SessionStatus status) {

	// Used for notification purposes.
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;

	// If request is to create a new project.
	if (project.getId() == 0) {
	    alertFactory.setMessage("Successfully <b>created</b> project <b>"
		    + project.getName() + "</b>.");
	    this.projectService.create(project);
	    redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		    alertFactory.generateHTML());
	    status.setComplete();
	    return SystemConstants.CONTROLLER_REDIRECT + ATTR_PROJECT + "/"
		    + SystemConstants.REQUEST_LIST;
	}

	// If request is to edit a project.
	alertFactory.setMessage("Successfully <b>updated</b> project <b>"
		+ project.getName() + "</b>.");
	this.projectService.update(project);
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + ATTR_PROJECT + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + project.getId();
    }

    /**
     * Update existing project fields.
     * 
     * @param session
     * @param fieldIdentifiers
     * @param status
     * @param model
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = Field.OBJECT_NAME + "/"
	    + SystemConstants.REQUEST_UPDATE, method = RequestMethod.POST)
    public String updateField(HttpSession session,
	    @ModelAttribute(ATTR_FIELD) FieldAssignmentBean newFaBean,
	    SessionStatus status, RedirectAttributes redirectAttrs) {

	// Old values.
	FieldAssignmentBean faBean = (FieldAssignmentBean) session
		.getAttribute("old" + ATTR_FIELD);

	// Do service.
	this.fieldService.updateAssignedProjectField(faBean.getProjectID(),
		faBean.getFieldID(), faBean.getLabel(), faBean.getValue(),
		newFaBean.getLabel(), newFaBean.getValue());

	// Create ui notifs.
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory
		.setMessage("Successfully <b>updated</b> extra information <b>"
			+ newFaBean.getLabel() + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Clear session and redirect.
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + faBean.getProjectID();
    }

    /**
     * Unassign team from a project.
     * 
     * @param projectID
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_UNASSIGN + "/"
	    + Team.OBJECT_NAME + "/{" + Team.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String unassignProjectTeam(HttpSession session,
	    SessionStatus status, @PathVariable(Team.OBJECT_NAME) long teamID,
	    RedirectAttributes redirectAttrs) {

	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();

	String teamName = this.teamService.getNameByID(teamID);
	this.teamService.unassignProjectTeam(projectID, teamID);

	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>unassigned</b> team <b>"
		+ teamName + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Assign team to a project.
     * 
     * @param projectID
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_ASSIGN + "/"
	    + Team.OBJECT_NAME, method = RequestMethod.POST)
    public String assignProjectTeam(
	    @ModelAttribute(ATTR_TEAM_ASSIGNMENT) TeamAssignmentBean taBean,
	    HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) {
	long teamID = taBean.getTeamID();
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	long projectID = proj.getId();

	String teamName = this.teamService.getNameByID(teamID);
	this.teamService.assignProjectTeam(projectID, teamID);

	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>assigned</b> team <b>"
		+ teamName + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Unassign all teams inside a project.
     * 
     * @param projectID
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_UNASSIGN + "/"
	    + Team.OBJECT_NAME + "/" + SystemConstants.ALL, method = RequestMethod.GET)
    public String unassignAllProjectTeams(HttpSession session,
	    SessionStatus status, RedirectAttributes redirectAttrs) {
	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();

	this.teamService.unassignAllProjectTeams(projectID);

	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>unassigned all</b> teams.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    @RequestMapping(value = Staff.OBJECT_NAME + "/"
	    + SystemConstants.REQUEST_EDIT + "/{" + Staff.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String editStaff(
	    @PathVariable(Staff.OBJECT_NAME) long staffID,
	    @RequestParam(value = SystemConstants.ORIGIN, required = false) String origin,
	    @RequestParam(value = SystemConstants.ORIGIN_ID, required = false) long originID,
	    Model model) {
	// Add origin details.
	model.addAttribute(SystemConstants.ORIGIN, origin);
	model.addAttribute(SystemConstants.ORIGIN_ID, originID);

	// If new, create it.
	if (staffID == 0) {
	    model.addAttribute(ATTR_STAFF, new Staff());
	    model.addAttribute(SystemConstants.ATTR_ACTION,
		    SystemConstants.ACTION_CREATE);
	    return JSP_EDIT;
	}

	// Else if not new, edit it.
	model.addAttribute(ATTR_STAFF,
		this.staffService.getWithAllCollectionsByID(staffID));
	model.addAttribute(SystemConstants.ATTR_ACTION,
		SystemConstants.ACTION_EDIT);
	return JSP_EDIT;
    }

    /**
     * Unassign a field from a project.
     * 
     * @param fieldID
     * @param projectID
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = Field.OBJECT_NAME + "/"
	    + SystemConstants.REQUEST_DELETE, method = RequestMethod.GET)
    public String deleteProjectField(HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	// Fetch bean from session.
	FieldAssignmentBean faBean = (FieldAssignmentBean) session
		.getAttribute(ATTR_FIELD);

	// Construct ui notifs.
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory
		.setMessage("Successfully <b>deleted</b> extra information <b>"
			+ faBean.getLabel() + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Do service.
	// Clear session attrs then redirect.
	this.fieldService.unassignProject(faBean.getFieldID(),
		faBean.getProjectID(), faBean.getLabel(), faBean.getValue());
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + faBean.getProjectID();
    }

    /**
     * Getter. Opening the edit page of a project field.
     * 
     * @param id
     * @param redirectAttrs
     * @param status
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = Field.OBJECT_NAME + "/"
	    + SystemConstants.REQUEST_EDIT + "/{" + Field.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String editField(HttpSession session,
	    @PathVariable(Field.OBJECT_NAME) String fieldIdentifiers,
	    Model model) {

	// Get project id.
	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();
	long fieldID = Long.valueOf(fieldIdentifiers.split("-")[0]);
	String label = fieldIdentifiers.split("-")[1];
	String value = fieldIdentifiers.split("-")[2];

	// Set to model attribute "field".
	model.addAttribute(ATTR_FIELD, new FieldAssignmentBean(projectID,
		fieldID, label, value));
	session.setAttribute("old" + ATTR_FIELD, new FieldAssignmentBean(
		projectID, fieldID, label, value));

	return Field.OBJECT_NAME + "/" + JSP_EDIT_FIELD;
    }

    /**
     * Delete a project.
     * 
     * @param id
     * @param redirectAttrs
     * @param status
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_DELETE + "/{"
	    + Project.COLUMN_PRIMARY_KEY + "}", method = RequestMethod.GET)
    public String delete(@PathVariable(Project.COLUMN_PRIMARY_KEY) int id,
	    RedirectAttributes redirectAttrs, SessionStatus status) {
	// Alert result.
	String projectName = this.projectService.getNameByID(id);
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>deleted</b> project <b>"
		+ projectName + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Reset search entries in cache.
	AuthenticationToken auth = this.authHelper.getAuth();
	Project project = this.projectService.getByID(id);
	Long companyID = null;
	if (auth.getCompany() == null) {
	    if (project.getCompany() != null) {
		companyID = project.getCompany().getId();
	    }
	} else {
	    companyID = auth.getCompany().getId();
	}
	this.projectService.clearSearchCache(companyID);

	// TODO Cleanup also the SYS_HOME.
	this.projectService.delete(id);
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + ATTR_PROJECT + "/"
		+ SystemConstants.REQUEST_LIST;
    }

    /**
     * Delete a project's profile picture.
     * 
     * @param projectID
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.PROFILE + "/"
	    + SystemConstants.REQUEST_DELETE, method = RequestMethod.GET)
    public ModelAndView deleteProjectProfile(HttpSession session,
	    SessionStatus status, RedirectAttributes redirectAttrs)
	    throws IOException {
	// Get project id.
	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();

	// Do service.
	// Construct ui notification.
	this.photoService.deleteProjectProfile(projectID);
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory
		.setMessage("Successfully <b>deleted</b> the <b>profile picture</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Clear session var.
	// Then return.
	status.setComplete();
	return new ModelAndView(SystemConstants.CONTROLLER_REDIRECT
		+ Project.OBJECT_NAME + "/" + SystemConstants.REQUEST_EDIT
		+ "/" + projectID);
    }

    /**
     * Upload a project profile pic.
     * 
     * @param mBean
     * @return
     * @throws IOException
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_UPLOAD + "/"
	    + SystemConstants.PROFILE, method = RequestMethod.POST)
    public String uploadProfile(HttpSession session,
	    @RequestParam(PARAM_FILE) MultipartFile file, SessionStatus status)
	    throws IOException {
	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();

	// If file is not empty.
	if (!file.isEmpty()) {
	    this.photoService.uploadProjectProfile(file, projectID);
	} else {
	    // TODO Handle this scenario.
	}
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Unassign all fields of a project.
     * 
     * @param fieldID
     * @param projectID
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_UNASSIGN + "/"
	    + Field.OBJECT_NAME + "/" + SystemConstants.ALL, method = RequestMethod.GET)
    public String unassignAllFields(HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	// Get project ID.
	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();

	// Construct notification.
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory
		.setMessage("Successfully <b>removed all</b> extra information.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Do service and clear session vars.
	// Then return.
	this.fieldService.unassignAllProjects(projectID);
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Assign a field to a project.
     * 
     * @param fieldAssignment
     * @param fieldID
     * @param projectID
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_ASSIGN + "/"
	    + Field.OBJECT_NAME, method = RequestMethod.POST)
    public String assignField(HttpSession session,
	    @ModelAttribute(ATTR_FIELD) FieldAssignmentBean faBean,
	    RedirectAttributes redirectAttrs, SessionStatus status) {

	// Get project from session.
	// Construct commit object.
	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	long fieldID = 1;
	FieldAssignment fieldAssignment = new FieldAssignment();
	fieldAssignment.setLabel(faBean.getLabel());
	fieldAssignment.setField(new Field(faBean.getFieldID()));
	fieldAssignment.setValue(faBean.getValue());
	fieldAssignment.setProject(proj);

	// Construct ui notifications.
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory
		.setMessage("Successfully <b>added</b> extra information <b>"
			+ fieldAssignment.getLabel() + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Do service.
	this.fieldService.assignProject(fieldAssignment, fieldID, proj.getId());

	// Remove session variables.
	// Evict project cache.
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + proj.getId();
    }

    /**
     * Upload a file to a project.
     * 
     * @param file
     * @param projectID
     * @param description
     * @param redirectAttrs
     * @return
     * @throws IOException
     */
    @RequestMapping(value = SystemConstants.REQUEST_UPLOAD + "/"
	    + ProjectFile.OBJECT_NAME, method = RequestMethod.POST)
    public String uploadFileToProject(
	    @ModelAttribute(ATTR_PROJECT_FILE) MultipartBean mpBean,
	    SessionStatus status, HttpSession session,
	    RedirectAttributes redirectAttrs) throws IOException {

	MultipartFile file = mpBean.getFile();
	String description = mpBean.getDescription();
	long projectID = mpBean.getProjectID();

	AlertBoxFactory alertFactory = new AlertBoxFactory();
	// If file is not empty.
	if (!file.isEmpty()) {
	    // Upload then evict cache.
	    this.projectFileService.create(file, projectID, description);
	    alertFactory.setStatus(SystemConstants.UI_STATUS_SUCCESS);
	    alertFactory.setMessage("Successfully <b>uploaded</b> file <b>"
		    + file.getOriginalFilename() + "</b>.");
	} else {
	    alertFactory.setStatus(SystemConstants.UI_STATUS_DANGER);
	    alertFactory.setMessage("Failed to <b>upload</b> empty file <b>"
		    + file.getOriginalFilename() + "</b>.");
	}
	status.setComplete();
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Download a file from Project. TODO Need work on security url here.
     * 
     * @param projectID
     * @param fileID
     * @return
     */
    @RequestMapping(value = SystemConstants.REQUEST_DOWNLOAD + "/"
	    + ProjectFile.OBJECT_NAME + "/{" + ProjectFile.OBJECT_NAME + "}", method = RequestMethod.GET)
    public void downloadFileFromProject(
	    @PathVariable(ProjectFile.OBJECT_NAME) long fileID,
	    HttpServletResponse response) {

	File actualFile = this.projectFileService.getPhysicalFileByID(fileID);
	// AlertBoxFactory alertFactory = new AlertBoxFactory();
	try {
	    // alertFactory.setStatus(SystemConstants.UI_STATUS_INFO);
	    // alertFactory.setMessage("<b>Downloading</b> file <b>"
	    // + actualFile.getName() + "</b>.");

	    FileInputStream iStream = new FileInputStream(actualFile);
	    response.setContentType("application/octet-stream");
	    response.setContentLength((int) actualFile.length());
	    response.setHeader("Content-Disposition", "attachment; filename=\""
		    + actualFile.getName() + "\"");
	    IOUtils.copy(iStream, response.getOutputStream());
	    response.flushBuffer();
	} catch (Exception e) {
	    // alertFactory.setStatus(SystemConstants.UI_STATUS_DANGER);
	    // alertFactory.setMessage("Failed to <b>download</b> file <b>"
	    // + actualFile.getName() + "</b>.");
	    e.printStackTrace();
	}

	// redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
	// alertFactory.generateHTML());
	//
	// return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME +
	// "/"
	// + SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Upload a photo to a project.
     * 
     * @param file
     * @param projectID
     * @param description
     * @param redirectAttrs
     * @return
     * @throws IOException
     */
    @RequestMapping(value = SystemConstants.REQUEST_UPLOAD + "/"
	    + Photo.OBJECT_NAME, method = RequestMethod.POST)
    public String uploadPhotoToProject(
	    @ModelAttribute(ATTR_PHOTO) MultipartBean mpBean,
	    HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) throws IOException {

	Project proj = (Project) session
		.getAttribute(ProjectController.ATTR_PROJECT);
	MultipartFile file = mpBean.getFile();
	String description = mpBean.getDescription();
	long projectID = proj.getId();

	AlertBoxFactory alertFactory = new AlertBoxFactory();

	// TODO Limit only uploads to known extensions of an image.
	// If file is not empty.
	if (!file.isEmpty()) {
	    alertFactory = this.photoService.create(file, projectID,
		    description);
	} else {
	    alertFactory = AlertBoxFactory.FAILED;
	    alertFactory
		    .setMessage("You cannot <b>upload</b> an <b>empty</b> photo.");
	}

	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + projectID;
    }

    /**
     * Delete a photo from a project.
     * 
     * @param projectID
     * @param id
     * @param redirectAttrs
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_DELETE + "/"
	    + Photo.OBJECT_NAME + "/{" + Photo.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String deletePhoto(@PathVariable(Photo.OBJECT_NAME) long id,
	    HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	Project proj = (Project) session.getAttribute(ATTR_PROJECT);

	String name = this.photoService.getNameByID(id);
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>deleted</b> photo <b>" + name
		+ "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());
	this.photoService.delete(id);
	this.projectService.clearProjectCache(proj.getId());

	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + proj.getId();
    }

    /**
     * Delete a project file. TODO Make this general, create "From Origin"
     * version.
     * 
     * @param id
     * @param projectID
     * @param redirectAttrs
     * @return
     */
    @PreAuthorize("hasRole('" + SecurityRole.ROLE_PROJECT_EDITOR + "')")
    @RequestMapping(value = SystemConstants.REQUEST_DELETE + "/"
	    + ProjectFile.OBJECT_NAME + "/{" + ProjectFile.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String deleteProjectfile(
	    @PathVariable(ProjectFile.OBJECT_NAME) int id, HttpSession session,
	    SessionStatus status, RedirectAttributes redirectAttrs) {

	Project proj = (Project) session.getAttribute(ATTR_PROJECT);

	String fileName = this.projectFileService.getNameByID(id);
	AlertBoxFactory alertFactory = AlertBoxFactory.SUCCESS;
	alertFactory.setMessage("Successfully <b>deleted</b> file <b>"
		+ fileName + "</b>.");
	redirectAttrs.addFlashAttribute(SystemConstants.UI_PARAM_ALERT,
		alertFactory.generateHTML());

	// Delete, then
	// Evict cache.
	this.projectFileService.delete(id);
	this.projectService.clearProjectCache(proj.getId());
	status.setComplete();
	return SystemConstants.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ SystemConstants.REQUEST_EDIT + "/" + proj.getId();
    }

    /**
     * Open an existing/new project page.
     * 
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = SystemConstants.REQUEST_EDIT + "/{"
	    + Project.COLUMN_PRIMARY_KEY + "}")
    public String editProject(@PathVariable(Project.COLUMN_PRIMARY_KEY) int id,
	    Model model) {

	// Model for forms.
	model.addAttribute(ATTR_FIELD, new FieldAssignmentBean(id, 1));
	model.addAttribute(ATTR_PROJECT_FILE, new MultipartBean(id));
	model.addAttribute(ATTR_PHOTO, new MultipartBean(id));

	// If ID is zero, create new.
	if (id == 0) {
	    model.addAttribute(ATTR_PROJECT, new Project());
	    model.addAttribute(SystemConstants.ATTR_ACTION,
		    SystemConstants.ACTION_CREATE);
	    return JSP_EDIT;
	}

	Project proj = this.projectService.getByIDWithAllCollections(id);

	// TODO Speed up performance!
	// FIXME Since beginning of time 'til now.
	Date min = new Date(0);
	Date max = new Date(System.currentTimeMillis());

	// Payroll maps.
	Map<Team, Map<Staff, String>> teamPayrollMap = new HashMap<Team, Map<Staff, String>>();
	Map<ManagerAssignment, String> managerPayrollMap = new HashMap<ManagerAssignment, String>();

	// Wage for teams.
	Map<Long, String> computedMap = new HashMap<Long, String>();

	// Loop through all the teams.
	for (Team team : proj.getAssignedTeams()) {
	    Map<Staff, String> staffPayrollMap = new HashMap<Staff, String>();

	    for (Staff member : team.getMembers()) {
		long memberID = member.getId();

		// If a staff has already been computed before,
		// don't compute again.
		if (computedMap.containsKey(memberID)) {
		    staffPayrollMap.put(member,
			    "Check " + computedMap.get(memberID));
		    continue;
		}

		// Add to map
		// and add to computed list.
		staffPayrollMap.put(member, String.valueOf(this.payrollService
			.getTotalWageOfStaffInRange(member, min, max)));
		computedMap.put(memberID, "Team " + team.getName());
	    }

	    // Add to team list.
	    teamPayrollMap.put(team, staffPayrollMap);
	}

	// Wage for managers.
	for (ManagerAssignment assignment : proj.getManagerAssignments()) {
	    Staff manager = assignment.getManager();
	    long managerID = manager.getId();
	    // If a staff has already been computed before,
	    // don't compute again.
	    if (computedMap.containsKey(managerID)) {
		managerPayrollMap.put(assignment,
			"Check " + computedMap.get(managerID));
		continue;
	    }

	    // Get wage then add to map.
	    managerPayrollMap.put(assignment, String
		    .valueOf(this.payrollService.getTotalWageOfStaffInRange(
			    manager, min, max)));
	    computedMap.put(managerID, assignment.getProjectPosition());
	}

	// Construct JSON data for the gantt chart.
	List<TaskGanttBean> ganttBeanList = new ArrayList<TaskGanttBean>();

	// Add myself.
	TaskGanttBean myGanttBean = new TaskGanttBean(proj);
	ganttBeanList.add(myGanttBean);

	// Add all milestones and included tasks.
	for (Milestone milestone : proj.getMilestones()) {
	    TaskGanttBean milestoneBean = new TaskGanttBean(milestone,
		    myGanttBean);
	    ganttBeanList.add(milestoneBean);

	    // Actual adding of tasks under this milestone.
	    for (Task taskInMilestone : milestone.getTasks()) {
		TaskGanttBean ganttBean = new TaskGanttBean(taskInMilestone,
			milestoneBean);
		ganttBeanList.add(ganttBean);
	    }
	}

	// Get the gantt parent data.
	// All tasks without a milestone.
	for (Task task : proj.getAssignedTasks()) {

	    // Add only tasks without a milestone.
	    if (task.getMilestone() == null) {
		TaskGanttBean ganttBean = new TaskGanttBean(task, myGanttBean);
		ganttBeanList.add(ganttBean);
	    }
	}

	// Get list of fields.
	// Get list of staff members for manager assignments.
	Long companyID = this.authHelper.getAuth().isSuperAdmin() ? null : proj
		.getCompany().getId();
	List<Field> fieldList = this.fieldService.list();
	List<Staff> staffList = this.staffService.listUnassignedInProject(
		companyID, proj);

	// Get list of teams.
	List<Team> teamList = this.teamService.listUnassignedInProject(
		companyID, proj);
	if (teamList.size() > 0) {
	    Team team = teamList.get(0);
	    long sampleID = team.getId();
	    TeamAssignmentBean taBean = new TeamAssignmentBean(sampleID);
	    model.addAttribute(ATTR_TEAM_ASSIGNMENT, taBean);
	}

	model.addAttribute(ATTR_GANTT_JSON,
		new Gson().toJson(ganttBeanList, ArrayList.class));
	model.addAttribute(FieldController.ATTR_LIST, fieldList);
	model.addAttribute(TeamController.ATTR_LIST, teamList);
	model.addAttribute(StaffController.ATTR_LIST, staffList);
	model.addAttribute(ATTR_STAFF_POSITION, new StaffAssignmentBean());
	model.addAttribute(ATTR_PROJECT, proj);
	model.addAttribute(ATTR_PAYROLL_MAP_TEAM, teamPayrollMap);
	model.addAttribute(ATTR_PAYROLL_MAP_MANAGER, managerPayrollMap);
	model.addAttribute(SystemConstants.ATTR_ACTION,
		SystemConstants.ACTION_EDIT);

	return JSP_EDIT;
    }
}