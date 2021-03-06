package com.cebedo.pmsys.enums;

public enum StatusPayroll {

    NEW(0, "New", "btn-info"),

    PENDING(1, "Pending", "btn-primary"),

    APPROVED(2, "Approved", "btn-warning"),

    RELEASED(3, "Released", "btn-success");

    String label;
    int id;
    String css;

    StatusPayroll(int idn) {
	this.id = idn;
    }

    StatusPayroll(int idn, String lbl, String cssClass) {
	this.label = lbl;
	this.id = idn;
	this.css = cssClass;
    }

    public static StatusPayroll of(int idn) {
	if (idn == NEW.id()) {
	    return NEW;

	} else if (idn == PENDING.id()) {
	    return PENDING;

	} else if (idn == RELEASED.id()) {
	    return RELEASED;

	} else if (idn == APPROVED.id()) {
	    return APPROVED;

	}
	return NEW;
    }

    public String value() {
	return this.label;
    }

    public String css() {
	return this.css;
    }

    public String label() {
	return this.label;
    }

    public int id() {
	return this.id;
    }

}