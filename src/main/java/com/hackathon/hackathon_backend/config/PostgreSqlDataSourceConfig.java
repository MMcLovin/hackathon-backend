package com.hackathon.hackathon_backend.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PostgreSqlDataSourceConfig {

    @Value("${spring.datasource.postgresql.url}")
    private String url;

    @Value("${spring.datasource.postgresql.username}")
    private String username;

    @Value("${spring.datasource.postgresql.password}")
    private String password;

    @Value("${spring.datasource.postgresql.driver-class-name}")
    private String driver;

    @Bean(name = "postgreSqlDataSource")
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