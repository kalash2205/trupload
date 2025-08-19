package com.mathworks.bat.trupload.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.mathworks.bat.trupload.exception.TRWSException;
import com.mathworks.bat.trupload.service.JobService;

@Component
public class KafkaMsgConsumer {

    @Autowired
    JobService jobService;

    private static final Logger LOG = LoggerFactory.getLogger(KafkaMsgConsumer.class);
    private static final String JOB_TOPIC = "job";
    private static final String GROUP_ID = "trupload";
    /**
     * Listens to messages from the Kafka job topic
     *
     * @param data body of the message
     * @throws TRWSException
     */
    @KafkaListener(
        topics = JOB_TOPIC,
        groupId = GROUP_ID,
        containerFactory = "jobListenerContainerFactory"
        )
    public void topicsListener(@Payload String data, Acknowledgment ack) throws TRWSException {
        LOG.debug(
            String.format(
                "Consumed a message: %s from the topic: %s.", data, JOB_TOPIC
            )
        );
        try {
            jobService.processMessage(data);
            ack.acknowledge();
        } catch (TRWSException ProcessMessageException) {
            LOG.error(ProcessMessageException.getMessage());
            throw ProcessMessageException;
        }
    }
}