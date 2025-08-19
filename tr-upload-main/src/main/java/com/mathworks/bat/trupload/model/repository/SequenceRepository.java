package com.mathworks.bat.trupload.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mathworks.bat.trupload.model.SequenceEntity;

@Repository
public interface SequenceRepository extends JpaRepository<SequenceEntity, Integer> {

    Optional<SequenceEntity> findByName(String idName);

    @Modifying
    @Query(value = "CALL inc_sequence(:seq_name);", nativeQuery = true)
    void incrementIdQuery(@Param("seq_name") String sequenceName);
}

