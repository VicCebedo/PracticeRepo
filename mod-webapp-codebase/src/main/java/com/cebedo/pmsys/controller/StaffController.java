package com.cebedo.pmsys.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cebedo.pmsys.constants.ConstantsSystem;
import com.cebedo.pmsys.constants.RegistryJSPPath;
import com.cebedo.pmsys.constants.RegistryURL;
import com.cebedo.pmsys.model.Staff;
import com.cebedo.pmsys.service.StaffService;

@Controller
@SessionAttributes(

value = { StaffController.ATTR_STAFF }

)
@RequestMapping(Staff.OBJECT_NAME)
public class StaffController {

    public static final String ATTR_LIST = "staffList";
    public static final String ATTR_STAFF = Staff.OBJECT_NAME;
    public static final String ATTR_TASK_STATUS_MAP = "taskStatusMap";
    public static final String ATTR_GANTT_JSON = "ganttJSON";

    private StaffService staffService;

    @Autowired(required = true)
    @Qualifier(value = "staffService")
    public void setStaffService(StaffService s) {
	this.staffService = s;
    }

    @RequestMapping(value = { ConstantsSystem.REQUEST_ROOT,
	    ConstantsSystem.REQUEST_LIST }, method = RequestMethod.GET)
    public String listStaff(Model model, HttpSession session) {
	model.addAttribute(ATTR_LIST, this.staffService.listWithAllCollections());
	session.removeAttribute(ProjectController.ATTR_FROM_PROJECT);
	return RegistryJSPPath.JSP_LIST_STAFF;
    }

    private String editPage(long staffID, SessionStatus status) {
	if (status != null) {
	    status.setComplete();
	}
	return String.format(RegistryURL.REDIRECT_EDIT_STAFF, staffID);
    }

    /**
     * Commit function that would create/update staff.
     * 
     * @param staff
     * @param redirectAttrs
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_CREATE, method = RequestMethod.POST)
    public String create(@ModelAttribute(ATTR_STAFF) Staff staff, SessionStatus status,
	    RedirectAttributes redirectAttrs, BindingResult result) {

	String response = "";

	// Create staff.
	if (staff.getId() == 0) {
	    response = this.staffService.create(staff, result);
	}
	// Update staff.
	else {
	    response = this.staffService.update(staff, result);
	}

	// Add redirs attrs.
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);

	return editPage(staff.getId(), status);
    }

    /**
     * Delete coming from the staff list page.
     * 
     * @param id
     * @param redirectAttrs
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_DELETE + "/{" + Staff.OBJECT_NAME
	    + "}", method = RequestMethod.GET)
    public String delete(@PathVariable(Staff.OBJECT_NAME) long id, SessionStatus status,
	    RedirectAttributes redirectAttrs) {
	String response = this.staffService.delete(id);
	redirectAttrs.addFlashAttribute(ConstantsSystem.UI_PARAM_ALERT, response);
	status.setComplete();
	return ConstantsSystem.CONTROLLER_REDIRECT + ATTR_STAFF + "/" + ConstantsSystem.REQUEST_LIST;
    }

    /**
     * Open a view page where the user can edit the staff.
     * 
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = ConstantsSystem.REQUEST_EDIT + "/{" + Staff.COLUMN_PRIMARY_KEY + "}")
    public String editStaff(@PathVariable(Staff.COLUMN_PRIMARY_KEY) int id, Model model,
	    HttpSession session) {

	session.removeAttribute(ProjectController.ATTR_FROM_PROJECT);

	// If action is to create new staff.
	if (id == 0) {
	    model.addAttribute(ATTR_STAFF, new Staff());
	    return RegistryJSPPath.JSP_EDIT_STAFF;
	}

	// Get staff object.
	// Get the current year and month.
	// This will be minimum.
	Staff staff = this.staffService.getWithAllCollectionsByID(id);

	// Set model attributes.
	// Add objects.
	// Add front-end JSONs.
	model.addAttribute(ATTR_STAFF, staff);
	model.addAttribute(ATTR_TASK_STATUS_MAP, this.staffService.getTaskStatusCountMap(staff));
	model.addAttribute(ATTR_GANTT_JSON, this.staffService.getGanttJSON(staff));

	return RegistryJSPPath.JSP_EDIT_STAFF;
    }

}
