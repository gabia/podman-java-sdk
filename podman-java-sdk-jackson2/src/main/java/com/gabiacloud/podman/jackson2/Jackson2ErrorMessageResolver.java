/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabiacloud.podman.component.ErrorMessageResolver;
import com.gabiacloud.podman.model.PodmanError;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * {@link ErrorMessageResolver} backed by Jackson 2
 */
@AllArgsConstructor
public final class Jackson2ErrorMessageResolver implements ErrorMessageResolver {

    private final @NonNull ObjectMapper mapper;

    @Override
    public @Nullable PodmanError resolve(byte @NonNull [] body) throws IOException {
        return mapper.readValue(body, PodmanError.class);
    }
}
