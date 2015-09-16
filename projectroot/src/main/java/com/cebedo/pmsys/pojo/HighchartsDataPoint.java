package com.cebedo.pmsys.pojo;

import org.apache.commons.lang.StringUtils;

public class HighchartsDataPoint {

    private String name;
    private long x;
    private double y;
    private String color;

    public String toString() {
	return String.format("%s (%s, %s)", getName(), getX(), getY());
    }

    /**
     * Typically used in Pie/Donut charts.
     * 
     * @param n
     * @param y
     * @param color
     */
    public HighchartsDataPoint(String n, double y, String color) {
	setName(n);
	setY(y);
	setColor(color);
    }

    public HighchartsDataPoint() {
	;
    }

    /**
     * Typically used in charts with date and time as x-axis.
     * 
     * @param name2
     * @param time
     * @param yValue
     * @param color
     */
    public HighchartsDataPoint(String name2, long time, double yValue, String color) {
	setName(name2);
	setX(time);
	setY(yValue);
	setColor(color);
    }

    public HighchartsDataPoint(String name2, long time, double yValue) {
	setName(name2);
	setX(time);
	setY(yValue);
    }

    public HighchartsDataPoint(String n, double y) {
	setName(n);
	setY(y);
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = StringUtils.trim(name);
    }

    public double getY() {
	return y;
    }

    public void setY(double y) {
	this.y = y;
    }

    public String getColor() {
	return color;
    }

    public void setColor(String color) {
	this.color = StringUtils.trim(color);
    }

    public long getX() {
	return x;
    }

    public void setX(long x) {
	this.x = x;
    }

}
