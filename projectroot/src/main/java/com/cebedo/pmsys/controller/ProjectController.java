package com.cebedo.pmsys.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cebedo.pmsys.bean.EstimateComputationInputBean;
import com.cebedo.pmsys.constants.ConstantsRedis;
import com.cebedo.pmsys.constants.ConstantsSystem;
import com.cebedo.pmsys.constants.RegistryJSPPath;
import com.cebedo.pmsys.constants.RegistryResponseMessage;
import com.cebedo.pmsys.constants.RegistryURL;
import com.cebedo.pmsys.domain.Attendance;
import com.cebedo.pmsys.domain.Delivery;
import com.cebedo.pmsys.domain.EstimationOutput;
import com.cebedo.pmsys.domain.Material;
import com.cebedo.pmsys.domain.ProjectAux;
import com.cebedo.pmsys.domain.ProjectPayroll;
import com.cebedo.pmsys.domain.PullOut;
import com.cebedo.pmsys.enums.AttendanceStatus;
import com.cebedo.pmsys.enums.CalendarEventType;
import com.cebedo.pmsys.enums.CommonLengthUnit;
import com.cebedo.pmsys.enums.CommonMassUnit;
import com.cebedo.pmsys.enums.CommonVolumeUnit;
import com.cebedo.pmsys.enums.GanttElement;
import com.cebedo.pmsys.enums.MaterialCategory;
import com.cebedo.pmsys.enums.PayrollStatus;
import com.cebedo.pmsys.enums.ProjectStatus;
import com.cebedo.pmsys.enums.TableEstimationAllowance;
import com.cebedo.pmsys.enums.TaskStatus;
import com.cebedo.pmsys.helper.AuthHelper;
import com.cebedo.pmsys.model.Company;
import com.cebedo.pmsys.model.Field;
import com.cebedo.pmsys.model.Project;
import com.cebedo.pmsys.model.Staff;
import com.cebedo.pmsys.model.SystemUser;
import com.cebedo.pmsys.model.Task;
import com.cebedo.pmsys.model.assignment.FieldAssignment;
import com.cebedo.pmsys.pojo.FormDateRange;
import com.cebedo.pmsys.pojo.FormFieldAssignment;
import com.cebedo.pmsys.pojo.FormMassAttendance;
import com.cebedo.pmsys.pojo.FormMassUpload;
import com.cebedo.pmsys.pojo.FormPayrollIncludeStaff;
import com.cebedo.pmsys.pojo.FormStaffAssignment;
import com.cebedo.pmsys.service.AttendanceService;
import com.cebedo.pmsys.service.DeliveryService;
import com.cebedo.pmsys.service.EstimateService;
import com.cebedo.pmsys.service.EstimationOutputService;
import com.cebedo.pmsys.service.FieldService;
import com.cebedo.pmsys.service.MaterialService;
import com.cebedo.pmsys.service.ProjectAuxService;
import com.cebedo.pmsys.service.ProjectPayrollService;
import com.cebedo.pmsys.service.ProjectService;
import com.cebedo.pmsys.service.PullOutService;
import com.cebedo.pmsys.service.StaffService;
import com.cebedo.pmsys.service.TaskService;
import com.cebedo.pmsys.service.impl.ProjectPayrollServiceImpl;
import com.cebedo.pmsys.token.AuthenticationToken;
import com.cebedo.pmsys.ui.AlertBoxGenerator;
import com.cebedo.pmsys.utils.DateUtils;

@Controller
@SessionAttributes(

value = {
	// Project.
	Project.OBJECT_NAME, ProjectController.ATTR_FIELD, "old" + ProjectController.ATTR_FIELD,
	ProjectController.ATTR_MASS_UPLOAD_STAFF_BEAN,
	ProjectController.ATTR_TASK,
	ProjectController.ATTR_FROM_PROJECT,
	ProjectController.ATTR_PROJECT_PAYROLL,

	// Redis.
	ConstantsRedis.OBJECT_PAYROLL, ConstantsRedis.OBJECT_DELIVERY, ConstantsRedis.OBJECT_MATERIAL,
	ConstantsRedis.OBJECT_PULL_OUT,
	ConstantsRedis.OBJECT_ESTIMATE,

	// Staff.
	ProjectController.ATTR_STAFF, ProjectController.ATTR_ATTENDANCE_MASS,
	ProjectController.ATTR_CALENDAR_MIN_DATE, ProjectController.ATTR_CALENDAR_MAX_DATE,
	ProjectController.ATTR_ATTENDANCE,

	// Task.
	ProjectController.ATTR_TASK }

)
@RequestMapping(Project.OBJECT_NAME)
public class ProjectController {

    // TODO Clean this whole thing.
    public static final String ATTR_LIST = "projectList";
    public static final String ATTR_PROJECT = Project.OBJECT_NAME;
    public static final String ATTR_PROJECT_AUX = ConstantsRedis.OBJECT_PROJECT_AUX;
    public static final String ATTR_DELIVERY = ConstantsRedis.OBJECT_DELIVERY;
    public static final String ATTR_MATERIAL = ConstantsRedis.OBJECT_MATERIAL;
    public static final String ATTR_PULL_OUT = ConstantsRedis.OBJECT_PULL_OUT;
    public static final String ATTR_FIELD = Field.OBJECT_NAME;
    public static final String ATTR_STAFF = Staff.OBJECT_NAME;
    public static final String ATTR_TASK = Task.OBJECT_NAME;
    public static final String ATTR_ALL_STAFF = "allStaff";
    public static final String ATTR_PROJECT_PAYROLL = "projectPayroll";
    public static final String ATTR_MATERIAL_LIST = "materialList";
    public static final String ATTR_PROJECT_STATUS_LIST = "projectStatusList";
    public static final String ATTR_TASK_STATUS_LIST = "taskStatusList";
    public static final String ATTR_PULL_OUT_LIST = "pullOutList";
    public static final String ATTR_CONCRETE_ESTIMATION_SUMMARIES = "concreteEstimationSummaries";
    public static final String ATTR_MASONRY_CHB_ESTIMATION_SUMMARIES = "masonryCHBEstimationSummaries";
    public static final String ATTR_SHAPE_LIST = "shapeList";

    public static final String ATTR_MASS_UPLOAD_STAFF_BEAN = "massUploadStaffBean";
    public static final String ATTR_ESTIMATE = ConstantsRedis.OBJECT_ESTIMATE;
    public static final String ATTR_ESTIMATE_INPUT = "estimationInput";
    public static final String ATTR_ESTIMATE_OUTPUT_LIST = "estimationOutputList";
    public static final String ATTR_ESTIMATE_OUTPUT_JSON = "estimateJSON";
    public static final String ATTR_ESTIMATE_ALLOWANCE_LIST = "allowanceList";
    public static final String ATTR_ESTIMATE_MASONRY_LIST = "masonryEstimateList";
    public static final String ATTR_ESTIMATE_TYPES = "estimateTypes";
    public static final String ATTR_ESTIMATE_CONCRETE_LIST = "concreteEstimateList";
    public static final String ATTR_ESTIMATE_COMBINED_LIST = "combinedEstimateList";
    public static final String ATTR_CONCRETE_ESTIMATION_SUMMARY = ConstantsRedis.OBJECT_CONCRETE_ESTIMATION_SUMMARY;
    public static final String ATTR_CONCRETE_PROPORTION_LIST = "concreteProportionList";
    public static final String ATTR_CHB_LIST = "chbList";
    public static final String ATTR_BLOCK_LAYING_MIXTURE_LIST = "blockLayingMixtureList";
    public static final String ATTR_CHB_FOOTING_DIMENSION_LIST = "chbFootingDimensionList";

    public static final String ATTR_DELIVERY_LIST = "deliveryList";
    public static final String ATTR_PAYROLL_LIST = "payrollList";
    public static final String ATTR_COMMON_UNITS_LIST = "commonUnitsList";
    public static final String ATTR_CHB_MR_HORIZONTAL_LIST = "chbHorizontalReinforcementList";
    public static final String ATTR_CHB_MR_VERTICAL_LIST = "chbVerticalReinforcementList";
    public static final String ATTR_UNIT_LIST_LENGTH = "unitListLength";
    public static final String ATTR_UNIT_LIST_MASS = "unitListMass";
    public static final String ATTR_UNIT_LIST_VOLUME = "unitListVolume";
    public static final String ATTR_MATERIAL_CATEGORY_LIST = "materialCategoryList";
    public static final String ATTR_PAYROLL_LIST_TOTAL = "payrollListTotal";
    public static final String ATTR_STAFF_POSITION = "staffPosition";
    public static final String ATTR_FILE = "file";

