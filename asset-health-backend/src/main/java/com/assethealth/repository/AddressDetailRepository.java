package com.assethealth.repository;

import com.assethealth.entity.AddressDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressDetailRepository extends JpaRepository<AddressDetail, Long> {

    @Query(value = "SELECT DISTINCT city FROM address_detail WHERE city IS NOT NULL AND TRIM(city) <> '' ORDER BY city", nativeQuery = true)
    List<String> findDistinctCities();

    @Query(value = "SELECT DISTINCT ward_commune FROM address_detail WHERE ward_commune IS NOT NULL AND TRIM(ward_commune) <> '' AND (:city IS NULL OR city = :city) ORDER BY ward_commune", nativeQuery = true)
    List<String> findDistinctWardsByCity(@Param("city") String city);
}
