package com.mathworks.bat.trupload.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mathworks.bat.trupload.model.ClusterEntity;

@Repository
public interface ClusterRepository extends JpaRepository<ClusterEntity, Integer> {

    Optional<ClusterEntity> findByCluster(String cluster);
}