/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import com.gabiacloud.podman.model.PodmanError;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Reads libpod error bodies
 */
public interface ErrorMessageResolver {

    /**
     * @param body non-empty error body
     * @return {@code null} if the body is JSON {@code null}
     * @throws IOException if the body is not a libpod error
     */
    @Nullable PodmanError resolve(byte @NonNull [] body) throws IOException;
}
