package de.codearcs.spring.data.jpa.repository;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(value = "de.codearcs.spring.data.jpa.repository",
    repositoryFactoryBeanClass = TreeRepositoryFactoryBean.class)
public class ConfigurationClass {
}
