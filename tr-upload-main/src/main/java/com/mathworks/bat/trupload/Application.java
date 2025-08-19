package com.mathworks.bat.trupload;

import javax.servlet.Filter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;

import com.mathworks.bat.trupload.config.RequestFilter;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableHystrix
@EnableHystrixDashboard
@OpenAPIDefinition(info = @Info(title = "TR Upload Web Service", description = "A Web Service that persists data to test results database"), externalDocs = @ExternalDocumentation(description = "Documentation for TR Upload Web Service", url = "https://github.mathworks.com/development/tr-upload"))

public class Application {

    public static void main(String[] args) {
        /*
         * Hystrix configurations
         */
        System.setProperty("archaius.configurationSource.additionalUrls", "file:config/dynamic.properties");
        System.setProperty("archaius.fixedDelayPollingScheduler.initialDelayMills", "1000");
        System.setProperty("archaius.fixedDelayPollingScheduler.delayMills", "2000");
        SpringApplication.run(Application.class, args);
    }
    
    /**
     * Bean to add a filter to the filter chain.
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<Filter> initFilterRegistration() {
        FilterRegistrationBean<Filter> registry = new FilterRegistrationBean<>();
        registry.setFilter(getRequestFilter());
        registry.addUrlPatterns("/*");
        return registry;
    }

    /**
     * Method to get the filter.
     *
     * @return Filter
     */
    @Bean
    public Filter getRequestFilter() {
        return new RequestFilter();
    }
}