    public static final String ATTR_PAYROLL_SELECTOR_STATUS = "payrollStatusArr";

    public static final String ATTR_CALENDAR_EVENT_TYPES_MAP = "calendarEventTypesMap";
    public static final String ATTR_CALENDAR_EVENT_TYPES_LIST = "calendarEventTypes";
    public static final String ATTR_CALENDAR_JSON = "calendarJSON";
    public static final String ATTR_GANTT_JSON = "ganttJSON";
    public static final String ATTR_GANTT_TYPE_LIST = "ganttElemTypeList";

    public static final String ATTR_TIMELINE_TASK_STATUS_MAP = "taskStatusMap";

    public static final String ATTR_PAYROLL_JSON = "payrollJSON";
    public static final String ATTR_PAYROLL_CHECKBOX_STAFF = "staffList";
    public static final String ATTR_STAFF_LIST = "staffList";
    public static final String ATTR_STAFF_LIST_AVAILABLE = "availableStaffToAssign";
    public static final String ATTR_PAYROLL_MANUAL_STAFF_LIST = "manualStaffList";
    public static final String ATTR_PAYROLL_INCLUDE_STAFF = "payrollIncludeStaff";

    public static final String KEY_PROJECT_STRUCTURE_MANAGERS = "Managers";

    // Staff constants backup.
    public static final String ATTR_PAYROLL_TOTAL_WAGE = "payrollTotalWage";
    public static final String ATTR_TASK_STATUS_MAP = "taskStatusMap";

    public static final String ATTR_CALENDAR_STATUS_LIST = "calendarStatusList";
    public static final String ATTR_CALENDAR_MAX_DATE_STR = "maxDateStr";
    public static final String ATTR_CALENDAR_MIN_DATE = "minDate";
    public static final String ATTR_CALENDAR_MAX_DATE = "maxDate";
    public static final String ATTR_CALENDAR_RANGE_DATES = "rangeDate";

    public static final String ATTR_ATTENDANCE = ConstantsRedis.OBJECT_ATTENDANCE;
    public static final String ATTR_ATTENDANCE_LIST = "attendanceList";
    public static final String ATTR_ATTENDANCE_STATUS_MAP = "attendanceStatusMap";
    public static final String ATTR_ATTENDANCE_MASS = "massAttendance";

    public static final String ATTR_FROM_PROJECT = "fromProject";

    // Task attributes.
    public static final String ATTR_STAFF_ASSIGNMENT = "staffAssignment";

    private AuthHelper authHelper = new AuthHelper();

    private ProjectService projectService;
    private StaffService staffService;
    private FieldService fieldService;
    private DeliveryService deliveryService;
    private MaterialService materialService;
    private ProjectPayrollService projectPayrollService;
    private ProjectAuxService projectAuxService;
    private PullOutService pullOutService;
    private EstimateService estimateService;
    private EstimationOutputService estimationOutputService;
    private TaskService taskService;
    private AttendanceService attendanceService;

    @Autowired(required = true)
    @Qualifier(value = "attendanceService")
    public void setAttendanceService(AttendanceService s) {
	this.attendanceService = s;
    }

    @Autowired(required = true)
    @Qualifier(value = "taskService")
    public void setTaskService(TaskService taskService) {
	this.taskService = taskService;
    }

    @Autowired(required = true)
    @Qualifier(value = "estimationOutputService")
    public void setEstimationOutputService(EstimationOutputService estimationOutputService) {
	this.estimationOutputService = estimationOutputService;
    }

    @Autowired(required = true)
    @Qualifier(value = "estimateService")
    public void setEstimateService(EstimateService estimateService) {
	this.estimateService = estimateService;
    }

    @Autowired(required = true)
    @Qualifier(value = "pullOutService")
    public void setPullOutService(PullOutService pullOutService) {
	this.pullOutService = pullOutService;
    }

    @Autowired(required = true)
    @Qualifier(value = "projectAuxService")
    public void setProjectAuxService(ProjectAuxService projectAuxService) {
	this.projectAuxService = projectAuxService;
    }

    @Autowired(required = true)
    @Qualifier(value = "projectPayrollService")
    public void setProjectPayrollService(ProjectPayrollService projectPayrollService) {
	this.projectPayrollService = projectPayrollService;
    }

    @Autowired(required = true)
    @Qualifier(value = "materialService")
    public void setMaterialService(MaterialService materialService) {
	this.materialService = materialService;
    }

    @Autowired(required = true)
    @Qualifier(value = "deliveryService")
    public void setDeliveryService(DeliveryService deliveryService) {
	this.deliveryService = deliveryService;
    }

