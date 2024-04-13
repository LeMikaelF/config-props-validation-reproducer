package com.mikaelfrancoeur.validationreproducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

class ValidationReproducerTest implements WithAssertions {

    private ApplicationContextRunner runner;

    @BeforeEach
    void beforeEach() {
        runner = new ApplicationContextRunner()
                .withBean(LocalValidatorFactoryBean.class, this::newLocalValidatorFactoryBean)
                .withBean(TestConfig.class);
    }

    @Test
    void nestedClassMap() {
        runner.withPropertyValues("props.nested-map.something.prop1=myvalue")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("Field error in object 'props.nested-map.something' on field 'prop2': rejected value [null]"));
    }

    @Test
    void nestedClassSet() {
        runner.withPropertyValues("props.nested-set[0].prop1=myvalue")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("Field error in object 'props.nested-set[0]' on field 'prop2': rejected value [null]"));
    }

    @Data
    @Validated
    @ConfigurationProperties(prefix = "props")
    static class ConfigPropsClass {

        private Map<String, MyClass> nestedMap = new HashMap<>();
        private Set<MyClass> nestedSet = new HashSet<>();

        @Data
        static class MyClass {
            String prop1;
            @NotEmpty String prop2;
        }
    }

    @EnableConfigurationProperties(ConfigPropsClass.class)
    static class TestConfig {
    }

    private LocalValidatorFactoryBean newLocalValidatorFactoryBean() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }
}
