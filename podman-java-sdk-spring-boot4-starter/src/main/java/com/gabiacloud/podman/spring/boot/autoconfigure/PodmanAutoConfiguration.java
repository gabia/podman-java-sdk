/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.spring.boot.autoconfigure;

import com.gabiacloud.podman.api.ContainerApi;
import com.gabiacloud.podman.api.ImageApi;
import com.gabiacloud.podman.api.PodApi;
import com.gabiacloud.podman.api.SystemApi;
import com.gabiacloud.podman.component.ErrorMessageResolver;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.PodmanRequestProcessor;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.jackson3.Jackson3ErrorMessageResolver;
import com.gabiacloud.podman.jackson3.Jackson3RequestBodySerializer;
import com.gabiacloud.podman.jackson3.Jackson3ResponseBodyConverter;
import com.gabiacloud.podman.property.PodmanProperty;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * Auto-configuration that assembles the Podman APIs with the Jackson 3 serializer implementation
 *
 * <p>The dedicated {@link JsonMapper} is not a default candidate, so it does not take the place of the application's
 * mapper and is injected only by the {@value #JSON_MAPPER_BEAN_NAME} qualifier
 */
@AutoConfiguration
@EnableConfigurationProperties(PodmanSpringProperties.class)
public class PodmanAutoConfiguration {

    /** Name of the {@link JsonMapper} bean used by the Podman serializers */
    public static final String JSON_MAPPER_BEAN_NAME = "podmanJsonMapper";

    /** Creates the connection settings from the bound properties */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public PodmanProperty podmanProperty(@NonNull PodmanSpringProperties properties) {
        return new PodmanProperty(properties.getHost(), properties.getTimeout());
    }

    /** Creates the mapper dedicated to Podman request and response bodies */
    @Bean(name = JSON_MAPPER_BEAN_NAME, defaultCandidate = false)
    @ConditionalOnMissingBean(name = JSON_MAPPER_BEAN_NAME)
    @NonNull public JsonMapper podmanJsonMapper() {
        return JsonMapper.builder().build();
    }

    /** Creates the request body serializer */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public RequestBodySerializer podmanRequestBodySerializer(
            @Qualifier(JSON_MAPPER_BEAN_NAME) @NonNull JsonMapper mapper) {
        return new Jackson3RequestBodySerializer(mapper);
    }

    /** Creates the response body converter */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public ResponseBodyConverter podmanResponseBodyConverter(
            @Qualifier(JSON_MAPPER_BEAN_NAME) @NonNull JsonMapper mapper) {
        return new Jackson3ResponseBodyConverter(mapper);
    }

    /** Creates the error message resolver */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public ErrorMessageResolver podmanErrorMessageResolver(
            @Qualifier(JSON_MAPPER_BEAN_NAME) @NonNull JsonMapper mapper) {
        return new Jackson3ErrorMessageResolver(mapper);
    }

    /** Creates the request processor that matches the host setting */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public PodmanRequestProcessor podmanRequestProcessor(@NonNull PodmanProperty properties) {
        return PodmanRequestProcessor.forHost(properties);
    }

    /** Creates the request executor */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public PodmanRequestExecutor podmanRequestExecutor(
            @NonNull PodmanRequestProcessor processor, @NonNull ErrorMessageResolver errorMessageResolver) {
        return new PodmanRequestExecutor(processor, errorMessageResolver);
    }

    /** Creates the system API */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public SystemApi podmanSystemApi(
            @NonNull PodmanProperty properties,
            @NonNull PodmanRequestExecutor executor,
            @NonNull ResponseBodyConverter converter) {
        return new SystemApi(properties, executor, converter);
    }

    /** Creates the pod API */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public PodApi podmanPodApi(
            @NonNull PodmanProperty properties,
            @NonNull PodmanRequestExecutor executor,
            @NonNull RequestBodySerializer serializer,
            @NonNull ResponseBodyConverter converter) {
        return new PodApi(properties, executor, serializer, converter);
    }

    /** Creates the container API */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public ContainerApi podmanContainerApi(
            @NonNull PodmanProperty properties,
            @NonNull PodmanRequestExecutor executor,
            @NonNull RequestBodySerializer serializer,
            @NonNull ResponseBodyConverter converter) {
        return new ContainerApi(properties, executor, serializer, converter);
    }

    /** Creates the image API */
    @Bean
    @ConditionalOnMissingBean
    @NonNull public ImageApi podmanImageApi(
            @NonNull PodmanProperty properties,
            @NonNull PodmanRequestExecutor executor,
            @NonNull ResponseBodyConverter converter) {
        return new ImageApi(properties, executor, converter);
    }
}
