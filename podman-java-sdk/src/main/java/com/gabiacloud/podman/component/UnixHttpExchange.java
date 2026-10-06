package com.gabiacloud.podman.component;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Writes an HTTP/1.1 request and reads the response
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UnixHttpExchange {

    private static final String CRLF = "\r\n";

    private static final int MAX_HEADER_LINES = 100;

    private static final int MAX_LINE_LENGTH = 8 * 1024;

    private static final int SWITCHING_PROTOCOLS = 101;

    private static final int NO_CONTENT = 204;

    private static final int NOT_MODIFIED = 304;

    private static final List<String> RESERVED = List.of("host", "connection", "content-length");

    /**
     * Sends an HTTP request and returns the response
     *
     * @throws IOException if the exchange fails or the response is not HTTP
     */
    @NonNull static RawResponse send(@NonNull DeadlineChannel channel, @NonNull HttpRequest request, byte @Nullable [] body)
            throws IOException {
        channel.write(head(request, body).getBytes(StandardCharsets.UTF_8));
        if (body != null && body.length > 0) {
            channel.write(body);
        }

        var stream = channel.inputStream();
        var status = readStatus(readLine(stream));
        var headers = readHeaders(stream);

        return new RawResponse(status, bodyOf(status, headers, stream));
    }

    @NonNull private static String head(@NonNull HttpRequest request, byte @Nullable [] body) {
        var uri = request.uri();
        var head = new StringBuilder()
                .append(request.method())
                .append(' ')
                .append(target(uri))
                .append(" HTTP/1.1")
                .append(CRLF)
                .append("Host: ")
                .append(uri.getAuthority() == null ? "d" : uri.getAuthority())
                .append(CRLF)
                .append("Connection: close")
                .append(CRLF);

        request.headers().map().forEach((name, values) -> {
            if (!RESERVED.contains(name.toLowerCase(Locale.ROOT))) {
                values.forEach(
                        value -> head.append(name).append(": ").append(value).append(CRLF));
            }
        });

        if (body != null) {
            head.append("Content-Length: ").append(body.length).append(CRLF);
        }

        return head.append(CRLF).toString();
    }

    @NonNull private static String target(@NonNull URI uri) {
        var path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();

        return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
    }

    private static int readStatus(@NonNull String line) throws IOException {
        var parts = line.split(" ", 3);
        if (parts.length < 2 || !parts[0].startsWith("HTTP/")) {
            throw new IOException("not an HTTP response: " + line);
        }

        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IOException("malformed status line: " + line, e);
        }
    }

    @NonNull private static HttpHeaders readHeaders(@NonNull InputStream stream) throws IOException {
        var collected = new LinkedHashMap<String, List<String>>();

        for (var read = 0; read < MAX_HEADER_LINES; read++) {
            var line = readLine(stream);
            if (line.isEmpty()) {
                return HttpHeaders.of(collected, (name, value) -> true);
            }

            var separator = line.indexOf(':');
            if (separator < 0) {
                throw new IOException("malformed header line: " + line);
            }

            var name = line.substring(0, separator).trim();
            var value = line.substring(separator + 1).trim();
            collected.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
        }

        throw new IOException("too many header lines");
    }

    @NonNull private static InputStream bodyOf(int status, @NonNull HttpHeaders headers, @NonNull InputStream stream) {
        if (status == NO_CONTENT || status == NOT_MODIFIED || (status < 200 && status != SWITCHING_PROTOCOLS)) {
            return InputStream.nullInputStream();
        }

        if (headers.firstValue("Transfer-Encoding")
                .map(value -> value.toLowerCase(Locale.ROOT).contains("chunked"))
                .orElse(false)) {
            return new ChunkedInputStream(stream);
        }

        var length = headers.firstValueAsLong("Content-Length");
        if (length.isPresent()) {
            return new LengthLimitedInputStream(stream, length.getAsLong());
        }

        return stream;
    }

    @NonNull private static String readLine(@NonNull InputStream stream) throws IOException {
        var line = new StringBuilder();

        int next;
        while ((next = stream.read()) != -1) {
            if (next == '\n') {
                if (!line.isEmpty() && line.charAt(line.length() - 1) == '\r') {
                    line.setLength(line.length() - 1);
                }

                return line.toString();
            }

            line.append((char) next);
            if (line.length() > MAX_LINE_LENGTH) {
                throw new IOException("header line is too long");
            }
        }

        throw new EOFException("podman closed the connection before answering");
    }

    /** HTTP response */
    @Getter(AccessLevel.PACKAGE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class RawResponse {

        private final int statusCode;

        private final @NonNull InputStream body;

        byte @NonNull [] readBody() throws IOException {
            try (body) {
                return body.readAllBytes();
            }
        }
    }
}
