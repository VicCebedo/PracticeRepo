package com.cebedo.pmsys.bean;

import java.io.Serializable;

public class EstimateComputationShape implements Serializable {

    private static final long serialVersionUID = -8565430166993517307L;

    private double area = 0;
    private double volume = 0;
    private double originalArea = 0;
    private double originalVolume = 0;
    private double footingLength = 0;
    private double footingWidth = 0;
    private double footingHeight = 0;

    // Metal reinforcement.
    private double footingBarLength = 0;
    private double footingNumberOfBars = 0;
    private double footingBarIntersectionPerFooting = 0;

    private double postColumnLateralTiesSpacing = 0;
    private double postColumnLateralTiesLength = 0;
    private double postColumnMainReinforcementsPerCol = 0;

    public double getArea() {
	return area;
    }

    public void setArea(double area) {
	this.area = area;
    }

    public double getVolume() {
	return volume;
    }

    public void setVolume(double volume) {
	this.volume = volume;
    }

    public double getFootingLength() {
	return footingLength;
    }

    public void setFootingLength(double footingLength) {
	this.footingLength = footingLength;
    }

    public double getOriginalArea() {
	return originalArea;
    }

    public void setOriginalArea(double originalArea) {
	this.originalArea = originalArea;
    }

    public double getOriginalVolume() {
	return originalVolume;
    }

    public void setOriginalVolume(double originalVolume) {
	this.originalVolume = originalVolume;
    }

    public double getFootingWidth() {
	return footingWidth;
    }

    public void setFootingWidth(double footingWidth) {
	this.footingWidth = footingWidth;
    }

    public double getFootingHeight() {
	return footingHeight;
    }

    public void setFootingHeight(double footingHeight) {
	this.footingHeight = footingHeight;
    }

    public double getFootingBarLength() {
	return footingBarLength;
    }

    public void setFootingBarLength(double footingBarLength) {
	this.footingBarLength = footingBarLength;
    }

    public double getFootingNumberOfBars() {
	return footingNumberOfBars;
    }

    public void setFootingNumberOfBars(double footingNumberOfBars) {
	this.footingNumberOfBars = footingNumberOfBars;
    }

    public double getFootingBarIntersectionPerFooting() {
	return footingBarIntersectionPerFooting;
    }

    public void setFootingBarIntersectionPerFooting(double footingBarIntersectionPerFooting) {
	this.footingBarIntersectionPerFooting = footingBarIntersectionPerFooting;
    }

    public double getPostColumnLateralTiesSpacing() {
	return postColumnLateralTiesSpacing;
    }

    public void setPostColumnLateralTiesSpacing(double postColumnLateralTiesSpacing) {
	this.postColumnLateralTiesSpacing = postColumnLateralTiesSpacing;
    }

    public double getPostColumnLateralTiesLength() {
	return postColumnLateralTiesLength;
    }

    public void setPostColumnLateralTiesLength(double postColumnLateralTiesLength) {
	this.postColumnLateralTiesLength = postColumnLateralTiesLength;
    }

    public double getPostColumnMainReinforcementsPerCol() {
	return postColumnMainReinforcementsPerCol;
    }

    public void setPostColumnMainReinforcementsPerCol(double postColumnMainReinforcementsPerCol) {
	this.postColumnMainReinforcementsPerCol = postColumnMainReinforcementsPerCol;
    }
}
