package com.assethealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "address_detail")
public class AddressDetail {
    @Id
    private Long id;
    private String fullAddress;
    private String street;
    private String wardCommune;
    private String districtTown;
    private String city;
    private String stateProvince;
    private Long countryId;
    private Long status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getWardCommune() { return wardCommune; }
    public void setWardCommune(String wardCommune) { this.wardCommune = wardCommune; }
    public String getDistrictTown() { return districtTown; }
    public void setDistrictTown(String districtTown) { this.districtTown = districtTown; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStateProvince() { return stateProvince; }
    public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
    public Long getCountryId() { return countryId; }
    public void setCountryId(Long countryId) { this.countryId = countryId; }
    public Long getStatus() { return status; }
    public void setStatus(Long status) { this.status = status; }
}
