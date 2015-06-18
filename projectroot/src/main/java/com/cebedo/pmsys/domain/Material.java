package com.cebedo.pmsys.domain;

import java.util.Map;
import java.util.UUID;

import com.cebedo.pmsys.constants.RedisKeyRegistry;
import com.cebedo.pmsys.model.Company;
import com.cebedo.pmsys.model.Project;
import com.cebedo.pmsys.utils.NumberFormatUtils;

public class Material implements IDomainObject {

    private static final long serialVersionUID = 2300406369596684113L;
    /**
     * Keys: company:%s:project:%s:delivery:%s:material:%s
     */
    private Company company;
    private Project project;
    private Delivery delivery;
    private UUID uuid;

    /**
     * Detail fields.
     */
    private String name;
    private String remarks;

    /**
     * Specifications.
     */
    private double quantity;
    private double used;
    private double available;
    private String unit;

    /**
     * Cost per unit.
     */
    private double costPerUnitMaterial;
    private double costPerUnitLabor;
    private double costPerUnitEquipment;
    private double costPerUnitTotal;

    /**
     * Total cost per unit (costPerUnit * quantity).
     */
    private double totalCostPerUnitMaterial;
    private double totalCostPerUnitLabor;
    private double totalCostPerUnitEquipment;
    private double totalCost;

    /**
     * Extension map.
     */
    private Map<String, Object> extMap;

    public Material() {
	;
    }

    public Material(Delivery delivery2) {
	setCompany(delivery2.getCompany());
	setProject(delivery2.getProject());
	setDelivery(delivery2);
    }

    @Override
    public Map<String, Object> getExtMap() {
	return extMap;
    }

    @Override
    public void setExtMap(Map<String, Object> extMap) {
	this.extMap = extMap;
    }

    @Override
    public String getKey() {
	// company:%s:project:%s:delivery:%s:material:%s
	return String.format(RedisKeyRegistry.KEY_MATERIAL,
		this.company.getId(), this.project.getId(),
		this.delivery.getUuid(), this.uuid);
    }

    public Company getCompany() {
	return company;
    }

    public void setCompany(Company company) {
	this.company = company;
    }

    public Project getProject() {
	return project;
    }

    public void setProject(Project project) {
	this.project = project;
    }

    public Delivery getDelivery() {
	return delivery;
    }

    public void setDelivery(Delivery delivery) {
	this.delivery = delivery;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getRemarks() {
	return remarks;
    }

    public void setRemarks(String remarks) {
	this.remarks = remarks;
    }

    public double getQuantity() {
	return quantity;
    }

    public void setQuantity(double quantity) {
	this.quantity = quantity;
    }

    public String getUnit() {
	return unit;
    }

    public void setUnit(String unit) {
	this.unit = unit;
    }

    public String getCostPerUnitMaterialAsString() {
	return NumberFormatUtils.getCurrencyFormatter().format(
		costPerUnitMaterial);
    }

    public double getCostPerUnitMaterial() {
	return costPerUnitMaterial;
    }

    public void setCostPerUnitMaterial(double costPerUnitMaterial) {
	this.costPerUnitMaterial = costPerUnitMaterial;
    }

    public double getCostPerUnitLabor() {
	return costPerUnitLabor;
    }

    public void setCostPerUnitLabor(double costPerUnitLabor) {
	this.costPerUnitLabor = costPerUnitLabor;
    }

    public double getCostPerUnitEquipment() {
	return costPerUnitEquipment;
    }

    public void setCostPerUnitEquipment(double costPerUnitEquipment) {
	this.costPerUnitEquipment = costPerUnitEquipment;
    }

    public double getCostPerUnitTotal() {
	return costPerUnitTotal;
    }

    public void setCostPerUnitTotal(double costPerUnitTotal) {
	this.costPerUnitTotal = costPerUnitTotal;
    }

    public String getTotalCostPerUnitMaterialAsString() {
	return NumberFormatUtils.getCurrencyFormatter().format(
		totalCostPerUnitMaterial);
    }

    public double getTotalCostPerUnitMaterial() {
	return totalCostPerUnitMaterial;
    }

    public void setTotalCostPerUnitMaterial(double totalCostPerUnitMaterial) {
	this.totalCostPerUnitMaterial = totalCostPerUnitMaterial;
    }

    public double getTotalCostPerUnitLabor() {
	return totalCostPerUnitLabor;
    }

    public void setTotalCostPerUnitLabor(double totalCostPerUnitLabor) {
	this.totalCostPerUnitLabor = totalCostPerUnitLabor;
    }

    public double getTotalCostPerUnitEquipment() {
	return totalCostPerUnitEquipment;
    }

    public void setTotalCostPerUnitEquipment(double totalCostPerUnitEquipment) {
	this.totalCostPerUnitEquipment = totalCostPerUnitEquipment;
    }

    public double getTotalCost() {
	return totalCost;
    }

    public void setTotalCost(double totalCost) {
	this.totalCost = totalCost;
    }

    public UUID getUuid() {
	return uuid;
    }

    public void setUuid(UUID uuid) {
	this.uuid = uuid;
    }

    public double getUsed() {
	return used;
    }

    public void setUsed(double used) {
	this.used = used;
    }

    public double getAvailable() {
	return available;
    }

    public void setAvailable(double available) {
	this.available = available;
    }

    public static String constructPattern(Delivery delivery2) {
	Company company = delivery2.getCompany();
	Project project = delivery2.getProject();
	return String.format(RedisKeyRegistry.KEY_MATERIAL, company.getId(),
		project.getId(), delivery2.getUuid(), "*");
    }

    public static String constructPattern(Project project) {
	Company company = project.getCompany();
	return String.format(RedisKeyRegistry.KEY_MATERIAL, company.getId(),
		project.getId(), "*", "*");
    }

}
