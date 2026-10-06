package com.gabiacloud.podman.extension;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.junit.jupiter.api.extension.Extension;

/**
 * Loads {@code .env} in advance
 */
public final class DotenvExtension implements Extension {

    private static Dotenv dotenv;

    public DotenvExtension() {
        try {
            dotenv = Dotenv.load();
        } catch (DotenvException e) {
            dotenv = null;
        }
    }

    public static String get(String key) {
        if (dotenv == null) {
            return System.getenv(key);
        }

        return dotenv.get(key) == null ? System.getenv(key) : dotenv.get(key);
    }
}
