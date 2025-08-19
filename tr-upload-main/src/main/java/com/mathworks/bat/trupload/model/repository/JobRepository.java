package com.mathworks.bat.trupload.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mathworks.bat.trupload.model.JobEntity;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Integer> {

    Optional<JobEntity> findByJobNumber(Integer jobNumber);

    @Modifying
    @Query(value = "SET @client_version = :clientVersion", nativeQuery = true)
    void setClientVersion(@Param("clientVersion") Integer clientVersion);
    
    List<JobEntity> findByJobStatusNotIn(List<String> statuses);
}
