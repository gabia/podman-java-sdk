/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.component.ResponseBodyConverter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * {@link ResponseBodyConverter} backed by Jackson 2
 */
@AllArgsConstructor
public final class Jackson2ResponseBodyConverter implements ResponseBodyConverter {

    private final @NonNull ObjectMapper mapper;

    @Override
    public <T> @NonNull T convertValue(byte @NonNull [] payload, @NonNull Class<T> clazz) throws IOException {
        return mapper.readValue(payload, clazz);
    }

    @Override
    public <T> @NonNull List<T> convertList(byte @NonNull [] payload, @NonNull Class<T> clazz) throws IOException {
        var type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);

        return Objects.requireNonNullElseGet(mapper.<List<T>>readValue(payload, type), List::of);
    }
}
