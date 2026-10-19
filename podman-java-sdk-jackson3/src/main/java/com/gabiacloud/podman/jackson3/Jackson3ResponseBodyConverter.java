/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson3;

import com.gabiacloud.podman.component.ResponseBodyConverter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link ResponseBodyConverter} backed by Jackson 3
 */
@AllArgsConstructor
public final class Jackson3ResponseBodyConverter implements ResponseBodyConverter {

    private final @NonNull ObjectMapper mapper;

    @Override
    public <T> @NonNull T convertValue(byte @NonNull [] payload, @NonNull Class<T> clazz) throws IOException {
        try {
            return mapper.readValue(payload, clazz);
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }

    @Override
    public <T> @NonNull List<T> convertList(byte @NonNull [] payload, @NonNull Class<T> clazz) throws IOException {
        var type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);

        try {
            return Objects.requireNonNullElseGet(mapper.<List<T>>readValue(payload, type), List::of);
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
}
