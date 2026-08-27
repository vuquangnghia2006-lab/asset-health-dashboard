package com.assethealth.repository;

import com.assethealth.entity.SurgeArrester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SurgeArresterRepository extends JpaRepository<SurgeArrester, Long> {
    @Query("SELECT COUNT(sa) FROM SurgeArrester sa")
    long countAll();
}
