package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "disconnector")
public class Disconnector {
    @Id
    private Long id;
    private String assetType;
    private Long assetInfoId;
    private Double ratedVoltage;
    private String ratedVoltageUnit;
    private Double ratedCurrent;
    private String ratedCurrentUnit;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getRatedVoltage() { return ratedVoltage; }
    public void setRatedVoltage(Double ratedVoltage) { this.ratedVoltage = ratedVoltage; }
    public Double getRatedCurrent() { return ratedCurrent; }
    public void setRatedCurrent(Double ratedCurrent) { this.ratedCurrent = ratedCurrent; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
