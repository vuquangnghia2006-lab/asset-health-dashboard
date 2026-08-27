package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "surge_arrester")
public class SurgeArrester {
    @Id
    private Long id;
    private String assetType;
    private Long assetInfoId;
    private String position;
    private Integer unitCount;
    private String winding;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
