package com.mathworks.bat.trupload.health;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathworks.bat.reliability.HealthStatusCode;
import com.mathworks.bat.reliability.HttpGetInvocation;

/**
 * Utility class that checks the response of the get request of the Ping API to get the health
 * status of an application sub-component.
 */
public class HealthStatusChecker {

    private static final Logger LOG = LoggerFactory.getLogger(HealthStatusChecker.class);
    private final URL url;
    private static final String CRITICAL_STATUS_TEXT =
        "Check the component's health status for more details";
    private static final String REQUEST_FAILURE_TEXT = "Unable to execute the get request";
    private static final String GOOD_HEALTH_STATE = "OK";
    private static HealthStatusCode healthStatusCode = HealthStatusCode.WARNING;
    private static String statusText = null;

    public HealthStatusChecker(URL url) {
        this.url = url;
    }

    /**
     * Uses the utility in the health check jar to execute the get request and checks the result to
     * get the health status of the application.
     */
    public void getHealthStatus() {
        HttpGetInvocation httpGetInvocation = new HttpGetInvocation(url);
        try {
            if (httpGetInvocation.getState().equals(GOOD_HEALTH_STATE)) {
                healthStatusCode = HealthStatusCode.OK;
            } else {
                healthStatusCode = HealthStatusCode.CRITICAL;
                statusText = CRITICAL_STATUS_TEXT;
            }
        } catch (Exception e) {
            healthStatusCode = HealthStatusCode.CRITICAL;
            statusText = REQUEST_FAILURE_TEXT;
            LOG.error(REQUEST_FAILURE_TEXT, e);
        }
    }

    /**
     * Get the health status code for the sub-component application.
     *
     * @return HealthStatusCode health status code
     */
    public HealthStatusCode getHealthStatusCode() {
        return healthStatusCode;
    }

    /**
     * Get the health status text for the sub-component application.
     *
     * @return String status text
     */
    public String getHealthStatusText() {
        return statusText;
    }
}