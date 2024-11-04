package com.mcode.llp.codeGen.models;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

@Configuration
@ComponentScan
public class EnvironmentConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment env) {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        try {
            env.getPropertySources().addLast(new ResourcePropertySource("file:.env"));
        } catch (IOException e) {
            throw new RuntimeException("Could not load .env file", e);
        }
        return propertySourcesPlaceholderConfigurer;
    }
}
