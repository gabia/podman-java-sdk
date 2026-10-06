/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.component;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import org.jspecify.annotations.NonNull;

/**
 * Socket channel that enforces timeouts
 */
final class DeadlineChannel implements Closeable {

    private static final int BUFFER_SIZE = 8 * 1024;

    private final @NonNull SocketChannel channel;

    private final @NonNull Selector selector;

    private final @NonNull SelectionKey key;

    private final long deadlineNanos;

    private DeadlineChannel(
            @NonNull SocketChannel channel, @NonNull Selector selector, @NonNull SelectionKey key, long deadlineNanos) {
        this.channel = channel;
        this.selector = selector;
        this.key = key;
        this.deadlineNanos = deadlineNanos;
    }

    /**
     * Connects to a Unix socket
     *
     * @throws SocketTimeoutException if it cannot connect within the time limit
     */
    @NonNull static DeadlineChannel connect(@NonNull UnixDomainSocketAddress address, @NonNull Duration timeout)
            throws IOException {
        var deadline = System.nanoTime() + timeout.toNanos();

        var channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        Selector selector = null;
        try {
            channel.configureBlocking(false);
            selector = Selector.open();
            var key = channel.register(selector, 0);
            var opened = new DeadlineChannel(channel, selector, key, deadline);

            if (!channel.connect(address)) {
                while (!channel.finishConnect()) {
                    opened.await(SelectionKey.OP_CONNECT, "connect");
                }
            }

            return opened;
        } catch (IOException | RuntimeException e) {
            closeQuietly(selector);
            closeQuietly(channel);
            throw e;
        }
    }

    /**
     * Writes until everything is sent
     *
     * @throws SocketTimeoutException if it cannot finish writing before the deadline
     */
    void write(byte @NonNull [] data) throws IOException {
        var buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            if (channel.write(buffer) == 0) {
                await(SelectionKey.OP_WRITE, "write");
            }
        }
    }

    /** Returns the read stream */
    @NonNull InputStream inputStream() {
        return new ChannelInputStream();
    }

    @Override
    public void close() {
        closeQuietly(selector);
        closeQuietly(channel);
    }

    /**
     * Waits until the channel is ready
     *
     * @throws SocketTimeoutException if the deadline passes
     */
    private void await(int ops, @NonNull String action) throws IOException {
        while (true) {
            var remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0) {
                throw new SocketTimeoutException("podman did not " + action + " before the deadline");
            }

            key.interestOps(ops);
            selector.selectedKeys().clear();
            var ready = selector.select(Math.max(1L, remaining / 1_000_000L));
            selector.selectedKeys().clear();

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedIOException("interrupted while waiting to " + action);
            }
            if (ready > 0) {
                return;
            }
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException ignored) {
            // ignored
        }
    }

    /** Read stream of the channel */
    private final class ChannelInputStream extends InputStream {

        private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE).flip();

        private boolean eof;

        @Override
        public int read() throws IOException {
            if (!fill()) {
                return -1;
            }

            return buffer.get() & 0xFF;
        }

        @Override
        public int read(byte @NonNull [] target, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            if (!fill()) {
                return -1;
            }

            var count = Math.min(length, buffer.remaining());
            buffer.get(target, offset, count);

            return count;
        }

        @Override
        public int available() {
            return buffer.remaining();
        }

        /** @return {@code true} if there are bytes to read, {@code false} if the stream has ended */
        private boolean fill() throws IOException {
            if (buffer.hasRemaining()) {
                return true;
            }
            if (eof) {
                return false;
            }

            buffer.clear();
            int count;
            while ((count = channel.read(buffer)) == 0) {
                await(SelectionKey.OP_READ, "respond");
            }
            buffer.flip();

            if (count < 0) {
                eof = true;
                return false;
            }

            return true;
        }
    }
}
