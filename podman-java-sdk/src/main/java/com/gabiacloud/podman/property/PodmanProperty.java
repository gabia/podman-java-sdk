package com.gabiacloud.podman.property;

import com.gabiacloud.podman.exception.PodmanClientException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * Podman connection settings
 */
@Getter
public final class PodmanProperty {

    /** Default socket path */
    public static final URI DEFAULT_HOST = URI.create("unix:///run/podman/podman.sock");

    /** Default timeout */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    public static final String UNIX_SCHEME = "unix";

    public static final String TCP_SCHEME = "tcp";

    private static final URI UNIX_BASE = URI.create("http://d/");

    private final @NonNull URI host;

    private final @NonNull Duration timeout;

    private final @NonNull URI base;

    /** Creates settings with the default timeout */
    public PodmanProperty(@NonNull URI host) {
        this(host, DEFAULT_TIMEOUT);
    }

    /**
     * @throws PodmanClientException if the scheme is neither {@code unix} nor {@code tcp}
     */
    @Builder
    public PodmanProperty(@NonNull URI host, @NonNull Duration timeout) {
        this.host = host;
        this.timeout = timeout;
        this.base = baseOf(host);
    }

    /** Uses the default settings and connects to the default socket on the host */
    @NonNull public static PodmanProperty defaults() {
        return new PodmanProperty(DEFAULT_HOST);
    }

    /** Checks whether a Unix socket is used */
    public boolean isUnixSocket() {
        return UNIX_SCHEME.equals(host.getScheme());
    }

    /**
     * Path of the socket file to connect to
     *
     * @throws PodmanClientException if the settings are not for a Unix socket
     */
    @NonNull public Path socketPath() {
        if (!isUnixSocket()) {
            throw new PodmanClientException("not a unix socket endpoint: " + host);
        }

        return Path.of(host.getPath());
    }

    /** Creates a request */
    public HttpRequest.@NonNull Builder newRequest(@NonNull URI uri) {
        return HttpRequest.newBuilder()
                .uri(base.resolve(uri))
                .header("Accept", "application/json")
                .timeout(timeout);
    }

    /** Creates a request with a JSON body */
    public HttpRequest.@NonNull Builder newJsonRequest(@NonNull URI uri) {
        return newRequest(uri).header("Content-Type", "application/json");
    }

    @NonNull private static URI baseOf(@NonNull URI host) {
        var scheme = host.getScheme();
        if (UNIX_SCHEME.equals(scheme)) {
            if (host.getPath() == null || host.getPath().isBlank()) {
                throw new PodmanClientException("unix endpoint has no socket path: " + host);
            }

            return UNIX_BASE;
        }

        if (TCP_SCHEME.equals(scheme)) {
            if (host.getAuthority() == null || host.getAuthority().isBlank()) {
                throw new PodmanClientException("tcp endpoint has no host: " + host);
            }

            return URI.create("http://" + host.getAuthority());
        }

        throw new PodmanClientException("unsupported endpoint scheme: " + host);
    }
}
