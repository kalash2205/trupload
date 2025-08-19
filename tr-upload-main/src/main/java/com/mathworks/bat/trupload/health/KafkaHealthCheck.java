package com.mathworks.bat.trupload.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mathworks.bat.reliability.ComponentHealthResult;
import com.mathworks.bat.reliability.IHealthCheck;
import com.mathworks.bat.trupload.util.BrcReader;

@Service
public class KafkaHealthCheck implements IHealthCheck {

    @Autowired
    private BrcReader brcReader;

    public static final String COMPONENT_NAME = "KAFKA";
    private static final boolean IS_CRITICAL_COMPONENT = true;
    private static final String COMPONENT_DESCRIPTION = "KAFKA health status from the URL";

    @Override
    public ComponentHealthResult check() {
        String kafkaUrl = brcReader.getString(brcReader.KAFKA_URL);
        ComponentHealthCheck result = new ComponentHealthCheck(
            COMPONENT_NAME,
            COMPONENT_DESCRIPTION,
            IS_CRITICAL_COMPONENT,
            kafkaUrl
        );
        return result.createKafkaConnection(kafkaUrl);
    }
}