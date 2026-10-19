/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson3;

import com.gabiacloud.podman.component.RequestBodySerializer;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link RequestBodySerializer} backed by Jackson 3
 */
@AllArgsConstructor
public final class Jackson3RequestBodySerializer implements RequestBodySerializer {

    private final @NonNull ObjectMapper mapper;

    @Override
    public byte @NonNull [] serialize(@Nullable Object payload) throws IOException {
        try {
            return mapper.writeValueAsBytes(payload);
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
}
