package com.gabiacloud.podman.annotation;

import com.gabiacloud.podman.extension.DotenvExtension;
import com.gabiacloud.podman.extension.PodmanComponentResolver;
import com.gabiacloud.podman.extension.PodmanInfraExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Runs tests against a real Podman instance
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({DotenvExtension.class, PodmanInfraExtension.class, PodmanComponentResolver.class})
public @interface PodmanTest {}
