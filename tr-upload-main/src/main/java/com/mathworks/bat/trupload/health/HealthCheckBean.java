package com.mathworks.bat.trupload.health;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mathworks.bat.reliability.AppHealthResult;
import com.mathworks.bat.reliability.ComponentHealthResult;
import com.mathworks.bat.reliability.HealthCheckRegistry;

@Component
public class HealthCheckBean {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckBean.class);

    private static HealthCheckRegistry healthCheckRegistry;
    private static final String APP_NAME = "TRUpload-WS";
    private static final String APP_GROUP_NAME = "Build and Test Server";

    @Autowired
    private DatabaseHealthCheck databaseHealthCheck;

    @Autowired
    private BRCHealthCheck brcHealthCheck;

    @Autowired
    private JMDHealthCheck jmdHealthCheck;

    @Autowired
    private KafkaHealthCheck kafkaHealthCheck;

    @PostConstruct
    private void init() {
        healthCheckRegistry = new HealthCheckRegistry();
        healthCheckRegistry.register(DatabaseHealthCheck.COMPONENT_NAME, databaseHealthCheck);
        healthCheckRegistry.register(BRCHealthCheck.COMPONENT_NAME, brcHealthCheck);
        healthCheckRegistry.register(JMDHealthCheck.COMPONENT_NAME, jmdHealthCheck);
        healthCheckRegistry.register(KafkaHealthCheck.COMPONENT_NAME, kafkaHealthCheck);
    }

    public AppHealthResult getAppHealthInfo() {
        Map<String, ComponentHealthResult> healthChecks = healthCheckRegistry.runHealthChecks();
        List<ComponentHealthResult> healthCheckList = healthChecks.values().stream().collect(Collectors.toList());

        AppHealthResult appHealthResult = new AppHealthResult(APP_NAME, healthCheckList);
        appHealthResult.setGroup(APP_GROUP_NAME);

        String currentVersion = getClass().getPackage().getImplementationVersion();
        appHealthResult.setVersion(currentVersion);

        LOG.debug(
            "HealthCheck returning: {}",
            HealthCheckRegistry.deriveAppHealthStatus(appHealthResult).getState().toString()
        );

        return HealthCheckRegistry.deriveAppHealthStatus(appHealthResult);
    }
}