package com.mikaelfrancoeur.validationreproducer;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

class ValidationReproducerTest implements WithAssertions {

    private ApplicationContextRunner runner;

    @BeforeEach
    void beforeEach() {
        runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
                .withBean(TestConfig.class);
    }

    @Test
    void topLevelString() {
        runner
                .withPropertyValues("props.nesteds.something.prop1=something")
                .run(context -> {
                    assertThat(context)
                            .getFailure()
                            .hasMessage("");
                });
    }

    @Data
    @Validated
    @ConfigurationProperties(prefix = "props")
    static class ConfigPropsClass {

        private Map<String, Nested> nesteds = new HashMap<>();

        public record Nested(
                String prop1,
                @NotEmpty String prop2
        ) {
        }
    }

    @EnableConfigurationProperties(ConfigPropsClass.class)
    static class TestConfig {
    }
}
