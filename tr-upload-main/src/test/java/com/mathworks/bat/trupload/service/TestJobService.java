package com.mathworks.bat.trupload.service;

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.reflect.Whitebox;

import com.mathworks.bat.trupload.exception.TRWSException;
import com.mathworks.bat.trupload.model.ClusterEntity;
import com.mathworks.bat.trupload.model.JobEntity;
import com.mathworks.bat.trupload.model.SequenceEntity;
import com.mathworks.bat.trupload.model.repository.ClusterRepository;
import com.mathworks.bat.trupload.model.repository.JobRepository;
import com.mathworks.bat.trupload.model.repository.SequenceRepository;
import com.mathworks.bat.trupload.util.JmdServiceAdapter;
import com.mathworks.bat.trupload.view.JobParams;

public class TestJobService {

    @Mock
    private ClusterRepository clusterRepository;

    @Mock
    private SequenceRepository sequenceRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JmdServiceAdapter jmdServiceAdapter;

    @Spy
    @InjectMocks
    private JobService jobService;

    private ClusterEntity clusterEntity;
    private SequenceEntity sequenceEntity;
    private JobEntity jobEntity;
    private JobParams validJobParams;
    private com.mathworks.bat.jmd.api.job.Job jmdJob;

    @BeforeEach
    public void setUp() {
        
        Logger.getLogger("com.mathworks.bat.trupload.service.JobService").setLevel(Level.WARN);
        MockitoAnnotations.openMocks(this);
        clusterEntity = new ClusterEntity();
        sequenceEntity = new SequenceEntity();
        sequenceEntity.setSequence(1111);
        jobEntity = new JobEntity();
        validJobParams = new JobParams("test");
        jmdJob = mock(com.mathworks.bat.jmd.api.job.Job.class);
    }

    @Test
    public void testProcessMessage_withValidData() throws TRWSException {
        String jsonData = "{\"jobId\": 2222, \"state\": \"ACCEPTED\"}";
        
        Set<Integer> trackedJobs = ConcurrentHashMap.newKeySet();
        trackedJobs.add(2222);
        
        Whitebox.setInternalState(jobService, "trackedJobs", trackedJobs);
        doNothing().when(jobService).updateJobEntry(anyInt(), anyString());

        jobService.processMessage(jsonData);

        verify(jobService).updateJobEntry(2222, "ACCEPTED");
    }

