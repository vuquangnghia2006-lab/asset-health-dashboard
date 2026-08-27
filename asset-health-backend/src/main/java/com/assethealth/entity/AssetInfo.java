package com.assethealth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_info")
public class AssetInfo {
    @Id
    private Long id;
    private Long ownerId;
    private String ownerType;
    private String serialNo;
    private Long manufacturerId;
    private Integer manufacturingYear;
    private Long countryOfOrigin;
    private String appartusId;
    private String description;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private String createdUser;
    private String updatedUser;
    private Long status;
    private String assetName;
    private String manufacturerType;
    private LocalDateTime operationDate;
    private String numberOfPhase;
    private String phase;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    public Long getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(Long manufacturerId) { this.manufacturerId = manufacturerId; }
    public Integer getManufacturingYear() { return manufacturingYear; }
    public void setManufacturingYear(Integer manufacturingYear) { this.manufacturingYear = manufacturingYear; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public String getManufacturerType() { return manufacturerType; }
    public void setManufacturerType(String manufacturerType) { this.manufacturerType = manufacturerType; }
    public LocalDateTime getOperationDate() { return operationDate; }
    public void setOperationDate(LocalDateTime operationDate) { this.operationDate = operationDate; }
    public String getNumberOfPhase() { return numberOfPhase; }
    public void setNumberOfPhase(String numberOfPhase) { this.numberOfPhase = numberOfPhase; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
