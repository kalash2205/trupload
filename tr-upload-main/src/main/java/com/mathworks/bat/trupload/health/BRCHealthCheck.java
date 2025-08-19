package com.mathworks.bat.trupload.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mathworks.bat.reliability.ComponentHealthResult;
import com.mathworks.bat.reliability.IHealthCheck;

/** Health check for BRC component. */
@Service
public class BRCHealthCheck implements IHealthCheck {
    
    @Value("${brcHealthUrl}")
    private String brcHealthUrl;

    public static final String COMPONENT_NAME = "BRC";
    private static final boolean IS_CRITICAL_COMPONENT = false;
    private static final String COMPONENT_DESCRIPTION =
        "BRC-WS health status from the ping API";

    @Override
    public ComponentHealthResult check() {
        ComponentHealthCheck result = new ComponentHealthCheck(
            COMPONENT_NAME,
            COMPONENT_DESCRIPTION,
            IS_CRITICAL_COMPONENT,
            brcHealthUrl
        );
        return result.check();
    }
}