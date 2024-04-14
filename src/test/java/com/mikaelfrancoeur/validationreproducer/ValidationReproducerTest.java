package com.mikaelfrancoeur.validationreproducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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
    void nestedObject() {
        runner
                .withPropertyValues("props.nested.myprop1=value")
                .run(context -> assertThat(context).getFailure().rootCause()
                .hasMessageContaining("Field error in object 'props.nested' on field 'myprop2': rejected value [null]"));
    }

    @Test
    void nestedCollection() {
        runner
                .withPropertyValues("props.list[0].myprop1=value")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("Field error in object 'props.list[0]' on field 'myprop2': rejected value [null]"));
    }

    @Test
    void nestedMap() {
        runner
                .withPropertyValues("props.map.key.myprop1=value")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("Field error in object 'props.map.key' on field 'myprop2': rejected value [null]"));
    }

    @Data
    @Validated
    @ConfigurationProperties(prefix = "props")
    static class ConfigPropsClass {
        private Nested nested;
        private Collection<Nested> list;
        private Map<String, Nested> map;

        @Data
        static class Nested {
            String myprop1;
            @NotEmpty String myprop2;
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
