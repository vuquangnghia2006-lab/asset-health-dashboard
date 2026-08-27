package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "circuit_breaker")
public class CircuitBreaker {
    @Id
    private Long id;
    private String assetType;
    private Long assetInfoId;
    private Integer numberOfPhases;
    private String interruptingMedium;
    private String tankType;
    private Long status;
    private String position;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    public Long getAssetInfoId() { return assetInfoId; }
    public void setAssetInfoId(Long assetInfoId) { this.assetInfoId = assetInfoId; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
