package com.mathworks.bat.trupload.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mathworks.bat.reliability.ComponentHealthResult;
import com.mathworks.bat.reliability.IHealthCheck;
import com.mathworks.bat.trupload.util.BrcReader;

@Service
public class JMDHealthCheck implements IHealthCheck {

    @Autowired
    private BrcReader brcReader;

    public static final String COMPONENT_NAME = "JMD";
    private static final boolean IS_CRITICAL_COMPONENT = true;
    private static final String COMPONENT_DESCRIPTION = "JMD health status from the ping API";

    @Override
    public ComponentHealthResult check() {
        String jmdHealthUrl = brcReader.getString(brcReader.JMD_WEBAPP_URL) + "/rest/health/v2";
        
        ComponentHealthCheck result = new ComponentHealthCheck(
            COMPONENT_NAME,
            COMPONENT_DESCRIPTION,
            IS_CRITICAL_COMPONENT,
            jmdHealthUrl
        );
        return result.check();
    }
}