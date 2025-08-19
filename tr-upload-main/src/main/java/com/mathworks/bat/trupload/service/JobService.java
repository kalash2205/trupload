package com.mathworks.bat.trupload.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.mathworks.bat.jmd.api.job.Job;
import com.mathworks.bat.trupload.exception.TRWSException;
import com.mathworks.bat.trupload.model.ClusterEntity;
import com.mathworks.bat.trupload.model.JobEntity;
import com.mathworks.bat.trupload.model.SequenceEntity;
import com.mathworks.bat.trupload.model.repository.ClusterRepository;
import com.mathworks.bat.trupload.model.repository.JobRepository;
import com.mathworks.bat.trupload.model.repository.SequenceRepository;
import com.mathworks.bat.trupload.util.JmdServiceAdapter;
import com.mathworks.bat.trupload.view.KafkaMessageData;
import com.mathworks.bat.trupload.view.JobParams;

@Service
public class JobService {

    public static final Logger LOG = LoggerFactory.getLogger(JobService.class);

    private final ClusterRepository clusterRepository;
    private final SequenceRepository sequenceRepository;
    private final JobRepository jobRepository;
    private final JmdServiceAdapter jmdServiceAdapter;
    private Set<Integer> trackedJobs = ConcurrentHashMap.newKeySet();
    
    private final ExecutorService gecksExecutor = Executors.newSingleThreadExecutor();
    
    private static final List<String> JOB_COMPLETED_STATUSES = Arrays.asList(
        "Accept", "Accepted", "Crashed", "Fail", "Failed", "Rejected"
    );
    
    private static final int CLIENT_VERSION = 4;

    public JobService(ClusterRepository clusterRepository,
                      SequenceRepository sequenceRepository,
                      JobRepository jobRepository,
                      JmdServiceAdapter jmdServiceAdapter) {
        this.clusterRepository = clusterRepository;
        this.sequenceRepository = sequenceRepository;
        this.jobRepository = jobRepository;
        this.jmdServiceAdapter = jmdServiceAdapter;
    }
    
    @PostConstruct
    private void loadIncompleteJobs() {
        List<JobEntity> jobs = jobRepository.findByJobStatusNotIn(JOB_COMPLETED_STATUSES);
        for (JobEntity job : jobs) {
            trackedJobs.add(job.getJobNumber());
        }
        LOG.info("Loaded incomplete jobs: {}", trackedJobs);
    }

    @Transactional(rollbackOn = Exception.class)
    public void processMessage(String data) throws TRWSException {
        try {
            KafkaMessageData jobData = parseJobData(data);

            Integer jobNumber = jobData.getJobId();
            String state = jobData.getState();
            List<String> superStates = jobData.getSuperStates();

            if (trackedJobs.contains(jobNumber) && state != null) {
                updateJobEntry(jobNumber, state);

                if (superStates != null && superStates.contains("COMPLETED")) {
                    LOG.info("Job {} is completed. Removing from tracked jobs.", jobNumber);
                    trackedJobs.remove(jobNumber);
                }
            }
        } catch (TRWSException KafkaMsgException) {
            LOG.error(KafkaMsgException.getMessage());
            throw new TRWSException(KafkaMsgException);
        }
    }
    
    public KafkaMessageData parseJobData(String data) {
        Gson gson = new Gson();
        return gson.fromJson(data, KafkaMessageData.class);
    }

    public void updateJobEntry(Integer jobNumber, String state) throws TRWSException {
        try {
            JobEntity job = getJobEntity(jobNumber);
            if (job == null) {
                return;
            }
            
            LOG.info("Updating information for job {} to = {}", jobNumber, state);
            
            String mappedStatus = mapJobStatus(state);
            job.setJobStatus(mappedStatus);

            Job jmdJob = getJmdJob(jobNumber);
            Calendar lastStatusTime = jmdJob.getLastStatusTime();
            Calendar startTime = jmdJob.getStartTime();
            Timestamp lastStatusTimeinTimestamp = convertToTimestamp(lastStatusTime);

            if (startTime != null) {
                Timestamp startTimeinTimestamp = convertToTimestamp(startTime);
                Long duration = lastStatusTimeinTimestamp.getTime() - startTimeinTimestamp.getTime();
                job.setDuration(duration / 1000);
                job.setLastModified(lastStatusTimeinTimestamp);
            }
            
            jobRepository.save(job);
            fetchDirectGecks(job, jobNumber);

        } catch (TRWSException updateEntryException) {
            LOG.error(updateEntryException.getMessage());
            throw new TRWSException(updateEntryException);
        }
    }
    
    public Future<Void> fetchDirectGecks(JobEntity job, Integer jobNumber) {
        return gecksExecutor.submit(() -> {
            try {
                String gecks = "";
                try {
                    gecks = jmdServiceAdapter.getDirectGecks(jobNumber);
                    gecks = gecks.substring(1, gecks.length() - 1);
                    gecks = gecks.replace(", ", ",");
                } catch (Exception gecksException) {
                    LOG.error("Error fetching gecks: " + gecksException.getMessage());
                    throw new RuntimeException(new TRWSException(gecksException));
                }

                job.setGecks(gecks);
                jobRepository.save(job);

            } catch (RuntimeException exception) {
                if (exception.getCause() instanceof TRWSException) {
                    TRWSException trwsException = (TRWSException) exception.getCause();
                    String errorMessage = "Missing metadata for job number: " + jobNumber;
                    LOG.error("Failed to update gecks: " + trwsException.getMessage());
                    throw new RuntimeException(new TRWSException(errorMessage, trwsException));
                } else {
                    throw exception;
                }
            }
            return null;
        });
    } 

