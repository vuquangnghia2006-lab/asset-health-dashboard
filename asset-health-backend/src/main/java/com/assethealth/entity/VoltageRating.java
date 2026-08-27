package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "voltage_rating")
public class VoltageRating {
    @Id
    private Long id;
    private Long transformerId;
    private String winding;
    private Double voltageLL;
    private String voltageLLUnit;
    private Double voltageLN;
    private String voltageLNUnit;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTransformerId() { return transformerId; }
    public void setTransformerId(Long transformerId) { this.transformerId = transformerId; }
    public String getWinding() { return winding; }
    public void setWinding(String winding) { this.winding = winding; }
    public Double getVoltageLL() { return voltageLL; }
    public void setVoltageLL(Double voltageLL) { this.voltageLL = voltageLL; }
    public String getVoltageLLUnit() { return voltageLLUnit; }
    public void setVoltageLLUnit(String voltageLLUnit) { this.voltageLLUnit = voltageLLUnit; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
