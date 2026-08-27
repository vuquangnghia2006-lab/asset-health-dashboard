package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "power_rating")
public class PowerRating {
    @Id
    private Long id;
    private Long transformerId;
    private Double ratedPower;
    private String ratedPowerUnit;
    private String coolingClass;
    private Double currentRatingPrim;
    private String currentRatingPrimUnit;
    private Double currentRatingSec;
    private String currentRatingSecUnit;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTransformerId() { return transformerId; }
    public void setTransformerId(Long transformerId) { this.transformerId = transformerId; }
    public Double getRatedPower() { return ratedPower; }
    public void setRatedPower(Double ratedPower) { this.ratedPower = ratedPower; }
    public String getRatedPowerUnit() { return ratedPowerUnit; }
    public void setRatedPowerUnit(String ratedPowerUnit) { this.ratedPowerUnit = ratedPowerUnit; }
    public String getCoolingClass() { return coolingClass; }
    public void setCoolingClass(String coolingClass) { this.coolingClass = coolingClass; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