    @Autowired(required = true)
    @Qualifier(value = "fieldService")
    public void setFieldService(FieldService s) {
	this.fieldService = s;
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

    /**
     * List projects.
     * 
     * @param model
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_ROOT, ConstantsSystem.REQUEST_LIST }, method = RequestMethod.GET)
    public String listProjects(Model model, HttpSession session) {
	model.addAttribute(ATTR_LIST, this.projectService.list());
	session.removeAttribute(ProjectController.ATTR_FROM_PROJECT);
	return RegistryJSPPath.JSP_LIST_PROJECT;
    }

    /**
     * Unassign all staff from a project.
     * 
     * @param projectID
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_UNASSIGN + "/" + Staff.OBJECT_NAME + "-member" + "/"
	    + ConstantsSystem.ALL, method = RequestMethod.GET)
    public String unassignAllStaffMembers(HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	Project project = (Project) session.getAttribute(ATTR_PROJECT);

	// Get response.
	String response = this.staffService.unassignAllStaffMembers(project);

	// Attach response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageProject(project.getId(), status);
    }

    /**
     * Complete the session and return back to the project edit page.
     * 
     * @param projectID
     * @param status
     * @return
     */
    private String redirectEditPageProject(long projectID, SessionStatus status) {
	if (status != null) {
	    status.setComplete();
	}
	return String.format(RegistryURL.REDIRECT_EDIT_PROJECT, projectID);
    }

    /**
     * Unassign a staff from a project.
     * 
     * @param projectID
     * @param staffID
     * @param position
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_UNASSIGN + "/" + Staff.OBJECT_NAME + "-member"
	    + "/{" + Staff.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String unassignStaffMember(HttpSession session, SessionStatus status,
	    @PathVariable(Staff.OBJECT_NAME) long staffID, RedirectAttributes redirectAttrs) {

	Project project = (Project) session.getAttribute(ATTR_PROJECT);

	// Get response.
	String response = this.staffService.unassignStaffMember(project, staffID);

	// Attach response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageProject(project.getId(), status);
    }

    /**
     * Assign a staff to a project.
     * 
     * @param projectID
     * @param staffID
     * @param staffAssignment
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_ASSIGN + "/" + Staff.OBJECT_NAME + "/"
	    + ConstantsSystem.MASS, method = RequestMethod.POST)
    public String assignMassStaff(HttpSession session, SessionStatus status,
	    @ModelAttribute(ATTR_PROJECT) Project project, RedirectAttributes redirectAttrs) {

	// Get response.
	// Do service, clear session.
	// Then redirect.
	String response = this.staffService.assignStaffMass(project);

	// Attach response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageProject(project.getId(), status);
    }

    /**
     * Create a new project.
     * 
     * @param project
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_CREATE, method = RequestMethod.POST)
    public String create(@ModelAttribute(ATTR_PROJECT) Project project,
	    RedirectAttributes redirectAttrs, SessionStatus status) {

	// If request is to create a new project.
	if (project.getId() == 0) {

	    // Get response.
	    String response = this.projectService.create(project);

	    // Attach response.
	    redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	    return redirectEditPageProject(project.getId(), status);
	}

	// Get response.
	// If request is to edit a project.
	String response = this.projectService.update(project);

	// Attach response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageProject(project.getId(), status);
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
    @RequestMapping(value = Field.OBJECT_NAME + "/" + ConstantsSystem.REQUEST_UPDATE, method = RequestMethod.POST)
    public String updateField(HttpSession session,
	    @ModelAttribute(ATTR_FIELD) FormFieldAssignment newFaBean, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	// Old values.
	FormFieldAssignment faBean = (FormFieldAssignment) session.getAttribute("old" + ATTR_FIELD);

	// Get response.
	// Do service.
	String response = this.fieldService.updateAssignedProjectField(faBean.getProjectID(),
		faBean.getFieldID(), faBean.getLabel(), faBean.getValue(), newFaBean.getLabel(),
		newFaBean.getValue());

	// Attach response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Clear session and redirect.
	return redirectEditPageProject(faBean.getProjectID(), status);
    }

    /**
     * Unassign a field from a project.<br>
     * TODO Make this accessible also from ProjectEdit.jsp<br>
     * You should be able to delete directly, not access the
     * assignedFieldEdit.jsp first before you can delete.
     * 
     * @param fieldID
     * @param projectID
     * @return
     */
    @RequestMapping(value = Field.OBJECT_NAME + "/" + ConstantsSystem.REQUEST_DELETE, method = RequestMethod.GET)
    public String deleteProjectField(HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	// Fetch bean from session.
	FormFieldAssignment faBean = (FormFieldAssignment) session.getAttribute(ATTR_FIELD);

	// Do service.
	// Clear session attrs then redirect.
	// Get response.
	String response = this.fieldService.unassignFieldFromProject(faBean.getFieldID(),
		faBean.getProjectID(), faBean.getLabel(), faBean.getValue());

	// Attach response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageProject(faBean.getProjectID(), status);
    }

    /**
     * Getter. Opening the edit page of a project field.
     * 
     * @param id
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = Field.OBJECT_NAME + "/" + ConstantsSystem.REQUEST_EDIT + "/{"
	    + Field.OBJECT_NAME + "}", method = RequestMethod.GET)
    public String editField(HttpSession session,
	    @PathVariable(Field.OBJECT_NAME) String fieldIdentifiers, Model model) {

	// Get project id.
	Project proj = (Project) session.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();
	long fieldID = Long.valueOf(fieldIdentifiers.split(Field.IDENTIFIER_SEPARATOR)[0]);
	String label = fieldIdentifiers.split(Field.IDENTIFIER_SEPARATOR)[1];
	String value = fieldIdentifiers.split(Field.IDENTIFIER_SEPARATOR)[2];

	// Set to model attribute "field".
	model.addAttribute(ATTR_PROJECT, proj);
	model.addAttribute(ATTR_FIELD, new FormFieldAssignment(projectID, fieldID, label, value));
	session.setAttribute("old" + ATTR_FIELD, new FormFieldAssignment(projectID, fieldID, label,
		value));

	return RegistryJSPPath.JSP_EDIT_PROJECT_FIELD;
    }

    /**
     * Delete a project.
     * 
     * @param id
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_DELETE + "/{" + Project.COLUMN_PRIMARY_KEY + "}", method = RequestMethod.GET)
    public String delete(@PathVariable(Project.COLUMN_PRIMARY_KEY) int id,
	    RedirectAttributes redirectAttrs, SessionStatus status) {

	// Reset search entries in cache.
	AuthenticationToken auth = this.authHelper.getAuth();
	Project project = this.projectService.getByID(id);

	// Get company and
	// clear cache.
	Long companyID = null;
	if (auth.getCompany() == null) {
	    if (project.getCompany() != null) {
		companyID = project.getCompany().getId();
	    }
	} else {
	    companyID = auth.getCompany().getId();
	}
	this.projectService.clearSearchCache(companyID);

	// Do service.
	// FIXME Cleanup also the SYS_HOME.
	// Get response.
	String response = this.projectService.delete(id);

	// Attach response.
	// Alert result.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return listPage(status);
    }

    /**
     * Return to the list page.
     * 
     * @param status
     * @return
     */
    private String listPage(SessionStatus status) {
	status.setComplete();
	return RegistryURL.REDIRECT_LIST_PROJECT;
    }

    /**
     * Unassign all fields of a project.
     * 
     * @param fieldID
     * @param projectID
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_UNASSIGN + "/" + Field.OBJECT_NAME + "/"
	    + ConstantsSystem.ALL, method = RequestMethod.GET)
    public String unassignAllFields(HttpSession session, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	// Get project ID.
	Project proj = (Project) session.getAttribute(ProjectController.ATTR_PROJECT);
	long projectID = proj.getId();

	// Get response.
	String response = this.fieldService.unassignAllFieldsFromProject(projectID);

	// Attach response.
	// Construct notification.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Do service and clear session vars.
	// Then return.
	return redirectEditPageProject(projectID, status);
    }

    /**
     * Assign a field to a project.
     * 
     * @param fieldAssignment
     * @param fieldID
     * @param projectID
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_ASSIGN + "/" + Field.OBJECT_NAME, method = RequestMethod.POST)
    public String assignField(HttpSession session,
	    @ModelAttribute(ATTR_FIELD) FormFieldAssignment faBean, RedirectAttributes redirectAttrs,
	    SessionStatus status) {

	// Get project from session.
	// Construct commit object.
	Project proj = (Project) session.getAttribute(ProjectController.ATTR_PROJECT);
	long fieldID = 1;
	FieldAssignment fieldAssignment = new FieldAssignment();
	fieldAssignment.setLabel(faBean.getLabel());
	fieldAssignment.setField(new Field(faBean.getFieldID()));
	fieldAssignment.setValue(faBean.getValue());
	fieldAssignment.setProject(proj);

	// Do service.
	// Get response.
	String response = this.fieldService.assignFieldToProject(fieldAssignment, fieldID, proj.getId());

	// Construct ui notifications.
	// Attach response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Remove session variables.
	// Evict project cache.
	return redirectEditPageProject(proj.getId(), status);
    }

    @RequestMapping(value = ConstantsSystem.REQUEST_EDIT + "/" + ConstantsRedis.OBJECT_PAYROLL + "/"
	    + ConstantsSystem.REQUEST_INCLUDE + "/" + Staff.OBJECT_NAME, method = RequestMethod.POST)
    public String includeStaffToPayroll(
	    @ModelAttribute(ATTR_PROJECT_PAYROLL) ProjectPayroll projectPayroll,
	    @ModelAttribute(ATTR_PAYROLL_INCLUDE_STAFF) FormPayrollIncludeStaff includeStaffBean,
	    RedirectAttributes redirectAttrs) {

	String response = this.projectPayrollService.includeStaffToPayroll(projectPayroll,
		includeStaffBean);

	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPagePayroll(projectPayroll);
    }

    /**
     * Do Update a pull out.
     * 
     * @param delivery
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_UPDATE + "/" + ConstantsRedis.OBJECT_PULL_OUT }, method = RequestMethod.POST)
    public String updatePullout(@ModelAttribute(ConstantsRedis.OBJECT_PULL_OUT) PullOut pullout,
	    RedirectAttributes redirectAttrs) {

	// Do service and get response.
	String response = this.pullOutService.update(pullout);

	// Add to redirect attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Complete the transaction.
	return redirectEditPageSubmodule(ConstantsRedis.OBJECT_PULL_OUT, pullout.getKey());
    }

    /**
     * Update a material object.
     * 
     * @param delivery
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_UPDATE + "/" + ConstantsRedis.OBJECT_MATERIAL }, method = RequestMethod.POST)
    public String updateMaterial(@ModelAttribute(ConstantsRedis.OBJECT_MATERIAL) Material material,
	    RedirectAttributes redirectAttrs) {

	// Do service and get response.
	String response = this.materialService.update(material);

	// Add to redirect attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Complete the transaction.
	return redirectEditPageSubmodule(ConstantsRedis.OBJECT_MATERIAL, material.getKey());
    }

    /**
     * Create a delivery object.
     * 
     * @param delivery
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_CREATE + "/" + ConstantsRedis.OBJECT_DELIVERY }, method = RequestMethod.POST)
    public String createDelivery(@ModelAttribute(ConstantsRedis.OBJECT_DELIVERY) Delivery delivery,
	    RedirectAttributes redirectAttrs) {

	// Do service and get response.
	String response = this.deliveryService.set(delivery);

	// Add to redirect attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageSubmodule(ConstantsRedis.OBJECT_DELIVERY, delivery.getKey());
    }

    /**
     * Return to the edit page of the submodule.
     * 
     * @param submodule
     * @param key
     * @return
     */
    private String redirectEditPageSubmodule(String submodule, String key) {
	String deliveryEdit = ConstantsSystem.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ ConstantsSystem.REQUEST_EDIT + "/" + submodule + "/" + key + "-end";
	return deliveryEdit;
    }

    /**
     * Create many tasks by uploading an Excel file.
     */
    @RequestMapping(value = { RegistryURL.MASS_UPLOAD_AND_ASSIGN_TASK }, method = RequestMethod.POST)
    public String createMassTask(
	    @ModelAttribute(ATTR_MASS_UPLOAD_STAFF_BEAN) FormMassUpload massUploadTask,
	    RedirectAttributes redirectAttrs, SessionStatus status, HttpSession session) {

	Project proj = massUploadTask.getProject();

	// Do service and get response.
	String response = this.projectService.createTasksFromExcel(massUploadTask.getFile(), proj);

	// Add to redirect attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Complete the transaction.
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Create many staff members by uploading an Excel file.
     */
    @RequestMapping(value = { RegistryURL.MASS_UPLOAD_AND_ASSIGN_STAFF }, method = RequestMethod.POST)
    public String createMassStaff(
	    @ModelAttribute(ATTR_MASS_UPLOAD_STAFF_BEAN) FormMassUpload massUploadStaff,
	    RedirectAttributes redirectAttrs, SessionStatus status, HttpSession session) {

	Project proj = massUploadStaff.getProject();

	// Do service and get response.
	String response = this.projectService.createStaffFromExcel(massUploadStaff.getFile(), proj);

	// Add to redirect attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Complete the transaction.
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Commit function that would create/update staff.
     * 
     * @param staff
     * @param redirectAttrs
     * @return
     */
    @RequestMapping(value = RegistryURL.CREATE_STAFF, method = RequestMethod.POST)
    public String createStaff(@ModelAttribute(ATTR_STAFF) Staff staff, RedirectAttributes redirectAttrs) {

	String response = "";

	// Create staff.
	if (staff.getId() == 0) {
	    response = this.staffService.create(staff);
	}
	// Update staff.
	else {
	    response = this.staffService.update(staff);
	}

	// Add redirs attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);
	return redirectEditPageStaff(staff.getId());
    }

    /**
     * Return to edit staff page.
     * 
     * @param id
     * @return
     */
    private String redirectEditPageStaff(long id) {
	return String.format(RegistryURL.REDIRECT_EDIT_PROJECT_STAFF, id);
    }

    /**
     * Return to edit task page.
     * 
     * @param id
     * @return
     */
    private String redirectEditPageTask(long id) {
	return String.format(RegistryURL.REDIRECT_EDIT_PROJECT_TASK, id);
    }

    /**
     * Do create/update on an estimate.
     * 
     * @param delivery
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_CREATE + "/" + ConstantsRedis.OBJECT_ESTIMATE }, method = RequestMethod.POST)
    public String createEstimate(
	    @ModelAttribute(ProjectController.ATTR_ESTIMATE_INPUT) EstimateComputationInputBean estimateInput,
	    RedirectAttributes redirectAttrs, SessionStatus status, HttpSession session) {

	// Do service and get response.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	estimateInput.setProject(proj);
	String response = this.estimateService.estimate(estimateInput);

	// Add to redirect attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Complete the transaction.
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Delete a delivery.
     * 
     * @param key
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { RegistryURL.VIEW_ESTIMATION_RESULTS }, method = RequestMethod.GET)
    public String viewEstimation(@PathVariable(ConstantsRedis.OBJECT_ESTIMATION_OUTPUT) String key,
	    Model model) {

	// Get estimation output.
	EstimationOutput output = this.estimationOutputService.get(key);

	// Attach to model.
	model.addAttribute(ConstantsRedis.OBJECT_ESTIMATION_OUTPUT, output);

	// Return.
	return RegistryJSPPath.JSP_EDIT_ESTIMATION_OUTPUT;
    }

    /**
     * Delete an estimation.
     * 
     * @param key
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { RegistryURL.DELETE_ESTIMATION_RESULTS }, method = RequestMethod.GET)
    public String deleteEstimate(@PathVariable(ConstantsRedis.OBJECT_ESTIMATION_OUTPUT) String key,
	    RedirectAttributes redirectAttrs, SessionStatus status, HttpSession session) {

	// Do service
	// and get response.
	String response = this.estimationOutputService.delete(key);

	// Attach to redirect.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Set completed.
	// Return.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Delete a delivery.
     * 
     * @param key
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_DELETE + "/" + ConstantsRedis.OBJECT_DELIVERY
	    + "/{" + ConstantsRedis.OBJECT_DELIVERY + "}-end" }, method = RequestMethod.GET)
    public String deleteDelivery(@PathVariable(ConstantsRedis.OBJECT_DELIVERY) String key,
	    RedirectAttributes redirectAttrs, SessionStatus status, HttpSession session) {

	// Do service
	// and get response.
	String response = this.deliveryService.delete(key);

	// Attach to redirect.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Set completed.
	// Return.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Delete a payroll entry.
     * 
     * @param key
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_DELETE + "/" + ConstantsRedis.OBJECT_PAYROLL
	    + "/{" + ConstantsRedis.OBJECT_PAYROLL + "}-end" }, method = RequestMethod.GET)
    public String deleteProjectPayroll(@PathVariable(ConstantsRedis.OBJECT_PAYROLL) String key,
	    RedirectAttributes redirectAttrs, SessionStatus status, HttpSession session) {

	// Do service
	// and get response.
	String response = this.projectPayrollService.delete(key);

	// Attach to redirect.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Set completed.
	// Return.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Delete a pull-out entry.
     * 
     * @param key
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_DELETE + "/" + ConstantsRedis.OBJECT_PULL_OUT
	    + "/{" + ConstantsRedis.OBJECT_PULL_OUT + "}-end" }, method = RequestMethod.GET)
    public String deletePullOut(@PathVariable(ConstantsRedis.OBJECT_PULL_OUT) String key,
	    RedirectAttributes redirectAttrs, SessionStatus status, HttpSession session) {

	// Do service
	// and get response.
	String response = this.pullOutService.delete(key);

	// Attach to redirect.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Set completed.
	// Return.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Create a task.
     * 
     * @param task
     * @param status
     * @param origin
     * @param originID
     * @param redirectAttrs
     * @return
     */
    @RequestMapping(value = RegistryURL.CREATE_TASK, method = RequestMethod.POST)
    public String createTask(@ModelAttribute(ATTR_TASK) Task task, SessionStatus status,
	    RedirectAttributes redirectAttrs) {

	String response = "";
	if (task.getId() == 0) {
	    response = this.taskService.create(task);
	} else {
	    response = this.taskService.update(task);
	}

	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);
	return redirectEditPageTask(task.getId());
    }

    /**
     * Open a view page where the user can edit the staff.
     * 
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = RegistryURL.EDIT_ATTENDANCE_RANGE)
    public String editAttendanceRange(
	    @ModelAttribute(ATTR_CALENDAR_RANGE_DATES) FormDateRange rangeDates, HttpSession session,
	    Model model, RedirectAttributes redirectAttrs) {
	// Get prelim objects.
	Staff staff = (Staff) session.getAttribute(ATTR_STAFF);
	Project project = (Project) session.getAttribute(ATTR_PROJECT);

	// Get the start and end date from the bean.
	Date min = rangeDates.getStartDate();
	Date max = rangeDates.getEndDate();

	// If start date is > end date, error.
	if (min.after(max)) {
	    redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, AlertBoxGenerator.FAILED
		    .generateHTML(RegistryResponseMessage.ERROR_START_DATE_GT_END_DATE));
	    return redirectEditPageStaff(staff.getId());
	}

	// Given min and max, get range of attendances.
	// Get wage given attendances.
	String maxDateStr = DateUtils.formatDate(max, "yyyy-MM-dd");

	// Add attributes to model.
	setStaffAttributes(model, project, staff, min, max, maxDateStr);

	return RegistryJSPPath.JSP_EDIT_STAFF;
    }

    /**
     * Add an attendance.
     * 
     * @return
     */
    @RequestMapping(value = { RegistryURL.ADD_ATTENDACE }, method = RequestMethod.POST)
    public String addAttendance(@ModelAttribute(ATTR_ATTENDANCE) Attendance attendance,
	    HttpSession session, Model model) {

	// Do service.
	String response = this.attendanceService.set(attendance);
	model.addAttribute(ConstantsSystem.UI_PARAM_ALERT, response);
	return redirectEditPageStaffCalMaxDate(model, session, attendance.getDate());
    }

    /**
     * TODO Clean up.<br>
     * Add an attendance in mass.
     * 
     * @return
     */
    @RequestMapping(value = { RegistryURL.MASS_ADD_ATTENDACE }, method = RequestMethod.POST)
    public String addMassAttendance(
	    @ModelAttribute(ATTR_ATTENDANCE_MASS) FormMassAttendance attendanceMass,
	    RedirectAttributes redirectAttrs, HttpSession session, Model model) {

	Date startDate = attendanceMass.getStartDate();
	Date endDate = attendanceMass.getEndDate();

	// If start date is > end date, error.
	if (startDate.after(endDate)) {
	    redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, AlertBoxGenerator.FAILED
		    .generateHTML(RegistryResponseMessage.ERROR_START_DATE_GT_END_DATE));
	    return redirectEditPageStaff(attendanceMass.getStaff().getId());
	}

	// Do service.
	String response = this.attendanceService.multiSet(attendanceMass);
	model.addAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageStaffCalMaxDate(model, session, startDate);
    }

    /**
     * Open a view page where the user can edit the staff.
     * 
     * @param id
     * @param model
     * @return
     */
    private String redirectEditPageStaffCalMaxDate(Model model, HttpSession session, Date minDate) {

	// If the min date from session is lesser
	// than min date passed, use from session.
	Date minDateFromSession = (Date) session.getAttribute(ATTR_CALENDAR_MIN_DATE);
	if (minDateFromSession.before(minDate)) {
	    minDate = minDateFromSession;
	}

	// Get staff object.
	// Get the current year and month.
	// This will be minimum.
	Staff staff = (Staff) session.getAttribute(ATTR_STAFF);
	Project project = (Project) session.getAttribute(ATTR_PROJECT);
	Date maxDate = (Date) session.getAttribute(ATTR_CALENDAR_MAX_DATE);
	String maxDateStr = (String) session.getAttribute(ATTR_CALENDAR_MAX_DATE_STR);

	// Set model attributes.
	setStaffAttributes(model, project, staff, minDate, maxDate, maxDateStr);
	return RegistryJSPPath.JSP_EDIT_STAFF;
    }

    /**
     * TODO Clean up. <br>
     * Edit a staff.
     * 
     * @param taskID
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(value = RegistryURL.EDIT_STAFF, method = RequestMethod.GET)
    public String editStaff(@PathVariable(Staff.OBJECT_NAME) long staffID, Model model,
	    HttpSession session) {

	Project proj = (Project) session.getAttribute(ATTR_PROJECT);

	// If ID is zero,
	// Open a page with empty values, ready to create.
	if (staffID == 0) {
	    model.addAttribute(ATTR_FROM_PROJECT, true);
	    model.addAttribute(ATTR_STAFF, new Staff());
	    return RegistryJSPPath.JSP_EDIT_STAFF;
	}

	// Else, get the object from DB
	// then populate the fields in JSP.
	Staff staff = this.staffService.getWithAllCollectionsByID(staffID);
	model.addAttribute(ATTR_PROJECT, proj);

	Map<String, Date> datePair = getCalendarRangeDates(session);
	Date min = datePair.get(ATTR_CALENDAR_MIN_DATE);
	Date max = datePair.get(ATTR_CALENDAR_MAX_DATE);

	// Set model attributes.
	setStaffAttributes(model, proj, staff, min, max, null);

	return RegistryJSPPath.JSP_EDIT_STAFF;
    }

    /**
     * TODO Clean up. <br>
     * Set model attributes before forwarding to JSP.
     * 
     * @param model
     * @param staff
     * @param min
     * @param max
     * @param maxDateStr
     */
    private void setStaffAttributes(Model model, Project project, Staff staff, Date min, Date max,
	    String maxDateStr) {

	// Given min and max, get range of attendances.
	// Get wage given attendances.
	Set<Attendance> attendanceList = this.attendanceService.rangeStaffAttendance(project, staff,
		min, max);

	// Given min and max, get range of attendances.
	// Get wage given attendances.
	model.addAttribute(ATTR_PAYROLL_TOTAL_WAGE,
		this.attendanceService.getTotalWageFromAttendance(attendanceList));

	// Get attendance status map based on enum.
	model.addAttribute(ATTR_CALENDAR_STATUS_LIST, AttendanceStatus.getAllStatusInMap());

	// Get start date of calendar.
	// Add minimum and maximum of data loaded.
	model.addAttribute(ATTR_CALENDAR_MAX_DATE_STR,
		maxDateStr == null ? DateUtils.formatDate(max, "yyyy-MM-dd") : maxDateStr);
	model.addAttribute(ATTR_CALENDAR_MIN_DATE, min);
	model.addAttribute(ATTR_CALENDAR_MAX_DATE, max);
	model.addAttribute(ATTR_TASK_STATUS_MAP, this.staffService.getTaskStatusCountMap(staff));
	model.addAttribute(ATTR_ATTENDANCE_STATUS_MAP,
		this.staffService.getAttendanceStatusCountMap(attendanceList));

	// Add objects.
	// Add form beans.
	Company co = staff.getCompany();
	model.addAttribute(ATTR_STAFF, staff);
	model.addAttribute(ATTR_ATTENDANCE_LIST, attendanceList);
	model.addAttribute(ATTR_CALENDAR_RANGE_DATES, new FormDateRange());
	model.addAttribute(ATTR_ATTENDANCE_MASS, new FormMassAttendance(project, staff));
	model.addAttribute(ATTR_ATTENDANCE, new Attendance(co, project, staff));

	// Add front-end JSONs.
	model.addAttribute(ATTR_CALENDAR_JSON, this.staffService.getCalendarJSON(attendanceList));
	model.addAttribute(ATTR_GANTT_JSON, this.staffService.getGanttJSON(staff));

	model.addAttribute(ATTR_FROM_PROJECT, true);
    }

    /**
     * Get calendar min and max dates from session.
     * 
     * @param session
     * @return
     */
    private Map<String, Date> getCalendarRangeDates(HttpSession session) {
	Date min = (Date) session.getAttribute(ATTR_CALENDAR_MIN_DATE);
	Date max = (Date) session.getAttribute(ATTR_CALENDAR_MAX_DATE);

	// If null,
	// get current month.
	if (min == null) {
	    Calendar cal = Calendar.getInstance();
	    int year = cal.get(Calendar.YEAR);
	    int month = cal.get(Calendar.MONTH); // Zero-based.
	    min = new GregorianCalendar(year, month, 1).getTime();

	    // Based on minimum, get max days in current month.
	    // Given max days, create max object.
	    cal.setTime(min);
	    int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	    max = new GregorianCalendar(year, month, maxDays).getTime();
	}

	Map<String, Date> datePair = new HashMap<String, Date>();
	datePair.put(ATTR_CALENDAR_MIN_DATE, min);
	datePair.put(ATTR_CALENDAR_MAX_DATE, max);
	return datePair;
    }

    /**
     * Edit a task.
     * 
     * @param taskID
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(value = RegistryURL.EDIT_TASK, method = RequestMethod.GET)
    public String editTask(@PathVariable(Task.OBJECT_NAME) long taskID, Model model, HttpSession session) {

	// Task status selector.
	model.addAttribute(ATTR_TASK_STATUS_LIST, TaskStatus.class.getEnumConstants());

	// If ID is zero,
	// Open a page with empty values, ready to create.
	if (taskID == 0) {
	    Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	    model.addAttribute(ATTR_TASK, new Task(proj));
	    return RegistryJSPPath.JSP_EDIT_TASK;
	}

	// Else, get the object from DB
	// then populate the fields in JSP.
	Task task = this.taskService.getByIDWithAllCollections(taskID);

	return editPageTask(model, task.getProject(), task);
    }

    /**
     * Unassign a staff from a task.
     * 
     * @param projectID
     * @param staffID
     * @param position
     * @return
     */
    @RequestMapping(value = RegistryURL.UNASSIGN_TASK_STAFF, method = RequestMethod.GET)
    public String unassignTaskStaff(HttpSession session, @PathVariable(Staff.OBJECT_NAME) long staffID,
	    RedirectAttributes redirectAttrs) {

	// Get the object from the session.
	Task task = (Task) session.getAttribute(ATTR_TASK);

	// Do service and get response.
	String response = this.taskService.unassignStaffTask(task.getId(), staffID);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return redirectEditPageTask(task.getId());
    }

    /**
     * Assign a staff to a task.
     * 
     * @param projectID
     * @param staffID
     * @param staffAssignment
     * @return
     */
    @RequestMapping(value = RegistryURL.ASSIGN_TASK_STAFF, method = RequestMethod.POST)
    public String assignTaskStaff(HttpSession session,
	    @ModelAttribute(ATTR_STAFF_ASSIGNMENT) FormStaffAssignment staffAssignment,
	    RedirectAttributes redirectAttrs) {

	Task task = (Task) session.getAttribute(ATTR_TASK);

	// Do service.
	String response = this.taskService.assignStaffTask(task.getId(), staffAssignment.getStaffID());

	// Set response.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Then redirect.
	return redirectEditPageTask(task.getId());
    }

    /**
     * Unassign all staff from a task.
     * 
     * @param projectID
     * @return
     */
    @RequestMapping(value = RegistryURL.UNASSIGN_ALL_TASK_STAFF, method = RequestMethod.GET)
    public String unassignAllTaskStaff(HttpSession session, RedirectAttributes redirectAttrs) {

	// Get object from session.
	Task task = (Task) session.getAttribute(ATTR_TASK);
	long taskID = task.getId();

	// Do service and get response.
	String response = this.taskService.unassignAllStaffUnderTask(taskID);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);
	return redirectEditPageTask(taskID);
    }

