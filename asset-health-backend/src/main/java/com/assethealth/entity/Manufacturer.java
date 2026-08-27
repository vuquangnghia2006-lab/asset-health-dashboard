package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "manufacturer")
public class Manufacturer {
    @Id
    private Long id;
    private String name;
    private String type;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
