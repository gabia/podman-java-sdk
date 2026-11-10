/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.support;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * Reads integration test settings from {@code .env}, falling back to environment variables
 * <p>
 * {@code .env} is loaded once on first use, which may happen before any extension is created, such as when JUnit
 * configures parallel execution.
 */
public final class TestEnvironment {

    private static final Dotenv DOTENV = load();

    private TestEnvironment() {}

    public static String get(String key) {
        if (DOTENV == null) {
            return System.getenv(key);
        }

        return DOTENV.get(key) == null ? System.getenv(key) : DOTENV.get(key);
    }

    private static Dotenv load() {
        try {
            return Dotenv.load();
        } catch (DotenvException e) {
            return null;
        }
    }
}
