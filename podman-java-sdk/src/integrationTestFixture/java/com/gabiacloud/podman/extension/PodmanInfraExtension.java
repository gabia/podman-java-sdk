/*
 * SPDX-FileCopyrightText: 2026 Gabia Inc.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gabiacloud.podman.extension;

import com.gabiacloud.podman.property.PodmanProperty;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Leases a Podman instance from the {@link PodmanPool} for each top-level test class
 */
public final class PodmanInfraExtension implements BeforeAllCallback {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PodmanInfraExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getStore(NAMESPACE);
        if (store.get(PodmanPool.Lease.class) != null) {
            return;
        }

        var lease = PodmanPool.get().lease();
        store.put(PodmanPool.Lease.class, lease);
        store.put(PodmanProperty.class, lease.property());
    }
}
