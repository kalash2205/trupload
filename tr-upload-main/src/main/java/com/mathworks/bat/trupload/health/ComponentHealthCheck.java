package com.mathworks.bat.trupload.health;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.mathworks.bat.reliability.ComponentHealthResult;
import com.mathworks.bat.reliability.HealthStatusCode;

/** Component health check. */
public class ComponentHealthCheck {

    private String componentName;
    private String componentDescription;
    private boolean isCriticalComponent;
    private String healthCheckUrl;
    private String MSG_OFFSET = "latest";
    private Integer MAX_POLL_RECORDS = 100;

    public ComponentHealthCheck(String componentName, String componentDescription,
        boolean isCriticalComponent, String healthCheckUrl) {
        this.componentName = componentName;
        this.componentDescription = componentDescription;
        this.isCriticalComponent = isCriticalComponent;
        this.healthCheckUrl = healthCheckUrl;
    }
    
    public ComponentHealthResult check() {
        ComponentHealthResult result = new ComponentHealthResult(
            componentName, HealthStatusCode.WARNING, isCriticalComponent
        );
        
        URL url = null;
        try {
            url = new URL(healthCheckUrl);
        } catch (Exception e) {
            result.setDescription("Unable to Convert the URL");
            return result;
        }
        
        result.setDescription(componentDescription);
        return pingURL(url);
    }

    /**
     * Method to execute the get request to the provided URL and return the Component Health Result.
     * @param url URL for which we need to check health status.
     *
     * @return ComponentHealthResult result
     */
    public ComponentHealthResult pingURL(URL url) {
        ComponentHealthResult result =
            new ComponentHealthResult(componentName, HealthStatusCode.WARNING, isCriticalComponent);
        result.setDescription(componentDescription);
        result.setUri(url.toString());
        HealthStatusChecker healthStatusChecker = new HealthStatusChecker(url);
        healthStatusChecker.getHealthStatus();
        result.setStatusCode(healthStatusChecker.getHealthStatusCode());
        result.setStatusText(healthStatusChecker.getHealthStatusText());
        return result;
    }

    public ComponentHealthResult createKafkaConnection(String kafkaConsumerBroker) {
        ComponentHealthResult result =
            new ComponentHealthResult(componentName, HealthStatusCode.WARNING, isCriticalComponent);
        result.setDescription(componentDescription);
        result.setUri(kafkaConsumerBroker);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerBroker);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, MSG_OFFSET);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        KafkaConsumer<String, String> consumer = null;
        try {
            consumer = new KafkaConsumer<>(props);
            consumer.listTopics();
            result.setStatusCode(HealthStatusCode.OK);
            result.setStatusText("Able to connect to KAFKA broker");
         } catch (KafkaException e) {
             result.setStatusCode(HealthStatusCode.EXECUTION_FAILURE);
             result.setStatusText("Unable able to connect to KAFKA broker");
         } finally {
            if (consumer != null) {
               consumer.close();
            }
         }
        return result;
    }
}