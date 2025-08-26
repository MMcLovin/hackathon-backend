package com.hackathon.hackathon_backend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.hackathon.hackathon_backend.repositories.remote",
        entityManagerFactoryRef = "remoteEntityManagerFactory",
        transactionManagerRef = "remoteTransactionManager"
)
public class RemoteJpaConfig {

    private final EntityManagerFactoryBuilder builder;

    public RemoteJpaConfig(EntityManagerFactoryBuilder builder) {
        this.builder = builder;
    }
    @Bean(name = "remoteEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean remoteEntityManagerFactory(
            @Qualifier("sqlServerDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.hackathon.hackathon_backend.models.remote")
                .persistenceUnit("remotePU")
                .build();
    }

    @Bean(name = "remoteTransactionManager")
    public PlatformTransactionManager remoteTransactionManager(
            @Qualifier("remoteEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}
