package com.mikaelfrancoeur.validationreproducer;

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
    void topLevel() {
        runner.withPropertyValues("props.prop1=myvalue")
                .run(context -> assertThat(context).getFailure().rootCause()
                        .hasMessageContaining("Field error in object 'props' on field 'prop2': rejected value [null]"));
    }

    @Data
    @Validated
    @ConfigurationProperties(prefix = "props")
    static class ConfigPropsClass {
        private String prop1;
        @NotEmpty private String prop2;
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
