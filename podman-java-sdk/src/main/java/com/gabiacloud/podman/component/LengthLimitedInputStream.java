/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.jspecify.annotations.NonNull;

/**
 * Reads only up to {@code Content-Length} bytes
 */
final class LengthLimitedInputStream extends InputStream {

    private final @NonNull InputStream source;

    private long remaining;

    LengthLimitedInputStream(@NonNull InputStream source, long length) {
        this.source = source;
        this.remaining = length;
    }

    @Override
    public int read() throws IOException {
        if (remaining == 0) {
            return -1;
        }

        var next = source.read();
        if (next < 0) {
            throw new EOFException("body ended %d bytes early".formatted(remaining));
        }
        remaining--;

        return next;
    }

    @Override
    public int read(byte @NonNull [] target, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        if (remaining == 0) {
            return -1;
        }

        var count = source.read(target, offset, (int) Math.min(length, remaining));
        if (count < 0) {
            throw new EOFException("body ended %d bytes early".formatted(remaining));
        }
        remaining -= count;

        return count;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(remaining, source.available());
    }
}
