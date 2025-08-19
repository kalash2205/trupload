package com.mathworks.bat.trupload.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Sequence")
public class SequenceEntity {

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Id
    @Column(name = "name")
    private String name;

    // Default constructor
    public SequenceEntity() {
    }

    public SequenceEntity(Integer sequence, String name) {
        super();
        this.sequence = sequence;
        this.name = name;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public SequenceEntity setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getSequence() {
        return sequence;
    }

    public SequenceEntity setSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }
}