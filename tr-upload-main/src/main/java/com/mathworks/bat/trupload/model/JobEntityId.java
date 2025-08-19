package com.mathworks.bat.trupload.model;

import java.io.Serializable;

public class JobEntityId implements Serializable  {

    private static final long serialVersionUID = 1L;

    private Integer jobNumber;

    private Integer clusterId;

    public Integer getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(Integer jobNumber) {
        this.jobNumber = jobNumber;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }
}
