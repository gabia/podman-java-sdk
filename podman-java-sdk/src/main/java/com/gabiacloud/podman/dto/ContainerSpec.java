/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Request body of libpod container creation
 */
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContainerSpec {

    @JsonProperty("name")
    private final @NonNull String name;

    @JsonProperty("image")
    private final @NonNull String image;

    /** Pod to join. {@code null} to create a standalone container */
    @JsonProperty("pod")
    private final @Nullable String pod;

    /** Command to run. {@code null} to use the image default */
    @JsonProperty("command")
    private final @Nullable List<String> command;
}
