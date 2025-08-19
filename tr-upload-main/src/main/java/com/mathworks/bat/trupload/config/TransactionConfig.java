package com.mathworks.bat.trupload.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.mathworks.bat.trupload.util.BrcReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    @Autowired
    private BrcReader brcReader;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource getDataSource()
    {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(brcReader.getString(brcReader.TRWS_DB_URL));
        hikariConfig.setUsername(brcReader.getString(brcReader.TRWS_DB_USERNAME));
        hikariConfig.setPassword(brcReader.getString(brcReader.TRWS_DB_PASSWORD));

        // Create and return the HikariDataSource
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(getDataSource());
        return transactionManager;
    }

    @Bean
    public TransactionTemplate TransactionTemplate() {
        TransactionTemplate template = new TransactionTemplate(transactionManager());
        return template;
    }
}