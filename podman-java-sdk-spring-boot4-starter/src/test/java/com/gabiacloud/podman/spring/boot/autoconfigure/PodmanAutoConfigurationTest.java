/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.gabiacloud.podman.api.ContainerApi;
import com.gabiacloud.podman.api.ImageApi;
import com.gabiacloud.podman.api.PodApi;
import com.gabiacloud.podman.api.SystemApi;
import com.gabiacloud.podman.property.PodmanProperty;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

final class PodmanAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, PodmanAutoConfiguration.class));

    @Test
    @DisplayName("[Success] Registers the Podman APIs")
    void registersApis() {
        runner.run(context -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(SystemApi.class)
                .hasSingleBean(PodApi.class)
                .hasSingleBean(ContainerApi.class)
                .hasSingleBean(ImageApi.class));
    }

    @Test
    @DisplayName("[Success] Binds host and timeout")
    void bindsProperties() {
        runner.withPropertyValues("podman.host=tcp://127.0.0.1:8080", "podman.timeout=5s")
                .run(context -> {
                    var property = context.getBean(PodmanProperty.class);

                    assertThat(property.getHost()).isEqualTo(URI.create("tcp://127.0.0.1:8080"));
                    assertThat(property.getTimeout()).isEqualTo(Duration.ofSeconds(5));
                });
    }

    @Test
    @DisplayName("[Success] Keeps the application JsonMapper")
    void keepsApplicationJsonMapper() {
        runner.run(context -> assertThat(context.getBean(JsonMapper.class))
                .as("JsonMapper resolved by type must be the application mapper")
                .isNotSameAs(context.getBean(PodmanAutoConfiguration.JSON_MAPPER_BEAN_NAME)));
    }
}