    /**
     * Return to edit page.
     * 
     * @param model
     * @param proj
     * @param task
     * @return
     */
    private String editPageTask(Model model, Project proj, Task task) {
	// Total - assigned = staff to return.
	// Attach to model.
	Set<Staff> totalStaff = proj.getAssignedStaff();
	Set<Staff> assignedStaff = task.getStaff();
	totalStaff.removeAll(assignedStaff);
	model.addAttribute(ATTR_STAFF_LIST, totalStaff);

	// Add the task and beans.
	model.addAttribute(ATTR_STAFF_ASSIGNMENT, new FormStaffAssignment());
	model.addAttribute(ATTR_TASK, task);
	return RegistryJSPPath.JSP_EDIT_TASK;
    }

    /**
     * Delete all tasks.
     * 
     * @param taskID
     * @param session
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = RegistryURL.DELETE_TASK_ALL, method = RequestMethod.GET)
    public String deleteAllTask(HttpSession session, RedirectAttributes redirectAttrs,
	    SessionStatus status) {

	// Do service and get response.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	String response = this.taskService.deleteAllTasksByProject(proj.getId());
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Complete the transaction.
	// Redirect.
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Set the project task to the status specified. The method is GET since the
     * mark action is done via a href.
     * 
     * @param projectID
     * @param taskID
     * @param status
     * @return
     */
    @RequestMapping(value = RegistryURL.MARK_TASK, method = RequestMethod.GET)
    public String markTask(@RequestParam(Task.COLUMN_PRIMARY_KEY) long taskID,
	    @RequestParam(Task.COLUMN_STATUS) int status, RedirectAttributes redirectAttrs,
	    HttpSession session) {

	// Do service, get response.
	String response = this.taskService.mark(taskID, status);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	Project proj = (Project) session.getAttribute(ATTR_PROJECT);

	return redirectEditPageProject(proj.getId());
    }

