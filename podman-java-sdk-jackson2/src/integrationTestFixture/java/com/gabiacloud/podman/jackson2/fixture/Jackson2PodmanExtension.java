/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson2.fixture;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gabiacloud.podman.component.ErrorMessageResolver;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.extension.PodmanInfraExtension;
import com.gabiacloud.podman.jackson2.Jackson2ErrorMessageResolver;
import com.gabiacloud.podman.jackson2.Jackson2RequestBodySerializer;
import com.gabiacloud.podman.jackson2.Jackson2ResponseBodyConverter;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Registers the Jackson 2 serializer implementation for integration tests
 */
public final class Jackson2PodmanExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot().getStore(PodmanInfraExtension.NAMESPACE);
        if (store.get(ResponseBodyConverter.class) != null) {
            return;
        }

        var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

        store.put(RequestBodySerializer.class, new Jackson2RequestBodySerializer(mapper));
        store.put(ResponseBodyConverter.class, new Jackson2ResponseBodyConverter(mapper));
        store.put(ErrorMessageResolver.class, new Jackson2ErrorMessageResolver(mapper));
    }
}
