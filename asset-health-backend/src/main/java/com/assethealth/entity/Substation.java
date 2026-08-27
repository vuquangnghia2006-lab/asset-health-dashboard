package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "substation")
public class Substation {
    @Id
    private Long id;
    private String name;
    private Long ownerId;
    private String substationType;
    private String generationType;
    private String industryType;
    private Long addressDetailId;
    private Long contactId;
    private String description;
    private Long status;
    private String aliasName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getSubstationType() { return substationType; }
    public void setSubstationType(String substationType) { this.substationType = substationType; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }
}
