package com.mathworks.bat.trupload.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mathworks.bat.trupload.exception.TRWSException;
import com.mathworks.bat.trupload.service.JobService;
import com.mathworks.bat.trupload.view.JobParams;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "TR Upload", description = "Everything about TRUpload endpoints")
@RestController
@RequestMapping("/job")
public class JobController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final JobService jobService;

    public JobController(final JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "Find or create a new job and return jobId", description = "Find jobId for a job number from TRDB or create a new job with the job details fetched from JMD and return the jobId.")
    @PostMapping(value = "/{jobNum}", consumes = JSON, produces = JSON)
    @HystrixCommand(threadPoolKey = "addJob")
    public ResponseEntity<Integer> addJob(
            @PathVariable (name ="jobNum") Integer jobNum,
            @RequestBody JobParams newJobParams)
            throws TRWSException {

        return ResponseEntity.ok().body(jobService.addJob(jobNum, newJobParams));
    }
}

