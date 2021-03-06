package com.cebedo.pmsys.service.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.cebedo.pmsys.constants.ConstantsRedis;
import com.cebedo.pmsys.domain.Attendance;
import com.cebedo.pmsys.enums.AuditAction;
import com.cebedo.pmsys.enums.StatusAttendance;
import com.cebedo.pmsys.factory.AlertBoxFactory;
import com.cebedo.pmsys.helper.AuthHelper;
import com.cebedo.pmsys.helper.MessageHelper;
import com.cebedo.pmsys.helper.ValidationHelper;
import com.cebedo.pmsys.model.Company;
import com.cebedo.pmsys.model.Project;
import com.cebedo.pmsys.model.Staff;
import com.cebedo.pmsys.pojo.FormMassAttendance;
import com.cebedo.pmsys.repository.impl.AttendanceValueRepoImpl;
import com.cebedo.pmsys.service.AttendanceService;
import com.cebedo.pmsys.utils.DateUtils;
import com.cebedo.pmsys.validator.AttendanceValidator;
import com.cebedo.pmsys.validator.FormMassAttendanceValidator;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private AuthHelper authHelper = new AuthHelper();
    private MessageHelper messageHelper = new MessageHelper();
    private ValidationHelper validationHelper = new ValidationHelper();

    private AttendanceValueRepoImpl attendanceValueRepo;

    public void setAttendanceValueRepo(AttendanceValueRepoImpl r) {
	this.attendanceValueRepo = r;
    }

    @Autowired
    AttendanceValidator attendanceValidator;

    @Autowired
    FormMassAttendanceValidator formMassAttendanceValidator;

    @Override
    @Transactional
    public String set(Attendance attendance, BindingResult result) {

	// Security check.
	if (!this.authHelper.hasAccess(attendance)) {
	    this.messageHelper.unauthorizedKey(ConstantsRedis.OBJECT_ATTENDANCE, attendance.getKey());
	    return AlertBoxFactory.ERROR;
	}

	// Service layer form validation.
	this.attendanceValidator.validate(attendance, result);
	if (result.hasErrors()) {
	    return this.validationHelper.errorMessageHTML(result);
	}

	// Log.
	Staff staff = attendance.getStaff();
	Project proj = attendance.getProject();
	this.messageHelper.auditableKey(AuditAction.ACTION_SET, Staff.OBJECT_NAME, staff.getId(),
		ConstantsRedis.OBJECT_ATTENDANCE, attendance.getKey(), proj, staff.getFullName());

	// Set the status.
	if (attendance.getStatus() == null) {
	    attendance.setStatus(StatusAttendance.of(attendance.getStatusID()));
	}
	StatusAttendance status = attendance.getStatus();

	// If status is delete.
	if (status == StatusAttendance.DELETE) {
	    deleteAllInDate(attendance);
	    return AlertBoxFactory.SUCCESS.generateDelete(ConstantsRedis.OBJECT_ATTENDANCE,
		    "on " + attendance.getFormattedDateString());
	}

	// If status is absent.
	if (status == StatusAttendance.ABSENT && attendance.getWage() > 0) {
	    attendance.setWage(0);
	}
	if (status != StatusAttendance.ABSENT && attendance.getWage() == 0) {
	    attendance.setWage(getWage(staff, status));
	}

	// Delete all previously declared attendance in this date.
	deleteAllInDate(attendance);
	this.attendanceValueRepo.set(attendance);

	return AlertBoxFactory.SUCCESS.generateSet(ConstantsRedis.OBJECT_ATTENDANCE,
		"on " + attendance.getFormattedDateString());
    }

    /**
     * Delete all previously declared attendance in this date.
     * 
     * @param attendance
     */
    private void deleteAllInDate(Attendance attendance) {

	// Security check.
	if (!this.authHelper.hasAccess(attendance)) {
	    this.messageHelper.unauthorizedKey(ConstantsRedis.OBJECT_ATTENDANCE, attendance.getKey());
	    return;
	}

	Staff staff = attendance.getStaff();
	String key = Attendance.constructPattern(attendance.getProject(), staff, attendance.getDate());
	Set<String> keys = this.attendanceValueRepo.keys(key);
	this.attendanceValueRepo.delete(keys);
    }

    /**
     * Get corresponding wage of a staff member.
     * 
     * @param staff
     * @param status
     * @return
     */
    private double getWage(Staff staff, StatusAttendance status) {

	// Security check.
	if (!this.authHelper.hasAccess(staff)) {
	    this.messageHelper.unauthorizedID(Staff.OBJECT_NAME, staff.getId());
	    return 0.0;
	}

	// Log.
	this.messageHelper.nonAuditableIDWithAssocNoKey(AuditAction.ACTION_GET, Staff.OBJECT_NAME,
		staff.getId(), Staff.PROPERTY_WAGE);

	double wage = 0;

	if (status == StatusAttendance.PRESENT) {
	    wage = staff.getWage();

	} else if (status == StatusAttendance.LEAVE) {
	    wage = staff.getWage();

	} else if (status == StatusAttendance.LATE) {
	    wage = staff.getWage();

	} else if (status == StatusAttendance.ABSENT) {
	    wage = 0;

	} else if (status == StatusAttendance.HALFDAY) {
	    wage = staff.getWage() / 2;
	}
	return wage;
    }

    /**
     * Get total wage of all attendances.
     */
    @Transactional
    @Override
    public double getTotalWageFromAttendance(Collection<Attendance> attendances) {

	double totalWage = 0;
	Staff staff = null;

	for (Attendance attd : attendances) {

	    // Security check.
	    if (!this.authHelper.hasAccess(attd)) {
		this.messageHelper.unauthorizedKey(ConstantsRedis.OBJECT_ATTENDANCE, attd.getKey());
		return 0.0;
	    }

	    totalWage += attd.getWage();
	    staff = staff == null ? attd.getStaff() : staff;
	}

	// Log.
	if (attendances.size() > 0) {
	    this.messageHelper.nonAuditableIDWithAssocNoKey(AuditAction.ACTION_GET, Staff.OBJECT_NAME,
		    staff.getId(), Staff.PROPERTY_WAGE);
	}

	return totalWage;
    }

    /**
     * Get total wage of staff given a min and max date.
     */
    @Transactional
    @Override
    public double getTotalWageOfStaffInRange(Project project, Staff staff, Date min, Date max) {

	// Security check.
	if (!this.authHelper.hasAccess(staff)) {
	    this.messageHelper.unauthorizedID(Staff.OBJECT_NAME, staff.getId());
	    return 0.0;
	}

	// Log.
	this.messageHelper.nonAuditableIDWithAssocNoKey(AuditAction.ACTION_GET, Staff.OBJECT_NAME,
		staff.getId(), Staff.PROPERTY_WAGE);

	// Get all the attendances.
	Set<Attendance> attendances = this.rangeStaffAttendance(project, staff, min, max);

	// Get total wage of all attendances.
	return getTotalWageFromAttendance(attendances);
    }

    /**
     * Get attendances given a range of min and max.
     * 
     * @param project
     */
    @Override
    @Transactional
    public Set<Attendance> rangeStaffAttendance(Project project, Staff staff, Long min, Long max,
	    boolean includeAll) {

	// Security check.
	if (!this.authHelper.hasAccess(staff)) {
	    this.messageHelper.unauthorizedID(Staff.OBJECT_NAME, staff.getId());
	    return new HashSet<Attendance>();
	}

	// Log.
	this.messageHelper.nonAuditableIDWithAssocNoKey(AuditAction.ACTION_RANGE, Staff.OBJECT_NAME,
		staff.getId(), ConstantsRedis.OBJECT_ATTENDANCE);

	Set<String> keys = this.attendanceValueRepo.keys(Attendance.constructPattern(project, staff));
	Set<Attendance> attnSet = new HashSet<Attendance>();
	for (String key : keys) {
	    Attendance attn = this.attendanceValueRepo.get(key);

	    // TODO Optimize this.
	    // Leverage on the Date object. Don't use Timestamp millis.
	    long timestamp = attn.getDate().getTime();
	    if (includeAll) {
		attnSet.add(attn);
		continue;
	    }
	    if (timestamp <= max && timestamp >= min) {
		attnSet.add(attn);
	    }
	}
	return attnSet;
    }

    @Override
    @Transactional
    public Set<Attendance> rangeStaffAttendance(Project project, Staff staff) {
	return this.rangeStaffAttendance(project, staff, null, null, true);
    }

    @Override
    @Transactional
    public Set<Attendance> rangeStaffAttendance(Project project, Staff staff, Date min, Date max) {
	return this.rangeStaffAttendance(project, staff, min.getTime(), max.getTime(), false);
    }

    @Transactional
    @Override
    public String multiSet(Project proj, FormMassAttendance attendanceMass, BindingResult result) {

	long[] excludeList = attendanceMass.getExcludeList();

	for (Staff staff : proj.getAssignedStaff()) {

	    long staffId = staff.getId();

	    // If staff ID exists in the exclude list, skip it.
	    boolean skip = false;
	    for (long excludeId : excludeList) {
		if (staffId == excludeId) {
		    skip = true;
		    break;
		}
	    }
	    if (skip) {
		continue;
	    }

	    // Add staff to attendance then do operation.
	    // TODO Add proper error messages if failed.
	    attendanceMass.setStaff(staff);
	    multiSet(attendanceMass, result);
	}

	Date startDate = attendanceMass.getStartDate();
	Date endDate = attendanceMass.getEndDate();
	String startDateStr = DateUtils.formatDate(startDate);
	String endDateStr = DateUtils.formatDate(endDate);
	return AlertBoxFactory.SUCCESS.generateSet(ConstantsRedis.OBJECT_ATTENDANCE,
		String.format("from %s to %s", startDateStr, endDateStr));
    }

    @Override
    @Transactional
    public String multiSet(FormMassAttendance attendanceMass, BindingResult result) {

	Staff staff = attendanceMass.getStaff();

	// Security check.
	if (!this.authHelper.hasAccess(staff)) {
	    this.messageHelper.unauthorizedID(Staff.OBJECT_NAME, staff.getId());
	    return AlertBoxFactory.ERROR;
	}

	// Service layer form validation.
	this.formMassAttendanceValidator.validate(attendanceMass, result);
	if (result.hasErrors()) {
	    return this.validationHelper.errorMessageHTML(result);
	}

	// Log.
	Project proj = attendanceMass.getProject();
	this.messageHelper.auditableKey(AuditAction.ACTION_SET_MULTI, Staff.OBJECT_NAME, staff.getId(),
		ConstantsRedis.OBJECT_ATTENDANCE, "Mass", proj, "Mass");

	// Get the wage.
	StatusAttendance status = StatusAttendance.of(attendanceMass.getStatusID());
	double wage = 0;
	if (status == StatusAttendance.ABSENT) {
	    attendanceMass.setWage(0);
	} else {
	    wage = attendanceMass.getWage() == 0 ? staff.getWage() : attendanceMass.getWage();
	}

	// Get the dates.
	boolean includeSaturdays = attendanceMass.isIncludeSaturdays();
	boolean includeSundays = attendanceMass.isIncludeSundays();
	Date startDate = attendanceMass.getStartDate();
	Date endDate = attendanceMass.getEndDate();
	List<Date> dates = DateUtils.getDatesBetweenDates(startDate, endDate);
	Map<String, Attendance> keyAttendanceMap = new HashMap<String, Attendance>();

	// Iterate through all dates.
	for (Date date : dates) {
	    Company co = staff.getCompany();
	    Attendance attendance = new Attendance(co, attendanceMass.getProject(), staff, status, date,
		    wage);

	    // Check if date is a weekend.
	    int dayOfWeek = DateUtils.getDayOfWeek(date);
	    boolean isSaturday = dayOfWeek == Calendar.SATURDAY;
	    boolean isSunday = dayOfWeek == Calendar.SUNDAY;

	    // If status is delete.
	    if (status == StatusAttendance.DELETE) {
		if ((!includeSaturdays && isSaturday) || (!includeSundays && isSunday)) {
		    continue;
		}
		deleteAllInDate(attendance);
		continue;
	    }

	    deleteAllInDate(attendance);
	    if ((!includeSaturdays && isSaturday) || (!includeSundays && isSunday)) {
		continue;
	    }
	    keyAttendanceMap.put(attendance.getKey(), attendance);
	}

	String response = "";
	String startDateStr = DateUtils.formatDate(startDate);
	String endDateStr = DateUtils.formatDate(endDate);
	if (status != StatusAttendance.DELETE) {
	    this.attendanceValueRepo.multiSet(keyAttendanceMap);
	    response = AlertBoxFactory.SUCCESS.generateSet(ConstantsRedis.OBJECT_ATTENDANCE,
		    String.format("from %s to %s", startDateStr, endDateStr));
	} else {
	    response = AlertBoxFactory.SUCCESS.generateDelete(ConstantsRedis.OBJECT_ATTENDANCE,
		    String.format("from %s to %s", startDateStr, endDateStr));
	}
	return response;
    }

}
