/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Converts JSON response bodies into models
 */
public interface ResponseBodyConverter {

    /**
     * Reads a JSON value
     *
     * @throws IOException if the body is empty, is not JSON, or does not match the type
     */
    <T> @NonNull T convertValue(byte @NonNull [] payload, @NonNull Class<T> clazz) throws IOException;

    /**
     * Reads a JSON array
     *
     * @throws IOException if the body is empty, is not JSON, or does not match the type
     */
    <T> @NonNull List<T> convertList(byte @NonNull [] payload, @NonNull Class<T> clazz) throws IOException;
}
