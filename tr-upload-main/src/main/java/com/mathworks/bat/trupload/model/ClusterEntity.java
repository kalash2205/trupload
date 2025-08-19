package com.mathworks.bat.trupload.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Cluster")
public class ClusterEntity  {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "cluster", nullable = false)
    private String cluster;

    @Column(name = "comment")
    private String comment;

    // Default constructor
    public ClusterEntity() {
    }

    // Constructor with fields
    public ClusterEntity(String cluster, String comment) {
        this.cluster = cluster;
        this.comment = comment;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public ClusterEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getCluster() {
        return cluster;
    }

    public ClusterEntity setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public ClusterEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }
}
