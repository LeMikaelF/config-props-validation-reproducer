package com.mikaelfrancoeur.validationreproducer;

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
    void topLevel() {
        runner.run(context -> assertThat(context).getFailure().rootCause()
                .hasMessageContaining("Field error in object 'props' on field 'myprop': rejected value [null]"));
    }

    @Test
    void nested() {
        runner
                .withPropertyValues("props.myprop=value", "props.nested.myprop1=value")
                .run(context -> assertThat(context).getFailure().rootCause()
                .hasMessageContaining("Field error in object 'props.nested' on field 'myprop2': rejected value [null]"));
    }

    @Data
    @Validated
    @ConfigurationProperties(prefix = "props")
    static class ConfigPropsClass {
        @NotEmpty private String myprop;

        private Nested nested = new Nested();

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
