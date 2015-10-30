package com.cebedo.pmsys.enums;

public enum TableMRCHBVertical {

    CM_40(40.0, UnitCommonLength.CENTIMETER, 2.93),

    CM_60(60.0, UnitCommonLength.CENTIMETER, 2.13),

    CM_80(80.0, UnitCommonLength.CENTIMETER, 1.60);

    public static final TableMRCHBVertical SAFEST = CM_40;

    private double spacing;
    private UnitCommonLength unit;
    private double perSqMeter;

    TableMRCHBVertical(double space, UnitCommonLength unt, double perSqM) {
	this.spacing = space;
	this.unit = unt;
	this.perSqMeter = perSqM;
    }

    public double spacing() {
	return this.spacing;
    }

    public UnitCommonLength unit() {
	return this.unit;
    }

    public String label() {
	return this.spacing + " " + this.unit.getSymbol();
    }

    public double getPerSqMeter() {
	return perSqMeter;
    }

    public void setPerSqMeter(double perSqMeter) {
	this.perSqMeter = perSqMeter;
    }

}
