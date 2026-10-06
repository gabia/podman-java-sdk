/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.exception;

import lombok.experimental.StandardException;

/**
 * Client-level exception
 */
@StandardException
public class PodmanClientException extends RuntimeException {}
