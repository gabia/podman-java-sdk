/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson3.fixture;

import com.gabiacloud.podman.component.ErrorMessageResolver;
import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import com.gabiacloud.podman.extension.PodmanInfraExtension;
import com.gabiacloud.podman.jackson3.Jackson3ErrorMessageResolver;
import com.gabiacloud.podman.jackson3.Jackson3RequestBodySerializer;
import com.gabiacloud.podman.jackson3.Jackson3ResponseBodyConverter;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import tools.jackson.databind.json.JsonMapper;

/**
 * Registers the Jackson 3 serializer implementation for integration tests
 */
public final class Jackson3PodmanExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot().getStore(PodmanInfraExtension.NAMESPACE);
        if (store.get(ResponseBodyConverter.class) != null) {
            return;
        }

        var mapper = JsonMapper.builder().build();

        store.put(RequestBodySerializer.class, new Jackson3RequestBodySerializer(mapper));
        store.put(ResponseBodyConverter.class, new Jackson3ResponseBodyConverter(mapper));
        store.put(ErrorMessageResolver.class, new Jackson3ErrorMessageResolver(mapper));
    }
}
