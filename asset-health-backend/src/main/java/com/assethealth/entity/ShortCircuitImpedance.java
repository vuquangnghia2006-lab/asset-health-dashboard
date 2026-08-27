package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "short_circuit_impedance")
public class ShortCircuitImpedance {
    @Id
    private Long id;
    private Long transformerId;
    private String type;
    private Double impedance;
    private String impedanceUnit;
    private Double basePower;
    private String basePowerUnit;
    private Double baseVoltage;
    private String baseVoltageUnit;
    private Double loadLoss;
    private String loadLossUnit;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTransformerId() { return transformerId; }
    public void setTransformerId(Long transformerId) { this.transformerId = transformerId; }
    public Double getImpedance() { return impedance; }
    public void setImpedance(Double impedance) { this.impedance = impedance; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
