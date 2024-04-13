package com.mikaelfrancoeur.validationreproducer;

import java.util.HashMap;
import java.util.Map;

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
import lombok.RequiredArgsConstructor;

class ValidationReproducerTest implements WithAssertions {

    private ApplicationContextRunner runner;

    @BeforeEach
    void beforeEach() {
        runner = new ApplicationContextRunner()
                .withBean(LocalValidatorFactoryBean.class, this::newLocalValidatorFactoryBean)
                .withBean(TestConfig.class);
    }

    @Test
    void nestedRecordMap() {
        runner.withPropertyValues("props.nested-record-map.something.prop1=something")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("Field error in object 'props.nested-record-map.something' on field 'prop2': rejected value [null]"));
    }

    @Test
    void nestedClassMap() {
        runner.withPropertyValues("props.nested-class-map.something.prop1=something")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("Field error in object 'props.nested-class-map.something' on field 'prop2': rejected value [null]"));
    }

    @Data
    @Validated
    @ConfigurationProperties(prefix = "props")
    static class ConfigPropsClass {

        private Map<String, NestedRecord> nestedRecordMap = new HashMap<>();
        private Map<String, NestedClass> nestedClassMap = new HashMap<>();

        record NestedRecord(
                String prop1,
                @NotEmpty String prop2
        ) {
        }

        @RequiredArgsConstructor
        @SuppressWarnings("ClassCanBeRecord")
        static class NestedClass {
            final String prop1;
            @NotEmpty final String prop2;
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
