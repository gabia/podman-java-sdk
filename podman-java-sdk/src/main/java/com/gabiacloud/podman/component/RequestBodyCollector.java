/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Converts a request body into a byte array
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class RequestBodyCollector {

    /**
     * Collects the entire body
     *
     * @param publisher body to collect. {@code null} means there is no body
     * @param timeout how long to wait for the body to be fully collected
     * @return collected bytes, or {@code null} if there is no body
     * @throws IOException if the body cannot be produced or is not fully received in time
     * @throws InterruptedException if interrupted while waiting
     */
    static byte @Nullable [] collect(HttpRequest.@Nullable BodyPublisher publisher, @NonNull Duration timeout)
            throws IOException, InterruptedException {
        if (publisher == null) {
            return null;
        }

        var done = new CompletableFuture<byte[]>();
        publisher.subscribe(new Flow.Subscriber<>() {

            private final ByteArrayOutputStream collected = new ByteArrayOutputStream();

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                var chunk = new byte[item.remaining()];
                item.get(chunk);
                collected.writeBytes(chunk);
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                done.complete(collected.toByteArray());
            }
        });

        try {
            return done.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new IOException("request body was not ready in " + timeout, e);
        } catch (ExecutionException e) {
            throw new IOException("cannot build request body", e.getCause());
        }
    }
}
