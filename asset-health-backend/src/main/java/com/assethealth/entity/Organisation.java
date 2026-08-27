package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "organisation")
public class Organisation {
    @Id
    private Long id;
    private String name;
    private Long ownerId;
    private String taxCode;
    private String description;
    private Long status;
    private String shortName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
