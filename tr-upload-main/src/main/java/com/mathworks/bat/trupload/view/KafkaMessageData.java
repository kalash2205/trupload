package com.mathworks.bat.trupload.view;

import java.util.List;

public class KafkaMessageData {
    private Integer jobId;
    private String state;
    private List<String> superStates;

    // Getters and setters
    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getSuperStates() {
        return superStates;
    }

    public void setSuperStates(List<String> superStates) {
        this.superStates = superStates;
    }
}
