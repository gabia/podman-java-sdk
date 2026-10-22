/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Serializes request values into JSON
 */
public interface RequestBodySerializer {

    /**
     * @return UTF-8 JSON
     * @throws IOException if serialization fails
     */
    byte @NonNull [] serialize(@Nullable Object payload) throws IOException;
}
