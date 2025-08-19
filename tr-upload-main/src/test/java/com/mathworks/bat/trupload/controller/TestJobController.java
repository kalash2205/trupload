package com.mathworks.bat.trupload.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.mathworks.bat.trupload.exception.TRWSException;
import com.mathworks.bat.trupload.service.JobService;
import com.mathworks.bat.trupload.view.JobParams;

public class TestJobController {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private JobParams validJobParams;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validJobParams = new JobParams("test");
    }

    @Test
    public void testAddJobValidInput() throws TRWSException {
        Integer expectedJobId = 2222;
        when(jobService.addJob(any(Integer.class), any(JobParams.class)))
                .thenReturn(expectedJobId);

        ResponseEntity<Integer> response = jobController.addJob(1111, validJobParams);

        assertEquals(expectedJobId, response.getBody());
    }
}