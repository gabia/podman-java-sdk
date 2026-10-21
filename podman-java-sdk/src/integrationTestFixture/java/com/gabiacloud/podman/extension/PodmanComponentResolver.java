/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.extension;

import com.gabiacloud.podman.api.ContainerApi;
import com.gabiacloud.podman.api.ImageApi;
import com.gabiacloud.podman.api.PodApi;
import com.gabiacloud.podman.api.SystemApi;
import com.gabiacloud.podman.component.ErrorMessageResolver;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.PodmanRequestProcessor;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.support.PodmanFixtures;
import java.util.Set;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Assembles the APIs with the serializer implementation registered in the store and injects them into test methods
 */
public final class PodmanComponentResolver implements BeforeAllCallback, ParameterResolver {

    private static final Set<Class<?>> SUPPORTED_TYPES = Set.of(
            PodmanProperty.class,
            PodmanRequestExecutor.class,
            SystemApi.class,
            PodApi.class,
            ContainerApi.class,
            ImageApi.class,
            PodmanFixtures.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot().getStore(PodmanInfraExtension.NAMESPACE);
        if (store.get(PodmanRequestExecutor.class) != null) {
            return;
        }

        var properties = store.get(PodmanProperty.class, PodmanProperty.class);
        var serializer = store.get(RequestBodySerializer.class, RequestBodySerializer.class);
        var converter = store.get(ResponseBodyConverter.class, ResponseBodyConverter.class);
        var errorMessageResolver = store.get(ErrorMessageResolver.class, ErrorMessageResolver.class);
        if (serializer == null || converter == null || errorMessageResolver == null) {
            throw new IllegalStateException("no serializer implementation is registered");
        }

        var executor = new PodmanRequestExecutor(PodmanRequestProcessor.forHost(properties), errorMessageResolver);

        var pods = new PodApi(properties, executor, serializer, converter);
        var images = new ImageApi(properties, executor, converter);

        store.put(PodmanRequestExecutor.class, executor);
        store.put(SystemApi.class, new SystemApi(properties, executor, converter));
        store.put(PodApi.class, pods);
        store.put(ContainerApi.class, new ContainerApi(properties, executor, serializer, converter));
        store.put(ImageApi.class, images);
        store.put(PodmanFixtures.class, new PodmanFixtures(properties, executor, serializer, pods, images));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return SUPPORTED_TYPES.contains(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return extensionContext
                .getRoot()
                .getStore(PodmanInfraExtension.NAMESPACE)
                .get(parameterContext.getParameter().getType());
    }
}
