package com.assethealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transformer")
public class Transformer {
    @Id
    private Long id;
    private String assetType;
    private Long assetInfoId;
    private String phases;
    private String vectorGroup;
    private String vectorGroupPrim;
    private String vectorGroupSec;
    private Integer vectorGroupSecVal;
    private String vectorGroupTertiary;
    private Integer vectorGroupTertiaryVal;
    private Double ratedFrequency;
    private String ratedFrequencyUnit;
    private Double basePower;
    private String basePowerUnit;
    private Double baseVoltage;
    private String baseVoltageUnit;
    private LocalDateTime operationDate;
    private Long status;
    private String substationOwnerType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    public Long getAssetInfoId() { return assetInfoId; }
    public void setAssetInfoId(Long assetInfoId) { this.assetInfoId = assetInfoId; }
    public String getPhases() { return phases; }
    public void setPhases(String phases) { this.phases = phases; }
    public String getVectorGroup() { return vectorGroup; }
    public void setVectorGroup(String vectorGroup) { this.vectorGroup = vectorGroup; }
    public Double getRatedFrequency() { return ratedFrequency; }
    public void setRatedFrequency(Double ratedFrequency) { this.ratedFrequency = ratedFrequency; }
    public Double getBasePower() { return basePower; }
    public void setBasePower(Double basePower) { this.basePower = basePower; }
    public String getBasePowerUnit() { return basePowerUnit; }
    public void setBasePowerUnit(String basePowerUnit) { this.basePowerUnit = basePowerUnit; }
    public Double getBaseVoltage() { return baseVoltage; }
    public void setBaseVoltage(Double baseVoltage) { this.baseVoltage = baseVoltage; }
    public String getBaseVoltageUnit() { return baseVoltageUnit; }
    public void setBaseVoltageUnit(String baseVoltageUnit) { this.baseVoltageUnit = baseVoltageUnit; }
    public LocalDateTime getOperationDate() { return operationDate; }
    public void setOperationDate(LocalDateTime operationDate) { this.operationDate = operationDate; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
    public String getSubstationOwnerType() { return substationOwnerType; }
    public void setSubstationOwnerType(String substationOwnerType) { this.substationOwnerType = substationOwnerType; }
}