    /**
     * Delete a task.
     * 
     * @param taskID
     * @param session
     * @param redirectAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = RegistryURL.DELETE_TASK, method = RequestMethod.GET)
    public String deleteTask(@PathVariable(Task.OBJECT_NAME) long taskID, HttpSession session,
	    RedirectAttributes redirectAttrs, SessionStatus status) {

	// Do service and get response.
	String response = this.taskService.delete(taskID);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Complete the transaction.
	// Redirect.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	return redirectEditPageProject(proj.getId(), status);
    }

    /**
     * Open an edit page for a material.
     * 
     * @param key
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_EDIT + "/" + ConstantsRedis.OBJECT_MATERIAL + "/{"
	    + ConstantsRedis.OBJECT_MATERIAL + "}-end" }, method = RequestMethod.GET)
    public String editMaterial(@PathVariable(ConstantsRedis.OBJECT_MATERIAL) String key, Model model,
	    HttpSession session) {

	// Construct the bean for the form.
	Material material = this.materialService.get(key);
	model.addAttribute(ATTR_MATERIAL, material);

	// Get the list of staff in this project.
	// This is for the selector.
	// Who pulled-out the material?
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	Set<Staff> staffList = proj.getAssignedStaff();

	// Add material category list.
	// And Units list.
	model.addAttribute(ATTR_MATERIAL_CATEGORY_LIST, MaterialCategory.class.getEnumConstants());
	model.addAttribute(ATTR_UNIT_LIST_LENGTH, CommonLengthUnit.class.getEnumConstants());
	model.addAttribute(ATTR_UNIT_LIST_MASS, CommonMassUnit.class.getEnumConstants());
	model.addAttribute(ATTR_UNIT_LIST_VOLUME, CommonVolumeUnit.class.getEnumConstants());

	// Add the staff list to model.
	model.addAttribute(ATTR_STAFF_LIST, staffList);

	return RegistryJSPPath.JSP_EDIT_MATERIAL;
    }

    /**
     * Open a create page for a pull out.
     * 
     * @param key
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_PULL_OUT + "/" + ConstantsRedis.OBJECT_MATERIAL
	    + "/{" + ConstantsRedis.OBJECT_MATERIAL + "}-end" }, method = RequestMethod.GET)
    public String pulloutMaterial(@PathVariable(ConstantsRedis.OBJECT_MATERIAL) String key, Model model,
	    HttpSession session, RedirectAttributes redirectAttrs) {

	// You cannot pull-out materials if no staff is assigned to this
	// project.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	if (proj.getAssignedStaff().isEmpty()) {
	    redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, AlertBoxGenerator.FAILED
		    .generateHTML(RegistryResponseMessage.ERROR_PULLOUT_NO_PROJECT_STAFF));
	    return redirectEditPageProject(proj.getId());
	}

	// Construct the bean for the form.
	Material material = this.materialService.get(key);
	PullOut pullOut = new PullOut(material);
	model.addAttribute(ATTR_PULL_OUT, pullOut);

	// Get the list of staff in this project.
	// This is for the selector.
	// Who pulled-out the material?
	Set<Staff> staffList = proj.getAssignedStaff();

	// Add the staff list to model.
	model.addAttribute(ATTR_STAFF_LIST, staffList);

	return RegistryJSPPath.JSP_EDIT_MATERIAL_PULLOUT;
    }

    /**
     * Do the pull-out of materials.
     * 
     * @param pullOut
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_DO_PULL_OUT + "/" + ConstantsRedis.OBJECT_MATERIAL }, method = RequestMethod.POST)
    public String createPullOut(@ModelAttribute(ATTR_PULL_OUT) PullOut pullOut,
	    RedirectAttributes redirectAttrs) {

	// Do service
	// and get response.
	String response = this.pullOutService.create(pullOut);

	// Add to model.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	if (pullOut.getUuid() == null) {
	    return redirectEditPageProject(pullOut.getProject().getId());
	}

	return redirectEditPageSubmodule(ConstantsRedis.OBJECT_PULL_OUT, pullOut.getKey());
    }

    /**
     * Delete a material.
     * 
     * @param material
     * @param redirecAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_DELETE + "/" + ConstantsRedis.OBJECT_MATERIAL
	    + "/{" + ConstantsRedis.OBJECT_MATERIAL + "}-end" }, method = RequestMethod.GET)
    public String deleteMaterial(@PathVariable(ConstantsRedis.OBJECT_MATERIAL) String key,
	    RedirectAttributes redirecAttrs, SessionStatus status, HttpSession session) {

	// Do service
	// and get response.
	String response = this.materialService.delete(key);

	// Attach to redirect attributes.
	redirecAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// Set completed.
	// Return to the project.
	Project project = (Project) session.getAttribute(ATTR_PROJECT);
	return redirectEditPageProject(project.getId(), status);
    }

    /**
     * Add a material to delivery.
     * 
     * @param material
     * @param redirecAttrs
     * @param status
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_ADD + "/" + ConstantsRedis.OBJECT_MATERIAL }, method = RequestMethod.POST)
    public String addMaterial(@ModelAttribute(ConstantsRedis.OBJECT_MATERIAL) Material material,
	    RedirectAttributes redirecAttrs, SessionStatus status) {

	// Do service
	// and get response.
	String response = this.materialService.create(material);

	// Attach to redirect attributes.
	redirecAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return ConstantsSystem.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ ConstantsSystem.REQUEST_EDIT + "/" + ConstantsRedis.OBJECT_DELIVERY + "/"
		+ material.getDelivery().getKey() + "-end";
    }

    /**
     * Open an edit page with payroll object.
     * 
     * @param payrollKey
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_CREATE + "/" + ConstantsRedis.OBJECT_PAYROLL }, method = RequestMethod.POST)
    public String createPayroll(@ModelAttribute(ATTR_PROJECT_PAYROLL) ProjectPayroll projectPayroll,
	    Model model, HttpSession session, RedirectAttributes redirectAttrs) {

	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	if (proj == null) {
	    proj = this.projectService.getByIDWithAllCollections(projectPayroll.getProject().getId());
	}

	// Do service.
	String response = this.projectPayrollService.createPayroll(proj, projectPayroll);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// If payroll creation was rejected,
	// return back to project.
	if (projectPayroll.getUuid() == null) {
	    return redirectEditPageProject(proj.getId());
	}

	// List of possible approvers.
	setFormSelectors(proj, model);

	// Complete the transaction.
	return redirectEditPagePayroll(projectPayroll);
    }

    private String redirectEditPageProject(long id) {
	return redirectEditPageProject(id, null);
    }

    /**
     * Open an edit page with payroll object.
     * 
     * @param payrollKey
     * @return
     */
    @RequestMapping(value = { ConstantsSystem.REQUEST_CREATE + "/" + ConstantsRedis.OBJECT_PAYROLL + "/"
	    + ConstantsSystem.CLEAR + "/{" + ConstantsSystem.CLEAR + "}" }, method = RequestMethod.POST)
    public String createPayrollClearComputation(
	    @ModelAttribute(ATTR_PROJECT_PAYROLL) ProjectPayroll projectPayroll,
	    @PathVariable(ConstantsSystem.CLEAR) String toClear, Model model, HttpSession session,
	    RedirectAttributes redirectAttrs) {

	// End the session after this.
	// Then redirect to an edit page of this object.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	if (proj == null) {
	    proj = this.projectService.getByIDWithAllCollections(projectPayroll.getProject().getId());
	}

