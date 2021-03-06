package com.cebedo.pmsys.service;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.springframework.validation.BindingResult;

import com.cebedo.pmsys.domain.Attendance;
import com.cebedo.pmsys.model.Project;
import com.cebedo.pmsys.model.Staff;
import com.cebedo.pmsys.pojo.FormMassAttendance;

public interface AttendanceService {

    public String set(Attendance attendance, BindingResult result);

    /**
     * Get attendances of a staff in a range of time.
     * 
     * @param project
     * 
     * @param staff
     * @param min
     * @param max
     * @return
     */
    public Set<Attendance> rangeStaffAttendance(Project project, Staff staff, Date min, Date max);

    public Set<Attendance> rangeStaffAttendance(Project project, Staff staff, Long min, Long max,
	    boolean includeAll);

    /**
     * Get the total wage of a staff in a range of dates.
     * 
     * @param project
     * 
     * @param min
     * @param max
     * @return
     */
    public double getTotalWageOfStaffInRange(Project project, Staff staff, Date min, Date max);

    public double getTotalWageFromAttendance(Collection<Attendance> attendances);

    public String multiSet(FormMassAttendance attendanceMass, BindingResult result);

    public String multiSet(Project proj, FormMassAttendance attendanceMass, BindingResult result);

    public Set<Attendance> rangeStaffAttendance(Project project, Staff staff);

}
