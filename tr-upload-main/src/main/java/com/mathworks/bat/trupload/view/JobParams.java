package com.mathworks.bat.trupload.view;

public class JobParams {

    private String branch;

    public JobParams(String branch) {
        this.branch = branch;
    }

    public JobParams()
    {

    }

    public String getBranch() {
        return branch;
    }

    public JobParams setBranch(String branch) {
        this.branch = branch;
        return this;
    }
}
