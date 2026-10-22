/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class PodActionReportTest {

    @Test
    @DisplayName("[Success] No errors when Errs is missing, empty, or contains only null values")
    void noErrors() {
        assertNull(PodActionReport.builder().build().joinErrors());
        assertNull(PodActionReport.builder().errs(List.of()).build().joinErrors());
        assertNull(PodActionReport.builder()
                .errs(Arrays.asList(null, null))
                .build()
                .joinErrors());
    }

    @Test
    @DisplayName("[Success] Joins multiple error reasons")
    void joinsMessages() {
        var report = PodActionReport.builder()
                .errs(Arrays.asList("haproxy: no such image", null, "keepalived: denied"))
                .build();

        assertEquals("haproxy: no such image; keepalived: denied", report.joinErrors());
    }
}
