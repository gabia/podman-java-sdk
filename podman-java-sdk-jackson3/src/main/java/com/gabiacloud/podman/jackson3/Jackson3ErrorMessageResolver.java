/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.jackson3;

import com.gabiacloud.podman.component.ErrorMessageResolver;
import com.gabiacloud.podman.model.PodmanError;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link ErrorMessageResolver} backed by Jackson 3
 */
@AllArgsConstructor
public final class Jackson3ErrorMessageResolver implements ErrorMessageResolver {

    private final @NonNull ObjectMapper mapper;

    @Override
    public @Nullable PodmanError resolve(byte @NonNull [] body) throws IOException {
        try {
            return mapper.readValue(body, PodmanError.class);
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }
}
