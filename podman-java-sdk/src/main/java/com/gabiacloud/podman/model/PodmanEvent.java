/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Event of the Podman daemon ({@code GET /libpod/events})
 */
@Getter
@Builder
@ToString
@Jacksonized
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PodmanEvent {

    private static final String NAME_ATTRIBUTE = "name";

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    @JsonProperty("Type")
    @JsonAlias("type")
    private final @NonNull PodmanEventType type;

    /** Kind of change, e.g. {@code start}, {@code die}, {@code health_status} */
    @JsonProperty("Action")
    @JsonAlias({"action", "Status", "status"})
    private final @Nullable String action;

    @JsonProperty("Actor")
    @JsonAlias("actor")
    private final @Nullable Actor actor;

    /** Time of the event in epoch seconds */
    @JsonProperty("time")
    @JsonAlias("Time")
    private final long time;

    /** Time of the event in epoch nanoseconds. {@code 0} if absent */
    @JsonProperty("timeNano")
    @JsonAlias("TimeNano")
    private final long timeNano;

    /** ID of the object the event is about */
    @Nullable public String getActorId() {
        return actor == null ? null : actor.getId();
    }

    /** Name of the object the event is about, taken from the {@code name} attribute */
    @Nullable public String getName() {
        return getAttributes().get(NAME_ATTRIBUTE);
    }

    /** Attributes of the object, e.g. labels and image */
    @NonNull public Map<String, String> getAttributes() {
        return actor == null || actor.getAttributes() == null ? Map.of() : actor.getAttributes();
    }

    /** Time of the event */
    @NonNull public Instant getTimestamp() {
        return timeNano > 0
                ? Instant.ofEpochSecond(timeNano / NANOS_PER_SECOND, timeNano % NANOS_PER_SECOND)
                : Instant.ofEpochSecond(time);
    }

    /** {@code Actor} */
    @Getter
    @Builder
    @ToString
    @Jacksonized
    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Actor {

        @JsonProperty("ID")
        @JsonAlias("Id")
        private final @Nullable String id;

        @JsonProperty("Attributes")
        private final @Nullable Map<String, String> attributes;
    }
}
