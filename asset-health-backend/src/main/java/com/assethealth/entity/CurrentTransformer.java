package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "current_transformer")
public class CurrentTransformer {
    @Id
    private Long id;
    private String assetType;
    private Long assetInfoId;
    private String standard;
    private Integer primaryWinding;
    private Double systemVoltage;
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
    public Integer getPrimaryWinding() { return primaryWinding; }
    public void setPrimaryWinding(Integer primaryWinding) { this.primaryWinding = primaryWinding; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
