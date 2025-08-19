package com.mathworks.bat.trupload.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mathworks.bat.trupload.health.HealthCheckBean;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "TRUpload WS API Home Page", description = "Everything about TR Upload endpoints")
@RestController
public class DefaultController {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultController.class);

    static final String HEALTH_SERVICE = "/health/v2";
    static final String HEALTH_PING = HEALTH_SERVICE + "/ping";
    static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired
    private HealthCheckBean healthCheckerBean;

    @Operation(summary = "Gets the Welcome Page", description = "Gets the Welcome Page")
    @GetMapping("/")
    public String index() {
        LOG.debug("Greetings from TRUpload-WS");
        return "<Center><h1>Welcome to TRUpload Web-Services</h1><p/>Please see <a href=\"/swagger.html\">Swagger</a> for it's API documentation</center>";
    }

    @Operation(summary = "Gets the Health Page", description = "Gets the Health Page")
    @GetMapping(value = { HEALTH_SERVICE, HEALTH_PING }, produces = JSON)
    public ResponseEntity<?> getHealth() {
        return ResponseEntity.ok(healthCheckerBean.getAppHealthInfo());
    }

}