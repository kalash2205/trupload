package com.mathworks.bat.trupload.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@IdClass(JobEntityId.class)
@Table(name = "Job")
public class JobEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Id
    @Column(name = "cluster_id")
    private Integer clusterId;

    @Column(name = "branch")
    private String branch;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "duration", nullable = false)
    private Long duration;

    @Column(name = "number_files", nullable = false)
    private Integer numberFiles = 0;

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp startDate;

    @Column(name = "last_modified")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastModified;

    @Id
    @Column(name = "job_number")
    private Integer jobNumber;

    @Column(name = "job_status")
    private String jobStatus;

    @Column(name = "submitter")
    private String submitter;

    @Column(name = "gecks")
    private String gecks;

    @Column(name = "comment")
    private String comment;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public JobEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public JobEntity setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public String getBranch() {
        return branch;
    }

    public JobEntity setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public String getJobType() {
        return jobType;
    }

    public JobEntity setJobType(String jobType) {
        this.jobType = jobType;
        return this;
    }

    public Long getDuration() {
        return duration;
    }

    public JobEntity setDuration(Long duration) {
        this.duration = duration;
        return this;
    }

    public Integer getNumberFiles() {
        return numberFiles;
    }

    public JobEntity setNumberFiles(Integer numberFiles) {
        this.numberFiles = numberFiles;
        return this;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public JobEntity setStartDate(Timestamp startDate) {
        this.startDate = startDate;
        return this;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public JobEntity setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public Integer getJobNumber() {
        return jobNumber;
    }

    public JobEntity setJobNumber(Integer jobNumber) {
        this.jobNumber = jobNumber;
        return this;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public JobEntity setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public String getSubmitter() {
        return submitter;
    }

    public JobEntity setSubmitter(String submitter) {
        this.submitter = submitter;
        return this;
    }

    public String getGecks() {
        return gecks;
    }

    public JobEntity setGecks(String gecks) {
        this.gecks = gecks;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public JobEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public JobEntity setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
        return this;
    }
}