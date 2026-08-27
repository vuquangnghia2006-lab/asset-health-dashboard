package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "voltage_transformer")
public class VoltageTransformer {
    @Id
    private Long id;
    private String assetType;
    private Long assetInfoId;
    private String standard;
    private Double ratedVoltage;
    private String phase;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    public Long getAssetInfoId() { return assetInfoId; }
    public void setAssetInfoId(Long assetInfoId) { this.assetInfoId = assetInfoId; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