	// Update the payroll then clear the computation.
	String response = this.projectPayrollService.updatePayrollClearComputation(session,
		projectPayroll, toClear);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// List of possible approvers.
	setFormSelectors(proj, model);

	// Redirect to:
	return redirectEditPagePayroll(projectPayroll);
    }

    /**
     * Open an edit page with payroll object.
     * 
     * @param payrollKey
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_COMPUTE + "/" + ConstantsRedis.OBJECT_PAYROLL, method = RequestMethod.GET)
    public String computePayroll(@ModelAttribute(ATTR_PROJECT_PAYROLL) ProjectPayroll projectPayroll,
	    Model model, HttpSession session, RedirectAttributes redirectAttrs) {

	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	if (proj == null) {
	    proj = this.projectService.getByIDWithAllCollections(projectPayroll.getProject().getId());
	}
	Date startDate = projectPayroll.getStartDate();
	Date endDate = projectPayroll.getEndDate();

	// Get payroll maps.
	// And assign to model.
	String payrollJSON = this.projectPayrollService.computeAndGetResultJSON(proj, startDate,
		endDate, projectPayroll);

	// Construct response.
	String datePart = ProjectPayrollServiceImpl.getResponseDatePart(projectPayroll);
	String response = AlertBoxGenerator.SUCCESS.generateCompute(ConstantsRedis.OBJECT_PAYROLL,
		datePart);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	// List of possible approvers.
	// Get all managers in this project.
	model.addAttribute(ATTR_PAYROLL_JSON, payrollJSON);
	model.addAttribute(ATTR_PROJECT_PAYROLL, projectPayroll);
	setFormSelectors(proj, model);

	return redirectEditPagePayroll(projectPayroll);
    }

    /**
     * End state of payroll for create, update, and compute.
     * 
     * @param status
     * @param redirectAttrs
     * @param projectPayroll
     * @return
     */
    private String redirectEditPagePayroll(ProjectPayroll projectPayroll) {

	// /edit/payroll/${payrollRow.getKey()}-end
	return ConstantsSystem.CONTROLLER_REDIRECT + Project.OBJECT_NAME + "/"
		+ ConstantsSystem.REQUEST_EDIT + "/" + ConstantsRedis.OBJECT_PAYROLL + "/"
		+ projectPayroll.getKey() + "-end";
    }

    /**
     * Open an edit page for a pull-out.
     * 
     * @param key
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_EDIT + "/" + ConstantsRedis.OBJECT_PULL_OUT + "/{"
	    + ConstantsRedis.OBJECT_PULL_OUT + "}-end", method = RequestMethod.GET)
    public String editPullOut(@PathVariable(ConstantsRedis.OBJECT_PULL_OUT) String key, Model model,
	    HttpSession session) {

	// Get the object.
	PullOut pullOut = this.pullOutService.get(key);
	model.addAttribute(ATTR_PULL_OUT, pullOut);

	// Get the list of staff in this project.
	// This is for the selector.
	// Who pulled-out the material?
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);
	Set<Staff> staffList = proj.getAssignedStaff();

	// Add the staff list to model.
	model.addAttribute(ATTR_STAFF_LIST, staffList);

	// redirect to edit page.
	return RegistryJSPPath.JSP_EDIT_MATERIAL_PULLOUT;
    }

    /**
     * Open an edit page to create/update an object.
     * 
     * @param key
     * @param model
     * @param session
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_EDIT + "/" + ConstantsRedis.OBJECT_DELIVERY + "/{"
	    + ConstantsRedis.OBJECT_DELIVERY + "}-end", method = RequestMethod.GET)
    public String editDelivery(@PathVariable(ConstantsRedis.OBJECT_DELIVERY) String key, Model model,
	    HttpSession session) {

	Project proj = (Project) session.getAttribute(ATTR_PROJECT);

	// If we're creating.
	// Return an empty object.
	if (key.equals("0")) {
	    model.addAttribute(ATTR_DELIVERY, new Delivery(proj));
	    return RegistryJSPPath.JSP_EDIT_DELIVERY;
	}

	// Add material category list.
	// And Units list.
	model.addAttribute(ATTR_MATERIAL_CATEGORY_LIST, MaterialCategory.class.getEnumConstants());
	model.addAttribute(ATTR_UNIT_LIST_LENGTH, CommonLengthUnit.class.getEnumConstants());
	model.addAttribute(ATTR_UNIT_LIST_MASS, CommonMassUnit.class.getEnumConstants());
	model.addAttribute(ATTR_UNIT_LIST_VOLUME, CommonVolumeUnit.class.getEnumConstants());

	// If we're updating,
	// return the object from redis.
	Delivery delivery = this.deliveryService.get(key);
	model.addAttribute(ATTR_DELIVERY, delivery);

	// Get the list of materials this delivery has.
	// Then add to model.
	List<Material> materialList = this.materialService.list(delivery);
	model.addAttribute(ATTR_MATERIAL_LIST, materialList);
	model.addAttribute(ATTR_MATERIAL, new Material(delivery));

	return RegistryJSPPath.JSP_EDIT_DELIVERY;
    }

    /**
     * Open an edit page with payroll object.
     * 
     * @param payrollKey
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_EDIT + "/" + ConstantsRedis.OBJECT_PAYROLL + "/{"
	    + ConstantsRedis.OBJECT_PAYROLL + "}-end", method = RequestMethod.GET)
    public String editPayroll(@PathVariable(ConstantsRedis.OBJECT_PAYROLL) String payrollKey,
	    Model model, HttpSession session, SessionStatus status, RedirectAttributes redirectAttrs) {

	// Common to both edit new and existing.
	// List of all payroll status.
	Project proj = (Project) session.getAttribute(ATTR_PROJECT);

	if (proj.getAssignedStaff().size() < 1) {
	    redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, AlertBoxGenerator.FAILED
		    .generateHTML(RegistryResponseMessage.ERROR_PAYROLL_NO_STAFF));
	    return redirectEditPageProject(proj.getId(), status);
	}

	// Set the form selectors.
	// Managers and status.
	setFormSelectors(proj, model);

	// Required for key creation.
	Company co = proj.getCompany();

	// If a new payroll object.
	if (payrollKey.equals("0")) {

	    // Assign the creator.
	    // Add the empty object.
	    // Then redirect.
	    SystemUser creator = this.authHelper.getAuth().getUser();
	    model.addAttribute(ATTR_PROJECT_PAYROLL, new ProjectPayroll(co, proj, creator));
	    return RegistryJSPPath.JSP_EDIT_PAYROLL;
	}

	// Attach to response.
	// If flash attribute was null,
	// use the key.
	ProjectPayroll projectPayroll = this.projectPayrollService.get(payrollKey);

	// Set the project structure.
	Long companyID = co == null ? 0 : co.getId();
	setModelAttributesOfPayroll(projectPayroll, proj, model, companyID);

	return RegistryJSPPath.JSP_EDIT_PAYROLL;
    }

    /**
     * Options for the payroll status selector.
     * 
     * @param model
     */
    private void setFormSelectors(Project proj, Model model) {

	// Status.
	PayrollStatus[] payrollStatusArr = PayrollStatus.class.getEnumConstants();
	model.addAttribute(ATTR_PAYROLL_SELECTOR_STATUS, payrollStatusArr);
    }

    /**
     * Set the project structure.
     * 
     * @param projectPayroll
     * @param proj
     * @param model
     * @param proj
     */
    private void setModelAttributesOfPayroll(ProjectPayroll projectPayroll, Project proj, Model model,
	    Long companyID) {

	// Get collection of all staff here.
	List<Staff> manualStaffList = this.staffService.listUnassignedStaffInProjectPayroll(companyID,
		projectPayroll);

	// Full list minus already included.
	Set<Staff> staff = projectPayroll.getStaffList();

	// Set attributes.
	// Manually include team/staff beans.
	model.addAttribute(ATTR_PAYROLL_INCLUDE_STAFF, new FormPayrollIncludeStaff());

	// Actual object and result JSON.
	model.addAttribute(ATTR_PROJECT_PAYROLL, projectPayroll);
	model.addAttribute(ATTR_PAYROLL_JSON, projectPayroll.getPayrollJSON());

	// Structure/checklist attributes.
	model.addAttribute(ATTR_PAYROLL_CHECKBOX_STAFF, staff);
	model.addAttribute(ATTR_PAYROLL_MANUAL_STAFF_LIST, manualStaffList);
    }

    /**
     * Open an existing/new project page. TODO Remove this function.
     * 
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/clear/cache/{id}")
    public String clearCache(@PathVariable("id") long id, SessionStatus status) {
	this.projectService.clearProjectCache(id);
	this.projectService.clearListCache();

	return redirectEditPageProject(id, status);
    }

    /**
     * Open an existing/new project page.
     * 
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_EDIT + "/{" + Project.COLUMN_PRIMARY_KEY + "}")
    public String editProject(@PathVariable(Project.COLUMN_PRIMARY_KEY) long id, Model model) {

	// Set model attributes that are common in both create and update.
	setModelAttributes(model, id);

	// If ID is zero, create new.
	if (id == 0) {
	    model.addAttribute(ATTR_PROJECT, new Project());
	    return RegistryJSPPath.JSP_EDIT_PROJECT;
	}

	Project proj = this.projectService.getByIDWithAllCollections(id);
	model.addAttribute(ATTR_PROJECT, proj);

	// Estimate.
	setEstimateAttributes(proj, model);

	// Staff.
	setStaffAttributes(proj, model);

	// Payroll.
	setPayrollAttributes(proj, model);

	// Inventory.
	setInventoryAttributes(proj, model);

	// Auxillary.
	setAuxAttributes(proj, model);

	// Program of Works.
	setProgramOfWorksAttributes(proj, model);

	return RegistryJSPPath.JSP_EDIT_PROJECT;
    }

    /**
     * Set attributes used in Estimate.
     * 
     * @param proj
     * @param model
     */
    private void setEstimateAttributes(Project proj, Model model) {
	List<EstimationOutput> estimates = this.estimationOutputService.list(proj);
	model.addAttribute(ATTR_ESTIMATE_OUTPUT_LIST, estimates);
    }

    /**
     * Set attributes used by Staff and Manager tab.
     * 
     * @param proj
     * @param model
     */
    private void setStaffAttributes(Project proj, Model model) {

	// Get list of fields.
	// Get list of staff members for manager assignments.
	Long companyID = this.authHelper.getAuth().isSuperAdmin() ? null : proj.getCompany().getId();

	// Used in the manager selector.
	List<Staff> staffList = this.staffService.list();

	// Used in the "assign staff constrols".
	// Get the list of staff not yet assigned in this project.
	// Company staff, minus managers, minus assigned.
	List<Staff> availableStaffToAssign = this.staffService.listUnassignedInProject(companyID, proj);

	// Get lists for selectors.
	model.addAttribute(ATTR_STAFF_LIST_AVAILABLE, availableStaffToAssign);
	model.addAttribute(ATTR_STAFF_LIST, staffList);
	model.addAttribute(ATTR_STAFF_POSITION, new FormStaffAssignment());
	model.addAttribute(ATTR_MASS_UPLOAD_STAFF_BEAN, new FormMassUpload(proj));
    }

    /**
     * Set auxillary objects.
     * 
     * @param proj
     * @param model
     */
    private void setAuxAttributes(Project proj, Model model) {
	// Add the auxillary object.
	ProjectAux projectAux = this.projectAuxService.get(proj);
	model.addAttribute(ATTR_PROJECT_AUX, projectAux);
    }

    /**
     * Set model attributes that are common in both create and update.
     * 
     * @param model
     * @param id
     */
    private void setModelAttributes(Model model, long id) {

	// List of project status.
	model.addAttribute(ATTR_PROJECT_STATUS_LIST, ProjectStatus.class.getEnumConstants());

	// Set common attributes.
	// Model for forms.
	model.addAttribute(ATTR_FIELD, new FormFieldAssignment(id, 1));

	// Estimation input.
	model.addAttribute(ATTR_ESTIMATE_INPUT, new EstimateComputationInputBean());
	model.addAttribute(ATTR_ESTIMATE_ALLOWANCE_LIST,
		TableEstimationAllowance.class.getEnumConstants());
    }

    /**
     * Set payroll attributes.
     * 
     * @param proj
     * @param model
     */
    private void setPayrollAttributes(Project proj, Model model) {
	// Get all payrolls.
	// Add to model.
	List<ProjectPayroll> payrollList = this.projectPayrollService.getAllPayrolls(proj);
	model.addAttribute(ATTR_PAYROLL_LIST, payrollList);
    }

    /**
     * Set inventory attributes.
     * 
     * @param proj
     * @param model
     */
    private void setInventoryAttributes(Project proj, Model model) {
	// Get all deliveries.
	// Get all pull-outs.
	// Get inventory.
	// Then add to model.
	List<Delivery> deliveryList = this.deliveryService.list(proj);
	model.addAttribute(ATTR_DELIVERY_LIST, deliveryList);

	// Get all materials.
	// Add to model.
	List<Material> materialList = this.materialService.list(proj);
	model.addAttribute(ATTR_MATERIAL_LIST, materialList);

	// Get all pull-outs.
	// Add to model.
	List<PullOut> pullOutList = this.pullOutService.list(proj);
	model.addAttribute(ATTR_PULL_OUT_LIST, pullOutList);
    }

    /**
     * Set attributes before forwarding back to JSP.
     * 
     * @param proj
     * @param model
     */
    private void setProgramOfWorksAttributes(Project proj, Model model) {

	// Task status selector.
	model.addAttribute(ATTR_TASK_STATUS_LIST, TaskStatus.class.getEnumConstants());

	// Gant JSON to be used by the chart in timeline.
	// Get calendar JSON.
	model.addAttribute(ATTR_GANTT_JSON, this.projectService.getGanttJSON(proj));
	model.addAttribute(ATTR_CALENDAR_JSON, this.projectService.getCalendarJSON(proj));

	// Timeline taks status and count map.
	// Summary map found in timeline tab.
	model.addAttribute(ATTR_TIMELINE_TASK_STATUS_MAP,
		this.projectService.getTaskStatusCountMap(proj));
	model.addAttribute(ATTR_CALENDAR_EVENT_TYPES_LIST, CalendarEventType.class.getEnumConstants());
	model.addAttribute(ATTR_GANTT_TYPE_LIST, GanttElement.class.getEnumConstants());
    }
}