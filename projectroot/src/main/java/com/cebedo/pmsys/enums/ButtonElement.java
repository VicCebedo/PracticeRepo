package com.cebedo.pmsys.enums;

public enum ButtonElement {
    INFO("btn-info", "Info", "#fff", "#5bc0de", "#46b8da"), PRIMARY(
	    "btn-primary", "Primary", "#fff", "#337ab7", "#2e6da4"), SUCCESS(
	    "btn-success", "Success", "#fff", "#5cb85c", "#4cae4c"), WARNING(
	    "btn-warning", "Warning", "#fff", "#f0ad4e", "#eea236"), DANGER(
	    "btn-danger", "Danger", "#fff", "#d9534f", "#d43f3a"), DEFAULT(
	    "btn-default", "Default", "#333", "#fff", "#ccc"), DEFAULT_HOVER(
	    "btn-default:hover", "Default Hover", "#333", "#e6e6e6", "#adadad");

    String className;
    String label;
    String color;
    String backgroundColor;
    String borderColor;

    ButtonElement(String cName, String label, String color,
	    String backgroundColor, String borderColor) {
	this.className = cName;
	this.label = label;
	this.color = color;
	this.backgroundColor = backgroundColor;
	this.borderColor = borderColor;
    }

    public static ButtonElement of(String className) {
	if (className.equals(INFO.className())) {
	    return INFO;
	}

	else if (className.equals(PRIMARY.className())) {
	    return PRIMARY;
	}

	else if (className.equals(SUCCESS.className())) {
	    return SUCCESS;
	}

	else if (className.equals(WARNING.className())) {
	    return WARNING;
	}
	return INFO;
    }

    public String className() {
	return this.className;
    }

    public String label() {
	return this.label;
    }

    public String color() {
	return this.color;
    }

    public String backgroundColor() {
	return this.backgroundColor;
    }

    public String borderColor() {
	return this.borderColor;
    }

}