    /**
     * Fetch or create new job
     *
     * @param jobNumber
     * @param newJobConfig
     * @throws TRWSException
     */
    @Transactional(rollbackOn = Exception.class)
    public Integer addJob(Integer jobNumber, JobParams newJobParams) throws TRWSException {
        Optional<JobEntity> foundJobEntity = jobRepository.findByJobNumber(jobNumber);
        JobEntity job = foundJobEntity.orElseGet(JobEntity::new);
        
        trackedJobs.add(jobNumber);

        if (foundJobEntity.isPresent()) {
            return convertToView(job);
        }
        
        if (newJobParams.getBranch() == null || newJobParams.getBranch().isEmpty()) {
            String errorMessage = "Job branch is missing in job parameters for job number " + jobNumber;
            LOG.error(errorMessage);
            throw new TRWSException(errorMessage);
        }

        try {
            Job jmdJob = getJmdJob(jobNumber);
            String clusterName = jmdJob.getCluster();
            ClusterEntity clusterEntity = addCluster(clusterName);
            Integer clusterId = clusterEntity.getId();

            final AtomicInteger sequenceValueHolder = new AtomicInteger();
            sequenceRepository.incrementIdQuery("Job_id");
            Optional<SequenceEntity> sequence = sequenceRepository.findByName("Job_id");

            sequence.ifPresent(sequenceEntity -> {
                sequenceValueHolder.set(sequenceEntity.getSequence());
            });

            String jobStatus = jmdJob.getJobStatus();
            Calendar lastStatusTime = jmdJob.getLastStatusTime();
            Calendar startTime = jmdJob.getStartTime();

            Timestamp lastStatusTimeinTimestamp = convertToTimestamp(lastStatusTime);
            Timestamp startTimeinTimestamp = convertToTimestamp(startTime);

            Long duration = lastStatusTimeinTimestamp.getTime() - startTimeinTimestamp.getTime();

            String jobType = jmdJob.getJobType();
            String submitter = jmdJob.getCreator();
            
            job.setJobNumber(jobNumber)
               .setJobStatus(mapJobStatus(jobStatus))
               .setJobType(jobType)
               .setBranch(newJobParams.getBranch())
               .setId(sequenceValueHolder.get())
               .setClusterId(clusterId)
               .setStartDate(startTimeinTimestamp)
               .setLastModified(lastStatusTimeinTimestamp)
               .setDuration(duration/1000)
               .setSubmitter(submitter);

            jobRepository.setClientVersion(CLIENT_VERSION);

            jobRepository.save(job);
            fetchDirectGecks(job, jobNumber);
            
          } catch (TRWSException JMDJobException) {
            throw new TRWSException(JMDJobException);
          } catch (Exception addJobException) {
            LOG.error(addJobException.getMessage());
            throw new TRWSException(addJobException);
          }

          return convertToView(job);
    }

    protected Timestamp convertToTimestamp(Calendar calendarDate) {
        return new Timestamp(calendarDate.getTimeInMillis());
    }
    
    /**
     * Find existing job entity
     *
     * @param jobNumber
     * @return
     */
    protected JobEntity getJobEntity(Integer jobNumber) {
        return jobRepository.findByJobNumber(jobNumber).orElse(null);
    }

    /**
     * Get Cluster information based on cluster name
     * If cluster does not exist, add new entry for the cluster
     *
     * @param clusterName
     * @return cluster
     * @throws TRWSException
     */

    public ClusterEntity addCluster(String clusterName) throws TRWSException {
        Optional<ClusterEntity> foundClusterEntity = clusterRepository.findByCluster(clusterName);
        ClusterEntity cluster = foundClusterEntity.orElseGet(ClusterEntity::new);

        if (foundClusterEntity.isPresent()) {
            return cluster;
        }

        sequenceRepository.incrementIdQuery("Cluster_id");
        Optional<SequenceEntity> sequence = sequenceRepository.findByName("Cluster_id");

        cluster.setCluster(clusterName);
        sequence.ifPresent(sequenceEntity -> {
            Integer sequenceValue = sequenceEntity.getSequence();
            cluster.setId(sequenceValue);
        });

        return clusterRepository.save(cluster);
    }

    protected Job getJmdJob (Integer jobNumber) throws TRWSException {
        Job jmdJob = null;
        try {
            jmdJob = jmdServiceAdapter.getJob(jobNumber);
        } catch (Exception JMDJobException) {
            LOG.error("Could not fetch the job from JMD");
            throw new TRWSException(JMDJobException.getMessage());
        }
        return jmdJob;
    }


    /**
     * Convert JobEntity to Job
     *
     * @param jobDetails
     * @return
     */
    protected Integer convertToView(JobEntity jobDetails) {
        Integer jobId = (jobDetails.getId());
        return jobId;
    }
    
    private String mapJobStatus(String status) {
        if ("ACCEPTED".equals(status)) {
            return "Accept";
        } else if ("REJECTED".equals(status)) {
            return "Fail";
        }
        return status;
    }
}
