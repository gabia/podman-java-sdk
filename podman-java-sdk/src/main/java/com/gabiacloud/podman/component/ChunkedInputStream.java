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
 * Decodes and reads a {@code Transfer-Encoding: chunked} body
 */
final class ChunkedInputStream extends InputStream {

    private static final int MAX_SIZE_LINE = 32;

    private final @NonNull InputStream source;

    private long remaining;

    private boolean finished;

    ChunkedInputStream(@NonNull InputStream source) {
        this.source = source;
    }

    @Override
    public int read() throws IOException {
        var one = new byte[1];
        var count = read(one, 0, 1);

        return count < 0 ? -1 : one[0] & 0xFF;
    }

    @Override
    public int read(byte @NonNull [] target, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        if (finished || !ensureChunk()) {
            return -1;
        }

        var count = source.read(target, offset, (int) Math.min(length, remaining));
        if (count < 0) {
            throw new EOFException("chunk body ended early");
        }

        remaining -= count;
        if (remaining == 0) {
            expectCrlf();
        }

        return count;
    }

    /** @return {@code true} if there is a chunk to read */
    private boolean ensureChunk() throws IOException {
        if (remaining > 0) {
            return true;
        }

        var size = readSize();
        if (size == 0) {
            drainTrailers();
            finished = true;

            return false;
        }

        remaining = size;

        return true;
    }

    private long readSize() throws IOException {
        var line = readLine();
        var end = line.indexOf(';');
        var text = (end < 0 ? line : line.substring(0, end)).trim();
        if (text.isEmpty() || text.length() > MAX_SIZE_LINE) {
            throw new IOException("malformed chunk size: " + line);
        }

        try {
            var size = Long.parseLong(text, 16);
            if (size < 0) {
                throw new IOException("negative chunk size: " + line);
            }

            return size;
        } catch (NumberFormatException e) {
            throw new IOException("malformed chunk size: " + line, e);
        }
    }

    private void drainTrailers() throws IOException {
        while (!readLine().isEmpty()) {}
    }

    private void expectCrlf() throws IOException {
        if (!readLine().isEmpty()) {
            throw new IOException("chunk is not terminated by CRLF");
        }
    }

    @NonNull private String readLine() throws IOException {
        var line = new StringBuilder();

        int next;
        while ((next = source.read()) != -1) {
            if (next == '\n') {
                if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
                    line.setLength(line.length() - 1);
                }

                return line.toString();
            }

            line.append((char) next);
            if (line.length() > MAX_SIZE_LINE * 8) {
                throw new IOException("chunk header line is too long");
            }
        }

        throw new EOFException("chunked body ended before its terminator");
    }
}
