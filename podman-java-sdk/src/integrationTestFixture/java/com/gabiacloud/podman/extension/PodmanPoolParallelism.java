/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.extension;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

/**
 * Runs as many test classes concurrently as the {@link PodmanPool} has instances
 */
public final class PodmanPoolParallelism implements ParallelExecutionConfigurationStrategy {

    private static final int MAX_EXTRA_THREADS = 256;

    private static final int KEEP_ALIVE_SECONDS = 30;

    @Override
    public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        var parallelism = PodmanPool.get().size();

        return new ParallelExecutionConfiguration() {
            @Override
            public int getParallelism() {
                return parallelism;
            }

            @Override
            public int getMinimumRunnable() {
                return parallelism;
            }

            @Override
            public int getMaxPoolSize() {
                return parallelism + MAX_EXTRA_THREADS;
            }

            @Override
            public int getCorePoolSize() {
                return parallelism;
            }

            @Override
            public int getKeepAliveSeconds() {
                return KEEP_ALIVE_SECONDS;
            }
        };
    }
}
