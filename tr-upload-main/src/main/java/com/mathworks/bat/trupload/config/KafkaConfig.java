package com.mathworks.bat.trupload.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.util.backoff.FixedBackOff;

import com.mathworks.bat.trupload.util.BrcReader;

/** Configuration class for Kafka */
@Configuration
@EnableKafka
public class KafkaConfig {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfig.class);

    @Autowired
    private BrcReader brcReader;

    private final String GROUP_ID = "trupload.push";
    private final String MSG_OFFSET = "latest";
    private final Integer MAX_POLL_RECORDS = 100;
    private final long RETRY_INTERVAL = 10000L;
    private final long MAX_ATTEMPTS = 4L;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        String kafkaConsumerBroker = brcReader.getString(brcReader.KAFKA_URL);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerBroker);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, MSG_OFFSET);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public RecordFilterStrategy<String, String> recordFilterStrategy() {
        return consumerRecord -> {
            String dataClassHeader = new String(consumerRecord.headers()
                .lastHeader("dataClass").value());
            
            return !("job.state".equals(dataClassHeader));
        };
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> jobListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setRecordFilterStrategy(recordFilterStrategy());
        factory.setAckDiscarded(true); // discard and commit offset
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (consumerRecord, exception) -> {
                // Log the error after max retries
                LOG.error(
                    String.format(
                        "Failed to process record: %s after %d retries", consumerRecord.value(),
                        MAX_ATTEMPTS
                    ), exception
                );
            }, new FixedBackOff(RETRY_INTERVAL, MAX_ATTEMPTS)
        ); // Retry every KafkaConstants.RETRY_INTERVAL ms, max KafkaConstants.MAX_ATTEMPTS + 1 times

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}