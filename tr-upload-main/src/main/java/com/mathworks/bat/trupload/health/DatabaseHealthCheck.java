package com.mathworks.bat.trupload.health;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.mathworks.bat.reliability.ComponentHealthResult;
import com.mathworks.bat.reliability.HealthStatusCode;
import com.mathworks.bat.reliability.IHealthCheck;
import com.mathworks.bat.trupload.util.BrcReader;

/** Health check for the BaT2Gecko database component. */
@Component
public class DatabaseHealthCheck implements IHealthCheck {

    @Autowired
    private BrcReader brcReader;

    @Autowired
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public static final String COMPONENT_NAME = "TRDB";
    private static final boolean IS_CRITICAL_COMPONENT = true;
    private static final String COMPONENT_DESCRIPTION = "DB health status from the URL";

    @Override
    public ComponentHealthResult check() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        ComponentHealthResult result = new ComponentHealthResult(
            COMPONENT_NAME, HealthStatusCode.WARNING, IS_CRITICAL_COMPONENT
        );
        String dbUrl = brcReader.getString(brcReader.TRWS_DB_URL);
        result.setDescription(COMPONENT_DESCRIPTION);
        result.setUri(dbUrl);

        try {
            jdbcTemplate.execute("SELECT 1");
            result.setStatusText("Connection successful!");
            result.setStatusCode(HealthStatusCode.OK);
        } catch (Exception e) {
            result.setStatusText("Connection failed!");
            result.setStatusCode(HealthStatusCode.EXECUTION_FAILURE);
        }
        return result;
    }
}