    @Test
    public void testProcessMessage_throwsTRWSException() throws TRWSException {
        String jsonData = "{\"jobId\": 2222, \"state\": \"ACCEPTED\"}";
        
        Set<Integer> trackedJobs = ConcurrentHashMap.newKeySet();
        trackedJobs.add(2222);
        
        Whitebox.setInternalState(jobService, "trackedJobs", trackedJobs);
        doThrow(new TRWSException("Test Exception")).when(jobService).updateJobEntry(anyInt(), anyString());

        TRWSException thrown = assertThrows(
            TRWSException.class,
            () -> jobService.processMessage(jsonData),
            "Expected processMessage to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Test Exception"));
    }

    @Test
    public void testAddClusterClusterExists() throws TRWSException {
        String clusterName = "testCluster";
        when(clusterRepository.findByCluster(clusterName)).thenReturn(Optional.of(clusterEntity));

        ClusterEntity result = jobService.addCluster(clusterName);

        assertSame(clusterEntity, result);
        verify(clusterRepository, never()).save(any(ClusterEntity.class));
        verify(sequenceRepository, never()).incrementIdQuery(anyString());
    }

    @Test
    public void testAddClusterClusterDoesNotExist() throws TRWSException {
        String clusterName = "newTestCluster";
        when(clusterRepository.findByCluster(clusterName)).thenReturn(Optional.empty());
        when(sequenceRepository.findByName("Cluster_id")).thenReturn(Optional.of(sequenceEntity));
        
        ClusterEntity savedCluster = new ClusterEntity();
        savedCluster.setCluster(clusterName);
        savedCluster.setId(1111);

        when(clusterRepository.save(any(ClusterEntity.class))).thenReturn(savedCluster);

        ClusterEntity result = jobService.addCluster(clusterName);
        
        verify(clusterRepository).save(argThat(cluster -> 
        cluster.getCluster().equals(clusterName) && cluster.getId() == 1111
        ));

        assertEquals(clusterName, result.getCluster());
        assertEquals(1111, result.getId());
        verify(sequenceRepository).incrementIdQuery("Cluster_id");
    }

    @Test
    public void testAddJobJobExists() throws TRWSException {
        Integer jobNumber = 2222;
        when(jobRepository.findByJobNumber(jobNumber)).thenReturn(Optional.of(jobEntity));

        Integer result = jobService.addJob(jobNumber, validJobParams);

        verify(jobRepository, never()).save(any(JobEntity.class));
        verify(sequenceRepository, never()).incrementIdQuery(anyString());
    }

    @Test
    public void testAddJobNewJob() throws Exception {
        Integer jobNumber = 2222;
        when(jobRepository.findByJobNumber(jobNumber)).thenReturn(Optional.empty());
        when(sequenceRepository.findByName("Job_id")).thenReturn(Optional.of(sequenceEntity));
        when(jmdServiceAdapter.getDirectGecks(jobNumber)).thenReturn("gecks");
        when(jmdServiceAdapter.getJob(jobNumber)).thenReturn(jmdJob);
        when(jmdJob.getCluster()).thenReturn("testCluster");
        when(jmdJob.getJobStatus()).thenReturn("Running");
        when(jmdJob.getJobType()).thenReturn("Acceptance");
        when(jmdJob.getCreator()).thenReturn("testSubmitter");
        when(jmdJob.getLastStatusTime()).thenReturn(Calendar.getInstance());
        when(jmdJob.getStartTime()).thenReturn(Calendar.getInstance());
        when(jobService.addCluster("testCluster")).thenReturn(clusterEntity);
        
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(jobService.fetchDirectGecks(jobEntity, jobNumber)).thenReturn(future);
        
        Integer result = jobService.addJob(jobNumber, validJobParams);
       
        verify(sequenceRepository).incrementIdQuery("Job_id");
    }
    
    @Test
    public void testAddJobWithNullBranchThrowsException() {
        Integer jobNumber = 2222;
        validJobParams.setBranch(null);

        TRWSException exception = assertThrows(TRWSException.class, () -> {
            jobService.addJob(jobNumber, validJobParams);
        });

        assertEquals("Job branch is missing in job parameters for job number " + jobNumber, exception.getMessage());
    }
    
    @Test
    public void testAddJobWithEmptyBranchThrowsException() {
        Integer jobNumber = 2222;
        validJobParams.setBranch("");

        TRWSException exception = assertThrows(TRWSException.class, () -> {
            jobService.addJob(jobNumber, validJobParams);
        });

        assertEquals("Job branch is missing in job parameters for job number " + jobNumber, exception.getMessage());
    }
    
    @Test
    public void testJobEntityNotFound() throws TRWSException {
        when(jobService.getJobEntity(anyInt())).thenReturn(null);

        jobService.updateJobEntry(1111, "ACCEPTED");

        verify(jobRepository, never()).save(any(JobEntity.class));
    }
    
    @Test
    public void testJobStatusAcceptedMapping() throws Exception {
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(1000);
        Calendar lastStatusTime = Calendar.getInstance();
        lastStatusTime.setTimeInMillis(5000);
        
        when(jobService.getJobEntity(anyInt())).thenReturn(jobEntity);
        when(jobService.getJmdJob(anyInt())).thenReturn(jmdJob);
        when(jmdJob.getLastStatusTime()).thenReturn(lastStatusTime);
        when(jmdJob.getStartTime()).thenReturn(startTime);
        when(jobService.fetchDirectGecks(any(JobEntity.class), anyInt()))
        .thenReturn(CompletableFuture.completedFuture(null));

        jobService.updateJobEntry(1111, "ACCEPTED");

        assertEquals("Accept", jobEntity.getJobStatus());
        verify(jobRepository, times(1)).save(jobEntity);
    }
    
    @Test
    public void testJobStatusRejectedMapping() throws Exception {
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(1000);
        Calendar lastStatusTime = Calendar.getInstance();
        lastStatusTime.setTimeInMillis(5000);
        
        when(jobService.getJobEntity(anyInt())).thenReturn(jobEntity);
        when(jobService.getJmdJob(anyInt())).thenReturn(jmdJob);
        when(jmdJob.getLastStatusTime()).thenReturn(lastStatusTime);
        when(jmdJob.getStartTime()).thenReturn(startTime);
        when(jobService.fetchDirectGecks(any(JobEntity.class), anyInt()))
        .thenReturn(CompletableFuture.completedFuture(null));

        jobService.updateJobEntry(1111, "REJECTED");

        assertEquals("Fail", jobEntity.getJobStatus());
        verify(jobRepository, times(1)).save(jobEntity);
    }
    
    @Test
    public void testDurationAndLastModifiedUpdate() throws Exception {
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(1000);
        Calendar lastStatusTime = Calendar.getInstance();
        lastStatusTime.setTimeInMillis(5000);
        
        when(jobService.getJobEntity(anyInt())).thenReturn(jobEntity);
        when(jobService.getJmdJob(anyInt())).thenReturn(jmdJob);
        when(jmdJob.getStartTime()).thenReturn(startTime);
        when(jmdJob.getLastStatusTime()).thenReturn(lastStatusTime);
        when(jmdServiceAdapter.getDirectGecks(anyInt())).thenReturn("[1111,2222]");
        when(jobService.fetchDirectGecks(any(JobEntity.class), anyInt()))
        .thenReturn(CompletableFuture.completedFuture(null));
        
        jobService.updateJobEntry(1111, null);

        verify(jobService, times(1)).fetchDirectGecks(jobEntity, 1111);
        verify(jobRepository, times(1)).save(jobEntity);
    }
    
    @Test
    public void testExceptionHandling() {
        when(jobService.getJobEntity(anyInt())).thenReturn(null);

        assertDoesNotThrow(() -> {
            jobService.updateJobEntry(1111, null);
        });
        
        verify(jobRepository, never()).save(any(JobEntity.class));
    }

    @Test
    public void testAddJobGecksException() throws Exception {
        Integer jobNumber = 2222;
        when(jobRepository.findByJobNumber(jobNumber)).thenReturn(Optional.empty());
        when(jmdServiceAdapter.getJob(jobNumber)).thenReturn(jmdJob);
        when(jmdServiceAdapter.getDirectGecks(jobNumber)).thenThrow(new RuntimeException("GecksException"));

        assertThrows(TRWSException.class, () -> jobService.addJob(jobNumber, validJobParams));
    }
    
    @Test
    public void testGetJmdJobSuccess() throws Exception {
        Integer jobNumber = 2222;

        when(jmdServiceAdapter.getJob(jobNumber)).thenReturn(jmdJob);

        com.mathworks.bat.jmd.api.job.Job result = jobService.getJmdJob(jobNumber);

        assertEquals(jmdJob, result);
        verify(jmdServiceAdapter, times(1)).getJob(jobNumber);
    }

    @Test
    public void testGetJmdJobException() throws Exception {
        Integer jobNumber = 2222;

        when(jmdServiceAdapter.getJob(jobNumber)).thenThrow(new RuntimeException("JMDJobException"));

        assertThrows(TRWSException.class, () -> {
            jobService.getJmdJob(jobNumber);
        });

        verify(jmdServiceAdapter, times(1)).getJob(jobNumber);
    }
    
    @Test
    public void testFetchDirectGecksSuccess() throws Exception {
        Integer jobNumber = 2222;
        
        String testGecks = "[3333, 4444]";
        when(jmdServiceAdapter.getDirectGecks(jobNumber)).thenReturn(testGecks);

        Future<Void> future = jobService.fetchDirectGecks(jobEntity, jobNumber);
        future.get();

        verify(jmdServiceAdapter, times(1)).getDirectGecks(jobNumber);
        verify(jobRepository, times(1)).save(jobEntity);
        assert jobEntity.getGecks().equals("3333,4444");
    }
    
    @Test
    public void testFetchDirectGecksFailure() throws Exception {
        Integer jobNumber = 2222;
        
        when(jmdServiceAdapter.getDirectGecks(jobNumber)).thenThrow(new RuntimeException("Service error"));

        Future<Void> future = jobService.fetchDirectGecks(jobEntity, jobNumber);
        
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Missing metadata for job number: " + jobNumber, exception.getCause().getMessage());
        verify(jobRepository, never()).save(any(JobEntity.class));
    }
}
