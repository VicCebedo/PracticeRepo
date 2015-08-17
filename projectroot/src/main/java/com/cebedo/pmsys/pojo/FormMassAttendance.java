package com.cebedo.pmsys.pojo;

import java.util.Date;

import com.cebedo.pmsys.model.Staff;

public class FormMassAttendance {

	private Staff staff;
	private Date startDate;
	private Date endDate;
	private int statusID;
	private double wage;
	private boolean includeWeekends;

	public FormMassAttendance() {
		;
	}

	public FormMassAttendance(Staff stf) {
		setStaff(stf);
		setWage(stf.getWage());
	}

	public boolean isIncludeWeekends() {
		return includeWeekends;
	}

	public void setIncludeWeekends(boolean includeWeekends) {
		this.includeWeekends = includeWeekends;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getStatusID() {
		return statusID;
	}

	public void setStatusID(int statusID) {
		this.statusID = statusID;
	}

	public double getWage() {
		return wage;
	}

	public void setWage(double wage) {
		this.wage = wage;
	}

}