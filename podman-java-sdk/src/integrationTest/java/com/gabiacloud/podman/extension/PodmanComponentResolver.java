package com.gabiacloud.podman.extension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gabiacloud.podman.api.ContainerApi;
import com.gabiacloud.podman.api.ImageApi;
import com.gabiacloud.podman.api.PodApi;
import com.gabiacloud.podman.api.SystemApi;
import com.gabiacloud.podman.component.PodmanRequestExecutor;
import com.gabiacloud.podman.component.PodmanRequestProcessor;
import com.gabiacloud.podman.property.PodmanProperty;
import com.gabiacloud.podman.support.PodmanFixtures;
import java.util.Set;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Assembles the APIs and injects them into test methods
 */
public final class PodmanComponentResolver implements BeforeAllCallback, ParameterResolver {

    private static final Set<Class<?>> SUPPORTED_TYPES = Set.of(
            PodmanProperty.class,
            PodmanRequestExecutor.class,
            SystemApi.class,
            PodApi.class,
            ContainerApi.class,
            ImageApi.class,
            PodmanFixtures.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot().getStore(PodmanInfraExtension.NAMESPACE);
        if (store.get(PodmanRequestExecutor.class) != null) {
            return;
        }

        var properties = store.get(PodmanProperty.class, PodmanProperty.class);
        var mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        var executor = new PodmanRequestExecutor(mapper, PodmanRequestProcessor.forHost(properties));

        var pods = new PodApi(properties, mapper, executor);
        var images = new ImageApi(properties, mapper, executor);

        store.put(ObjectMapper.class, mapper);
        store.put(PodmanRequestExecutor.class, executor);
        store.put(SystemApi.class, new SystemApi(properties, mapper, executor));
        store.put(PodApi.class, pods);
        store.put(ContainerApi.class, new ContainerApi(properties, mapper, executor));
        store.put(ImageApi.class, images);
        store.put(PodmanFixtures.class, new PodmanFixtures(properties, mapper, executor, pods, images));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return SUPPORTED_TYPES.contains(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return extensionContext
                .getRoot()
                .getStore(PodmanInfraExtension.NAMESPACE)
                .get(parameterContext.getParameter().getType());
    }
}
