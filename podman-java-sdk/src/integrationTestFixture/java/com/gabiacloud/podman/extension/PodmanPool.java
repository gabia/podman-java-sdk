/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.extension;

import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.support.TestEnvironment;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Pool of Podman instances that test classes lease exclusively
 */
final class PodmanPool {

    static final String POOL_SIZE_PROPERTY = "podman.test.pool-size";

    private static final int DEFAULT_POOL_SIZE = 2;

    private static final String DEFAULT_IMAGE = "quay.io/podman/stable:v5.8.7";

    private static final int PORT = 8080;

    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(180);

    private static final System.Logger LOGGER = System.getLogger(PodmanPool.class.getName());

    private static final PodmanPool POOL = create();

    private final int size;

    private final boolean external;

    private final Semaphore permits;

    private final Queue<Instance> idle = new ConcurrentLinkedQueue<>();

    private PodmanPool(int size, boolean external, List<Instance> instances) {
        this.size = size;
        this.external = external;
        this.permits = new Semaphore(size, true);
        this.idle.addAll(instances);
    }

    static PodmanPool get() {
        return POOL;
    }

    int size() {
        return size;
    }

    Lease lease() {
        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for a Podman instance", e);
        }

        try {
            var instance = idle.poll();
            return new Lease(this, instance == null ? Instance.start() : instance);
        } catch (RuntimeException e) {
            permits.release();
            throw e;
        }
    }

    private void release(Instance instance, Runnable cleaner) {
        try {
            if (external || cleaner == null) {
                idle.offer(instance);
                return;
            }

            try {
                cleaner.run();
                idle.offer(instance);
            } catch (RuntimeException e) {
                LOGGER.log(System.Logger.Level.WARNING, "discarding a Podman instance that could not be reset", e);
                instance.stop();
            }
        } finally {
            permits.release();
        }
    }

    private static PodmanPool create() {
        if (Boolean.parseBoolean(TestEnvironment.get("USE_EXTERNAL_PODMAN"))) {
            var uris = TestEnvironment.get("TEST_PODMAN_URI");
            var instances = Arrays.stream(uris == null ? new String[0] : uris.split(","))
                    .map(String::strip)
                    .filter(uri -> !uri.isEmpty())
                    .map(uri -> new Instance(propertyOf(URI.create(uri)), null))
                    .toList();
            if (instances.isEmpty()) {
                throw new IllegalStateException("TEST_PODMAN_URI is required when USE_EXTERNAL_PODMAN is true");
            }

            return new PodmanPool(instances.size(), true, instances);
        }

        var size = Integer.getInteger(POOL_SIZE_PROPERTY, DEFAULT_POOL_SIZE);
        if (size < 1) {
            throw new IllegalStateException(POOL_SIZE_PROPERTY + " must be positive: " + size);
        }

        return new PodmanPool(size, false, List.of());
    }

    private static PodmanProperty propertyOf(URI host) {
        return PodmanProperty.builder()
                .host(host)
                .timeout(PodmanProperty.DEFAULT_TIMEOUT)
                .build();
    }

    static final class Lease implements AutoCloseable {

        private final PodmanPool pool;

        private final Instance instance;

        private volatile Runnable cleaner;

        private boolean released;

        private Lease(PodmanPool pool, Instance instance) {
            this.pool = pool;
            this.instance = instance;
        }

        PodmanProperty property() {
            return instance.property();
        }

        void onRelease(Runnable cleaner) {
            this.cleaner = cleaner;
        }

        @Override
        public synchronized void close() {
            if (released) {
                return;
            }

            released = true;
            pool.release(instance, cleaner);
        }
    }

    private record Instance(PodmanProperty property, GenericContainer<?> container) {

        @SuppressWarnings("resource")
        static Instance start() {
            var image = TestEnvironment.get("PODMAN_IMAGE");
            var container = new GenericContainer<>(
                            DockerImageName.parse(image == null || image.isBlank() ? DEFAULT_IMAGE : image))
                    .withPrivilegedMode(true)
                    .withCommand("podman", "system", "service", "--time=0", "tcp://0.0.0.0:" + PORT)
                    .withExposedPorts(PORT)
                    .waitingFor(Wait.forHttp("/_ping").forPort(PORT).withStartupTimeout(STARTUP_TIMEOUT));
            container.start();

            var host = URI.create("tcp://%s:%d".formatted(container.getHost(), container.getMappedPort(PORT)));
            return new Instance(propertyOf(host), container);
        }

        void stop() {
            if (container != null) {
                container.stop();
            }
        }
    }
}
