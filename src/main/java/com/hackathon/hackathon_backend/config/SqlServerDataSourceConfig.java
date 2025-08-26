package com.hackathon.hackathon_backend.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SqlServerDataSourceConfig {

    @Value("${spring.datasource.sqlserver.url}")
    private String url;

    @Value("${spring.datasource.sqlserver.username}")
    private String username;

    @Value("${spring.datasource.sqlserver.password}")
    private String password;

    @Value("${spring.datasource.sqlserver.driver-class-name}")
    private String driver;

    @Bean(name = "sqlServerDataSource")
    @Primary
    public DataSource postgreSqlDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driver)
                .build();
    }
}