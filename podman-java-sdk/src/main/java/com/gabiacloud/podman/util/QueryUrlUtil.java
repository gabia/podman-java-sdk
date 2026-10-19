/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.util;

import com.gabiacloud.podman.component.RequestBodySerializer;
import com.gabiacloud.podman.exception.PodmanClientException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

/**
 * Appends query strings to request URIs
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryUrlUtil {

    /**
     * Creates a URI with the query string appended. Keys and values are encoded.
     *
     * @param uri URI without a query
     * @param params entries to add. If empty, {@code uri} is returned as is
     */
    @NonNull public static URI addQuery(@NonNull URI uri, @NonNull Map<String, String> params) {
        if (params.isEmpty()) {
            return uri;
        }

        var query = params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));

        return URI.create(uri + "?" + query);
    }

    /**
     * Builds the {@code filters} value for libpod list requests
     *
     * @param filters e.g. {@code {"label": ["app=web"]}}
     * @throws PodmanClientException if serialization fails
     */
    @NonNull public static String writeFilters(
            @NonNull RequestBodySerializer serializer, @NonNull Map<String, List<String>> filters) {
        try {
            return new String(serializer.serialize(filters), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PodmanClientException("cannot serialize filters", e);
        }
    }

    @NonNull private static String encode(@NonNull String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
