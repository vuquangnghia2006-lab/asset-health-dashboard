package com.assethealth.repository;

import com.assethealth.entity.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CircuitBreakerRepository extends JpaRepository<CircuitBreaker, Long> {
    @Query("SELECT COUNT(cb) FROM CircuitBreaker cb")
    long countAll();
}
