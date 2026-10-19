/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.component.RequestBodySerializer;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@link RequestBodySerializer} backed by Jackson 2
 */
@AllArgsConstructor
public final class Jackson2RequestBodySerializer implements RequestBodySerializer {

    private final @NonNull ObjectMapper mapper;

    @Override
    public byte @NonNull [] serialize(@Nullable Object payload) throws IOException {
        return mapper.writeValueAsBytes(payload);
    }
}
