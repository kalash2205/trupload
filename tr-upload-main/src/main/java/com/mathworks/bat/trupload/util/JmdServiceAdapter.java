package com.mathworks.bat.trupload.util;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mathworks.bat.jmd.api.common.ChangeContents.ContentType;
import com.mathworks.bat.jmd.api.job.IJobService;
import com.mathworks.bat.jmd.api.job.Job;
import com.mathworks.bat.jmd.client.JmdServicesFactory;
import com.mathworks.bat.jmd.client.ServiceConfiguration;

@Service
public class JmdServiceAdapter {

    @Autowired
    private BrcReader brc;

    private IJobService jobService;

    /**
     * Method to initialize things before running application. Will be invoked when spring container
     * is initialized.
    */
    @PostConstruct
    private void setUp() {
        ServiceConfiguration serviceConfiguration = ServiceConfiguration.getDefault();
        serviceConfiguration.setJmdWebAppUrl(brc.getString(brc.JMD_WEBAPP_URL));

        jobService = JmdServicesFactory.getJobService(serviceConfiguration);
    }

    public Job getJob (int jobId) throws Exception  {
        return jobService.getJob(jobId);
    }

    public String getDirectGecks(Integer jobNumber) throws Exception {
        return jobService.getContents(jobNumber, false, -1, Collections.singletonList(ContentType.GECKS), null)
                         .get(0)
                         .getDirect()
                         .toString();
    }
}